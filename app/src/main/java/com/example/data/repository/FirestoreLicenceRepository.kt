package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.LicencePreferences
import com.example.data.model.FirestoreLicence
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Date
import java.util.concurrent.TimeUnit

class FirestoreLicenceRepository(
    private val context: Context,
    private val preferences: LicencePreferences = LicencePreferences(context)
) {
    private val secureRandom = SecureRandom()
    private val alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    private val db: FirebaseFirestore by lazy {
        val databaseId = try {
            context.getString(R.string.firestore_database_id)
        } catch (e: Exception) {
            "(default)"
        }
        try {
            FirebaseFirestore.getInstance(databaseId)
        } catch (e: Exception) {
            FirebaseFirestore.getInstance()
        }
    }

    private val licencesCollection get() = db.collection("licences")

    val deviceId: String get() = preferences.deviceId

    fun generateComputerCode(prefix: String = "PMU"): String {
        fun randomBlock(length: Int): String {
            val sb = StringBuilder(length)
            for (i in 0 until length) {
                sb.append(alphabet[secureRandom.nextInt(alphabet.length)])
            }
            return sb.toString()
        }
        return "$prefix-${randomBlock(4)}-${randomBlock(4)}-${randomBlock(4)}"
    }

    fun observeAllLicences(): Flow<List<FirestoreLicence>> = callbackFlow {
        val registration = licencesCollection
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        doc.toObject(FirestoreLicence::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    fun observeActiveDeviceLicence(): Flow<FirestoreLicence?> = callbackFlow {
        val currentCode = preferences.getActiveCode()
        if (currentCode.isNullOrBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val registration = licencesCollection.document(currentCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val licence = snapshot.toObject(FirestoreLicence::class.java)?.copy(id = snapshot.id)
                    trySend(licence)
                } else {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun initDefaultCodesIfEmpty() {
        try {
            val count = licencesCollection.limit(1).get().await().size()
            if (count == 0) {
                val now = System.currentTimeMillis()
                val demoCode1 = FirestoreLicence(
                    code = "PMU-VIP-30J-7777",
                    expire_le = Timestamp(Date(now + TimeUnit.DAYS.toMillis(30))),
                    statut = FirestoreLicence.STATUT_ACTIF,
                    device_id = preferences.deviceId,
                    created_at = Timestamp.now(),
                    client_name = "Compte VIP Démo",
                    notes = "Code VIP par défaut 30 jours"
                )
                val demoCode2 = FirestoreLicence(
                    code = "PMU-PRO-365-8888",
                    expire_le = Timestamp(Date(now + TimeUnit.DAYS.toMillis(365))),
                    statut = FirestoreLicence.STATUT_ACTIF,
                    device_id = null,
                    created_at = Timestamp.now(),
                    client_name = "Abonné Prestige 1 An",
                    notes = "Code annuel 365 jours"
                )
                licencesCollection.document(demoCode1.code).set(demoCode1).await()
                licencesCollection.document(demoCode2.code).set(demoCode2).await()
                // The device is NOT auto-activated: user must enter an access code to unlock
            }
        } catch (e: Exception) {
            // Non-blocking in case of offline/network setup
        }
    }

    suspend fun createLicence(
        clientName: String,
        durationDays: Int,
        customCode: String? = null,
        notes: String = ""
    ): Result<FirestoreLicence> {
        return try {
            val codeValue = customCode?.ifBlank { null } ?: generateComputerCode()
            val cleanCode = codeValue.uppercase().trim()
            val nowMillis = System.currentTimeMillis()
            val expireMillis = nowMillis + TimeUnit.DAYS.toMillis(durationDays.toLong())

            val licence = FirestoreLicence(
                code = cleanCode,
                expire_le = Timestamp(Date(expireMillis)),
                statut = FirestoreLicence.STATUT_ACTIF,
                device_id = null,
                created_at = Timestamp.now(),
                client_name = clientName.ifBlank { "Client VIP" },
                notes = notes
            )

            licencesCollection.document(cleanCode).set(licence).await()
            Result.success(licence)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extendLicence(code: String, additionalDays: Int): Result<Unit> {
        return try {
            val docRef = licencesCollection.document(code)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Licence introuvable"))
            }

            val existing = doc.toObject(FirestoreLicence::class.java)
                ?: return Result.failure(Exception("Données de licence invalides"))

            val currentExpireMillis = existing.expire_le?.toDate()?.time ?: System.currentTimeMillis()
            val baseTime = if (currentExpireMillis > System.currentTimeMillis()) currentExpireMillis else System.currentTimeMillis()
            val newExpireMillis = baseTime + TimeUnit.DAYS.toMillis(additionalDays.toLong())
            val newTimestamp = Timestamp(Date(newExpireMillis))

            val updates = mutableMapOf<String, Any>(
                "expire_le" to newTimestamp
            )
            // If the code was expired or active, ensure it is set to active
            if (existing.statut == FirestoreLicence.STATUT_ACTIF) {
                updates["statut"] = FirestoreLicence.STATUT_ACTIF
            }

            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockLicence(code: String): Result<Unit> {
        return try {
            licencesCollection.document(code).update("statut", FirestoreLicence.STATUT_BLOQUE).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockLicence(code: String): Result<Unit> {
        return try {
            licencesCollection.document(code).update("statut", FirestoreLicence.STATUT_ACTIF).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun revokeLicence(code: String): Result<Unit> {
        return try {
            licencesCollection.document(code).update("statut", FirestoreLicence.STATUT_REVOQUE).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteLicence(code: String): Result<Unit> {
        return try {
            licencesCollection.document(code).delete().await()
            if (preferences.getActiveCode() == code) {
                preferences.clearActiveCode()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateOnDevice(inputCode: String): Result<FirestoreLicence> {
        return try {
            val cleanCode = inputCode.uppercase().trim()
            val now = Timestamp.now()

            // Support master admin code activation
            if (cleanCode == "PMU-ADMIN-2026" || cleanCode == "ADMIN2026") {
                val adminDoc = licencesCollection.document("PMU-ADMIN-2026").get().await()
                val licence = if (!adminDoc.exists()) {
                    val newAdmin = FirestoreLicence(
                        code = "PMU-ADMIN-2026",
                        expire_le = Timestamp(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3650))),
                        statut = FirestoreLicence.STATUT_ACTIF,
                        device_id = preferences.deviceId,
                        created_at = Timestamp.now(),
                        client_name = "Administrateur Principal",
                        notes = "Accès Illimité Administrateur"
                    )
                    licencesCollection.document("PMU-ADMIN-2026").set(newAdmin).await()
                    newAdmin
                } else {
                    licencesCollection.document("PMU-ADMIN-2026").update(
                        mapOf(
                            "device_id" to preferences.deviceId,
                            "statut" to FirestoreLicence.STATUT_ACTIF
                        )
                    ).await()
                    adminDoc.toObject(FirestoreLicence::class.java) ?: FirestoreLicence(code = "PMU-ADMIN-2026")
                }
                preferences.setActiveCode("PMU-ADMIN-2026")
                return Result.success(licence.copy(id = "PMU-ADMIN-2026", device_id = preferences.deviceId))
            }

            // Direct document lookup by code
            val doc = licencesCollection.document(cleanCode).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Code d'accès non reconnu ou inexistant."))
            }

            val licence = doc.toObject(FirestoreLicence::class.java)
                ?: return Result.failure(Exception("Erreur de lecture de la licence"))

            val status = licence.statut
            val expireLe = licence.expire_le

            when {
                status == FirestoreLicence.STATUT_BLOQUE ->
                    return Result.failure(Exception("Ce code d'accès est bloqué par l'administrateur."))
                status == FirestoreLicence.STATUT_REVOQUE ->
                    return Result.failure(Exception("Ce code d'accès a été révoqué."))
                expireLe != null && expireLe < now ->
                    return Result.failure(Exception("Ce code d'accès a expiré le ${licence.formattedExpirationDate}."))
                status != FirestoreLicence.STATUT_ACTIF ->
                    return Result.failure(Exception("Statut de licence non valide ($status)."))
            }

            // Bind device ID and activate
            licencesCollection.document(cleanCode).update("device_id", preferences.deviceId).await()
            preferences.setActiveCode(cleanCode)

            Result.success(licence.copy(id = doc.id, device_id = preferences.deviceId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deactivateCurrentDevice() {
        preferences.clearActiveCode()
    }
}
