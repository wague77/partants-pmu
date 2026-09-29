package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Entity(tableName = "access_codes")
data class AccessCodeEntity(
    @PrimaryKey val code: String,
    val clientName: String,
    val createdAt: Long,
    val durationDays: Int,
    val expiresAt: Long,
    val status: String = STATUS_ACTIVE, // "ACTIVE", "BLOCKED", "REVOKED"
    val isActivatedOnDevice: Boolean = false,
    val activatedAt: Long? = null,
    val notes: String = ""
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt

    val effectiveStatus: String
        get() = when {
            status == STATUS_REVOKED -> STATUS_REVOKED
            status == STATUS_BLOCKED -> STATUS_BLOCKED
            isExpired -> STATUS_EXPIRED
            else -> STATUS_ACTIVE
        }

    val remainingDays: Long
        get() {
            val diff = expiresAt - System.currentTimeMillis()
            return if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff) + 1 else 0
        }

    val formattedExpirationDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
            return sdf.format(Date(expiresAt))
        }

    val formattedCreatedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            return sdf.format(Date(createdAt))
        }

    companion object {
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_BLOCKED = "BLOCKED"
        const val STATUS_REVOKED = "REVOKED"
        const val STATUS_EXPIRED = "EXPIRED"
    }
}
