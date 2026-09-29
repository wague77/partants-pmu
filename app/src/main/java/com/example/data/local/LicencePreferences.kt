package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.util.UUID

class LicencePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pmu_licence_prefs", Context.MODE_PRIVATE)

    val deviceId: String by lazy {
        val stored = prefs.getString(KEY_DEVICE_ID, null)
        if (!stored.isNullOrBlank()) {
            stored
        } else {
            val androidId = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                null
            }
            val newId = if (!androidId.isNullOrBlank()) androidId else "device_${UUID.randomUUID()}"
            prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
            newId
        }
    }

    fun getActiveCode(): String? {
        return prefs.getString(KEY_ACTIVE_CODE, null)
    }

    fun setActiveCode(code: String) {
        prefs.edit().putString(KEY_ACTIVE_CODE, code).apply()
    }

    fun clearActiveCode() {
        prefs.edit().remove(KEY_ACTIVE_CODE).apply()
    }

    companion object {
        private const val KEY_ACTIVE_CODE = "active_licence_code"
        private const val KEY_DEVICE_ID = "unique_device_id"
    }
}
