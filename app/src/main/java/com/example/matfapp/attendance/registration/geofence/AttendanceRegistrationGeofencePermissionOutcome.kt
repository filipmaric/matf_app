package rs.ac.bg.matf.attendance.registration

sealed class AttendanceRegistrationGeofencePermissionOutcome {
    data object NotHandled : AttendanceRegistrationGeofencePermissionOutcome()
    data object Granted : AttendanceRegistrationGeofencePermissionOutcome()
    data object Denied : AttendanceRegistrationGeofencePermissionOutcome()
}
