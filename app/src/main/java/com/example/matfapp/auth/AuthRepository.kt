package rs.ac.bg.matf.auth

import android.content.Context
import java.io.IOException

class AuthRepository(
    context: Context,
    private val api: HttpAuthApi,
) {
    private val tokenStore = SecureTokenStore(context.applicationContext)
    private val deviceIdStore = DeviceIdStore(context.applicationContext)

    fun login(username: String, password: String, deviceName: String): AuthSession {
        val response = api.login(
            LoginRequest(
                username = username,
                password = password,
                deviceId = deviceIdStore.getOrCreateDeviceId(),
                deviceName = deviceName,
            )
        )
        tokenStore.save(response.token)
        return response.toAuthSession()
    }

    fun restoreSession(): AuthSession? {
        val token = tokenStore.read() ?: return null
        return try {
            val me = api.me(token)
            me.toAuthSession(token)
        } catch (error: ApiException) {
            if (error.code == 401) {
                tokenStore.clear()
                null
            } else {
                throw error
            }
        } catch (error: IOException) {
            throw error
        }
    }

    fun logout() {
        val token = tokenStore.read()
        if (!token.isNullOrBlank()) {
            try {
                api.logout(token)
            } catch (_: Exception) {
                // Local logout still succeeds if the server is unavailable.
            }
        }
        tokenStore.clear()
    }

    private fun LoginResponse.toAuthSession(): AuthSession {
        return AuthSession(
            token = token,
            username = user.radiusUsername,
            deviceId = session.deviceId,
            deviceName = session.deviceName,
            expiresAt = expiresAt,
        )
    }

    private fun MeResponse.toAuthSession(token: String): AuthSession {
        return AuthSession(
            token = token,
            username = user.radiusUsername,
            deviceId = session.deviceId,
            deviceName = session.deviceName,
            expiresAt = session.expiresAt,
        )
    }
}
