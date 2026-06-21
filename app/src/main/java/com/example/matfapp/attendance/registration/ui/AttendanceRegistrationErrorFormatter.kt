package com.example.matfapp.attendance.registration

import android.content.Context
import com.example.matfapp.R
import org.json.JSONObject

class AttendanceRegistrationErrorFormatter(
    private val context: Context,
) {
    fun parseServerErrorBody(body: String): String? {
        val trimmed = body.trim()
        if (!trimmed.startsWith("{")) {
            return null
        }
        return try {
            val json = JSONObject(trimmed)
            val parts = mutableListOf<String>()
            val errorText = json.optString("error").trim()
            if (errorText.isNotBlank()) {
                parts.add(errorText)
            }
            if (parts.isEmpty()) null else parts.joinToString("\n")
        } catch (_: Exception) {
            null
        }
    }
}
