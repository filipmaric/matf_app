package com.example.matfapp.attendance.registration

import com.example.matfapp.attendance.AttendanceLocation

data class AttendanceTarget(
    val kind: String,
    val eventId: Long,
    val eventDate: String,
    val joinToken: String,
)

data class AttendanceEventInfo(
    val courseName: String?,
    val description: String?,
    val roomName: String?,
    val startSlot: Int,
    val endSlot: Int,
    val teacherName: String?,
    val eventDate: String?,
    val reservationDate: String?,
)

data class AttendanceChallengeInfo(
    val bucket: Long,
    val challengeChoices: List<Int>,
    val expiresIn: Int,
)

data class AttendanceChallengeResponse(
    val event: AttendanceEventInfo,
    val challenge: AttendanceChallengeInfo,
    val attendanceGeofenceAvailable: Boolean = false,
    val attendanceGeofenceEnabled: Boolean = false,
    val attendanceGeofenceWarning: String? = null,
    val attendanceLocations: List<AttendanceLocation> = emptyList(),
    val attendanceAttemptToken: String? = null,
)
