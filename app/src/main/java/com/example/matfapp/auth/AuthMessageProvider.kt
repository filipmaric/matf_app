package com.example.matfapp.auth

import android.content.Context
import com.example.matfapp.R
import java.io.IOException

class AuthMessageProvider(
    private val context: Context,
) {
    fun messageFor(error: Exception): String {
        return when (error) {
            is IllegalArgumentException -> context.getString(R.string.auth_required_fields)
            is ApiException -> when (error.code) {
                401 -> context.getString(R.string.auth_invalid_credentials)
                409 -> context.getString(R.string.auth_phone_reserved)
                503 -> context.getString(R.string.auth_radius_unreachable)
                else -> context.getString(R.string.auth_server_error, error.code)
            }
            is IOException -> context.getString(R.string.auth_network_error, error.message ?: "unknown")
            else -> context.getString(R.string.auth_unknown_error, error.message ?: "unknown")
        }
    }
}
