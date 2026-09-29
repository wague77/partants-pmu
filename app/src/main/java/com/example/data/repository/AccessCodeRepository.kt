package com.example.data.repository

import com.example.data.local.dao.AccessCodeDao
import com.example.data.local.entity.AccessCodeEntity
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class AccessCodeRepository(
    private val dao: AccessCodeDao
) {
    private val secureRandom = SecureRandom()
    // Characters excluding ambiguous ones like 0, O, 1, I
    private val alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    val allCodes: Flow<List<AccessCodeEntity>> = dao.getAllCodes()
    val activeDeviceCode: Flow<AccessCodeEntity?> = dao.getActiveDeviceCodeFlow()

    fun generateComputerCode(prefix: String = "PMU"): String {
        fun randomBlock(length: Int): String {
            val sb = java.lang.StringBuilder(length)
            for (i in 0 until length) {
                val index = secureRandom.nextInt(alphabet.length)
                sb.append(alphabet[index])
            }
            return sb.toString()
        }
        return "$prefix-${randomBlock(4)}-${randomBlock(4)}-${randomBlock(4)}"
    }

    suspend fun initDefaultCodesIfEmpty() {
        val now = System.currentTimeMillis()
        val defaultCodes = listOf(
            AccessCodeEntity(
                code = "PMU-VIP-30J-7777",
                clientName = "Compte VIP Démo",
                createdAt = now,
                durationDays = 30,
                expiresAt = now + TimeUnit.DAYS.toMillis(30),
                status = AccessCodeEntity.STATUS_ACTIVE,
                isActivatedOnDevice = true,
                activatedAt = now,
                notes = "Code VIP par défaut 30 jours"
            ),
            AccessCodeEntity(
                code = "PMU-PRO-365-8888",
                clientName = "Abonné Prestige 1 An",
                createdAt = now,
                durationDays = 365,
                expiresAt = now + TimeUnit.DAYS.toMillis(365),
                status = AccessCodeEntity.STATUS_ACTIVE,
                isActivatedOnDevice = false,
                notes = "Code annuel 365 jours"
            )
        )
        dao.insertAll(defaultCodes)
    }

    suspend fun createCode(
        clientName: String,
        durationDays: Int,
        customCode: String? = null,
        notes: String = ""
    ): AccessCodeEntity {
        val codeValue = customCode?.ifBlank { null } ?: generateComputerCode()
        val now = System.currentTimeMillis()
        val expiresAt = now + TimeUnit.DAYS.toMillis(durationDays.toLong())

        val entity = AccessCodeEntity(
            code = codeValue.uppercase().trim(),
            clientName = clientName.ifBlank { "Client VIP" },
            createdAt = now,
            durationDays = durationDays,
            expiresAt = expiresAt,
            status = AccessCodeEntity.STATUS_ACTIVE,
            notes = notes
        )
        dao.insertCode(entity)
        return entity
    }

    suspend fun extendDuration(code: String, additionalDays: Int): Result<AccessCodeEntity> {
        val existing = dao.getCode(code) ?: return Result.failure(Exception("Code introuvable"))
        val now = System.currentTimeMillis()
        val baseTime = if (existing.expiresAt > now) existing.expiresAt else now
        val newExpiresAt = baseTime + TimeUnit.DAYS.toMillis(additionalDays.toLong())

        val updated = existing.copy(
            expiresAt = newExpiresAt,
            durationDays = existing.durationDays + additionalDays,
            status = if (existing.status == AccessCodeEntity.STATUS_BLOCKED || existing.status == AccessCodeEntity.STATUS_REVOKED) {
                existing.status
            } else {
                AccessCodeEntity.STATUS_ACTIVE
            }
        )
        dao.updateCode(updated)
        return Result.success(updated)
    }

    suspend fun setCustomExpiration(code: String, newExpiresAtMillis: Long): Result<AccessCodeEntity> {
        val existing = dao.getCode(code) ?: return Result.failure(Exception("Code introuvable"))
        val updated = existing.copy(
            expiresAt = newExpiresAtMillis
        )
        dao.updateCode(updated)
        return Result.success(updated)
    }

    suspend fun blockCode(code: String): Result<Unit> {
        val existing = dao.getCode(code) ?: return Result.failure(Exception("Code introuvable"))
        val updated = existing.copy(
            status = AccessCodeEntity.STATUS_BLOCKED,
            isActivatedOnDevice = false
        )
        dao.updateCode(updated)
        return Result.success(Unit)
    }

    suspend fun unblockCode(code: String): Result<Unit> {
        val existing = dao.getCode(code) ?: return Result.failure(Exception("Code introuvable"))
        val updated = existing.copy(
            status = AccessCodeEntity.STATUS_ACTIVE
        )
        dao.updateCode(updated)
        return Result.success(Unit)
    }

    suspend fun revokeCode(code: String): Result<Unit> {
        val existing = dao.getCode(code) ?: return Result.failure(Exception("Code introuvable"))
        val updated = existing.copy(
            status = AccessCodeEntity.STATUS_REVOKED,
            isActivatedOnDevice = false
        )
        dao.updateCode(updated)
        return Result.success(Unit)
    }

    suspend fun deleteCode(code: String): Result<Unit> {
        dao.deleteCodeByValue(code)
        return Result.success(Unit)
    }

    suspend fun verifyAndActivateCode(inputCode: String): Result<AccessCodeEntity> {
        val cleanedCode = inputCode.uppercase().trim()
        val found = dao.getCode(cleanedCode)
            ?: return Result.failure(Exception("Code d'accès invalide ou non reconnu."))

        if (found.status == AccessCodeEntity.STATUS_BLOCKED) {
            return Result.failure(Exception("Ce code d'accès a été bloqué par l'administrateur."))
        }
        if (found.status == AccessCodeEntity.STATUS_REVOKED) {
            return Result.failure(Exception("Ce code d'accès a été révoqué définitivement."))
        }
        if (found.isExpired) {
            return Result.failure(Exception("Ce code d'accès a expiré le ${found.formattedExpirationDate}."))
        }

        // Deactivate previous active code on this device
        dao.clearActiveDeviceCodes()

        // Activate this code
        val activated = found.copy(
            isActivatedOnDevice = true,
            activatedAt = System.currentTimeMillis()
        )
        dao.updateCode(activated)
        return Result.success(activated)
    }

    suspend fun deactivateCurrentDeviceLicense() {
        dao.clearActiveDeviceCodes()
    }
}
