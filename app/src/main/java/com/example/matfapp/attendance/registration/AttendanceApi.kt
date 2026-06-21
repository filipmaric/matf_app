package com.example.matfapp.attendance.registration

import com.example.matfapp.auth.ApiException
import com.example.matfapp.attendance.AttendanceLocation
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AttendanceApi(private val baseUrl: String) {
    fun getChallenge(token: String, target: AttendanceTarget): AttendanceChallengeResponse {
        val path = buildChallengePath(target, null)
        val response = requestJson(path, "GET", null, token)
        return parseChallengeResponse(response)
    }

    fun getChallenge(
        token: String,
        target: AttendanceTarget,
        attendanceAttemptToken: String,
    ): AttendanceChallengeResponse {
        val path = buildChallengePath(target, attendanceAttemptToken)
        val response = requestJson(path, "GET", null, token)
        return parseChallengeResponse(response)
    }

    fun submit(
        token: String,
        target: AttendanceTarget,
        selectedCode: Int,
        attendanceAttemptToken: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ): JSONObject {
        val body = JSONObject()
            .put("selected_code", selectedCode)
        if (!attendanceAttemptToken.isNullOrBlank()) {
            body.put("attendance_attempt_token", attendanceAttemptToken)
        } else {
            body.put("join_token", target.joinToken)
        }
        if (latitude != null && longitude != null) {
            body.put("latitude", latitude)
            body.put("longitude", longitude)
        }
        return requestJson(buildPath(target, "join"), "POST", body, token)
    }

    private fun buildChallengePath(target: AttendanceTarget, attendanceAttemptToken: String?): String {
        val query = buildString {
            if (!attendanceAttemptToken.isNullOrBlank()) {
                append("?attendance_attempt_token=")
                append(encode(attendanceAttemptToken))
            } else {
                append("?join_token=")
                append(encode(target.joinToken))
            }
        }
        return buildPath(target, "challenge") + query
    }

    private fun buildPath(target: AttendanceTarget, suffix: String): String {
        return "attendance/${target.kind}/${target.eventId}/${target.eventDate}/$suffix"
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
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

    private fun parseChallengeResponse(json: JSONObject): AttendanceChallengeResponse {
        val event = json.getJSONObject("event")
        val challenge = json.getJSONObject("challenge")
        return AttendanceChallengeResponse(
            event = AttendanceEventInfo(
                courseName = event.optString("course_name").takeIf { it.isNotBlank() },
                description = event.optString("description").takeIf { it.isNotBlank() },
                roomName = event.optString("room_name").takeIf { it.isNotBlank() },
                startSlot = event.optInt("start_slot", 0),
                endSlot = event.optInt("end_slot", 0),
                teacherName = event.optString("teacher_name").takeIf { it.isNotBlank() },
                eventDate = event.optString("event_date").takeIf { it.isNotBlank() },
                reservationDate = event.optString("reservation_date").takeIf { it.isNotBlank() },
            ),
            challenge = AttendanceChallengeInfo(
                bucket = challenge.optLong("bucket", 0L),
                challengeChoices = challenge.getJSONArray("options").let { array ->
                    buildList {
                        for (index in 0 until array.length()) {
                            add(array.getInt(index))
                        }
                    }
                },
                expiresIn = challenge.optInt("expires_in", 0),
            ),
            attendanceGeofenceAvailable = json.optBoolean("attendance_geofence_available", false),
            attendanceGeofenceEnabled = json.optBoolean("attendance_geofence_enabled", false),
            attendanceGeofenceWarning = json.optString("attendance_geofence_warning").takeIf { it.isNotBlank() },
            attendanceLocations = json.optJSONArray("attendance_locations")?.let { array ->
                buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        add(
                            AttendanceLocation(
                                name = item.optString("name").ifBlank { "Lokacija ${index + 1}" },
                                latitude = item.optDouble("latitude"),
                                longitude = item.optDouble("longitude"),
                                radiusMeters = item.optDouble("radius_m", item.optDouble("radius", 100.0)),
                            )
                        )
                    }
                }
            } ?: emptyList(),
            attendanceAttemptToken = json.optString("attendance_attempt_token").takeIf { it.isNotBlank() },
        )
    }
}
