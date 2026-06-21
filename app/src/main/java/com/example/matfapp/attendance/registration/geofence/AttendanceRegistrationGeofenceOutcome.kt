package com.example.matfapp.attendance.registration

sealed class AttendanceRegistrationGeofenceOutcome {
    data object PermissionRequired : AttendanceRegistrationGeofenceOutcome()
    data object CheckingStarted : AttendanceRegistrationGeofenceOutcome()
    data class Allowed(val latitude: Double, val longitude: Double) : AttendanceRegistrationGeofenceOutcome()
    data class Blocked(val message: String) : AttendanceRegistrationGeofenceOutcome()
}
