package com.example.matfapp.attendance.history

import com.example.matfapp.auth.ApiException
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AttendanceHistoryApi(private val baseUrl: String) {
    fun attendanceHistory(token: String): AttendanceHistoryResponse {
        val response = requestJson("mobile/attendance/history", "GET", null, token)
        return parseAttendanceHistory(response)
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

    private fun parseAttendanceHistory(json: JSONObject): AttendanceHistoryResponse {
        val currentSemester = json.optJSONObject("current_semester")?.let { semester ->
            SemesterInfo(
                id = semester.getLong("id"),
                name = semester.getString("name"),
                startDate = semester.getString("start_date"),
                endDate = semester.getString("end_date"),
            )
        }
        val summaries = json.optJSONArray("summaries")?.let { array ->
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        AttendanceCourseSummary(
                            courseName = item.optString("course_name"),
                            courseCode = item.optString("course_code").takeIf { it.isNotBlank() },
                            attendedLessons = item.optInt("attended_lessons", 0),
                            totalLessonsWithRecordedAttendance = item.optInt(
                                "total_lessons_with_recorded_attendance",
                                0,
                            ),
                            totalLessonsWithYourAttendance = item.optInt(
                                "attended_lessons",
                                0,
                            ),
                        )
                    )
                }
            }
        } ?: emptyList()
        return AttendanceHistoryResponse(currentSemester = currentSemester, summaries = summaries)
    }
}
