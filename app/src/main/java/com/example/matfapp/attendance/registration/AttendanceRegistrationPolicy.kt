package com.example.matfapp.attendance.registration

import com.example.matfapp.auth.ApiException

sealed class AttendanceRegistrationFailureReason {
    data object WrongChallengeNumber : AttendanceRegistrationFailureReason()
    data object OutsideClassTime : AttendanceRegistrationFailureReason()
    data object AttemptBlocked : AttendanceRegistrationFailureReason()
    data object AttemptExpired : AttendanceRegistrationFailureReason()
    data object GeofenceBlocked : AttendanceRegistrationFailureReason()
    data object LocationRequired : AttendanceRegistrationFailureReason()
    data object LocationMissing : AttendanceRegistrationFailureReason()
    data object Unauthorized : AttendanceRegistrationFailureReason()
    data object Unknown : AttendanceRegistrationFailureReason()
}

object AttendanceRegistrationPolicy {
    fun parseFailureReason(error: ApiException): AttendanceRegistrationFailureReason {
        return when (error.code) {
            409 -> AttendanceRegistrationFailureReason.WrongChallengeNumber
            401 -> AttendanceRegistrationFailureReason.Unauthorized
            403 -> when (error.errorCode) {
                "attendance_outside_class_time" -> AttendanceRegistrationFailureReason.OutsideClassTime
                "attendance_attempt_blocked" -> AttendanceRegistrationFailureReason.AttemptBlocked
                "attendance_attempt_expired" -> AttendanceRegistrationFailureReason.AttemptExpired
                "attendance_geofence_blocked" -> AttendanceRegistrationFailureReason.GeofenceBlocked
                "attendance_location_required" -> AttendanceRegistrationFailureReason.LocationRequired
                "attendance_location_missing" -> AttendanceRegistrationFailureReason.LocationMissing
                else -> AttendanceRegistrationFailureReason.Unknown
            }
            else -> AttendanceRegistrationFailureReason.Unknown
        }
    }
}
