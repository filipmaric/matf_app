package rs.ac.bg.matf.attendance.registration

sealed class AttendanceRegistrationQrOutcome {
    data object PermissionRequired : AttendanceRegistrationQrOutcome()
    data object ScanningStarted : AttendanceRegistrationQrOutcome()
    data class TargetDetected(val target: AttendanceTarget) : AttendanceRegistrationQrOutcome()
    data class InvalidCode(val message: String) : AttendanceRegistrationQrOutcome()
}
