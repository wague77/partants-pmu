package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class FirestoreLicence(
    @DocumentId val id: String = "",
    val code: String = "",
    val expire_le: Timestamp? = null,
    val statut: String = STATUT_ACTIF, // "actif", "bloque", "revoque"
    val device_id: String? = null,
    val created_at: Timestamp? = null,
    val client_name: String = "",
    val notes: String = ""
) {
    val isExpired: Boolean
        get() {
            val expiryMillis = expire_le?.toDate()?.time ?: 0L
            return System.currentTimeMillis() > expiryMillis
        }

    val effectiveStatus: String
        get() = when {
            statut == STATUT_REVOQUE -> STATUT_REVOQUE
            statut == STATUT_BLOQUE -> STATUT_BLOQUE
            isExpired -> STATUT_EXPIRE
            else -> STATUT_ACTIF
        }

    val remainingDays: Long
        get() {
            val expiryMillis = expire_le?.toDate()?.time ?: 0L
            val diff = expiryMillis - System.currentTimeMillis()
            return if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff) + 1 else 0
        }

    val formattedExpirationDate: String
        get() {
            val date = expire_le?.toDate() ?: return "--"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
            return sdf.format(date)
        }

    val formattedCreatedDate: String
        get() {
            val date = created_at?.toDate() ?: return "--"
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            return sdf.format(date)
        }

    companion object {
        const val STATUT_ACTIF = "actif"
        const val STATUT_BLOQUE = "bloque"
        const val STATUT_REVOQUE = "revoque"
        const val STATUT_EXPIRE = "expire"
    }
}
