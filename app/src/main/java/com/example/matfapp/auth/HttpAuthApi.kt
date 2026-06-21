package com.example.matfapp.auth

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ApiException(
    val code: Int,
    val body: String,
    val errorCode: String? = null,
    val errorMessage: String? = null,
) : IOException("HTTP $code: $body")

class HttpAuthApi(private val baseUrl: String) {
    fun login(request: LoginRequest): LoginResponse {
        val json = JSONObject()
            .put("username", request.username)
            .put("password", request.password)
            .put("device_id", request.deviceId)
            .put("device_name", request.deviceName)
        val response = requestJson("mobile/login", "POST", json, null)
        return parseLoginResponse(response)
    }

    fun me(token: String): MeResponse {
        val response = requestJson("mobile/me", "GET", null, token)
        return parseMeResponse(response)
    }

    fun logout(token: String) {
        requestJson("mobile/logout", "POST", JSONObject(), token)
    }

    private fun requestJson(
        path: String,
        method: String,
        body: JSONObject?,
        token: String?,
    ): JSONObject {
        val url = URL(baseUrl.trimEnd('/') + "/" + path.trimStart('/'))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 10_000
            doInput = true
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            if (!token.isNullOrBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
            }
        }

        body?.let {
            connection.outputStream.use { output ->
                output.write(it.toString().toByteArray(Charsets.UTF_8))
            }
        }

        val code = connection.responseCode
        val rawBody = (if (code in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader()
            ?.use { it.readText() }
            .orEmpty()

        if (code !in 200..299) {
            val bodyText = rawBody.ifBlank { connection.responseMessage.orEmpty() }
            val parsed = bodyText.takeIf { it.trim().startsWith("{") }?.let { text ->
                runCatching { JSONObject(text) }.getOrNull()
            }
            throw ApiException(
                code = code,
                body = bodyText,
                errorCode = parsed?.optString("error_code")?.takeIf { it.isNotBlank() },
                errorMessage = parsed?.optString("error")?.takeIf { it.isNotBlank() },
            )
        }

        if (rawBody.isBlank()) {
            return JSONObject()
        }
        return JSONObject(rawBody)
    }

    private fun parseLoginResponse(json: JSONObject): LoginResponse {
        return LoginResponse(
            token = json.getString("token"),
            tokenType = json.optString("token_type", "Bearer"),
            expiresAt = json.getString("expires_at"),
            user = parseUser(json.getJSONObject("user")),
            session = parseSession(json.getJSONObject("session")),
        )
    }

    private fun parseMeResponse(json: JSONObject): MeResponse {
        return MeResponse(
            user = parseUser(json.getJSONObject("user")),
            session = parseSession(json.getJSONObject("session")),
        )
    }

    private fun parseUser(json: JSONObject): RadiusUser {
        return RadiusUser(
            id = json.getLong("id"),
            radiusUsername = json.getString("radius_username"),
        )
    }

    private fun parseSession(json: JSONObject): SessionInfo {
        return SessionInfo(
            id = json.getLong("id"),
            deviceId = json.getString("device_id"),
            deviceName = json.getString("device_name"),
            expiresAt = json.getString("expires_at"),
        )
    }

}
