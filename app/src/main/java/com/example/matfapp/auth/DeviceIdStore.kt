package rs.ac.bg.matf.auth

import android.content.Context
import android.provider.Settings
import java.util.UUID

class DeviceIdStore(context: Context) {
    private val applicationContext = context.applicationContext
    private val prefs = applicationContext.getSharedPreferences("auth_device", Context.MODE_PRIVATE)

    fun getOrCreateDeviceId(): String {
        val androidId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID,
        )?.trim()

        if (!androidId.isNullOrBlank()) {
            return androidId
        }

        val existing = prefs.getString(KEY_DEVICE_ID_FALLBACK, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }

        val generated = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID_FALLBACK, generated).apply()
        return generated
    }

    private companion object {
        const val KEY_DEVICE_ID_FALLBACK = "device_id_fallback"
    }
}
