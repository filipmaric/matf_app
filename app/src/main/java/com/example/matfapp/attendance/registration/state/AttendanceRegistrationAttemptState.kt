package rs.ac.bg.matf.attendance.registration

data class AttendanceRegistrationAttemptState(
    val activeTarget: AttendanceTarget? = null,
    val attendanceAttemptToken: String? = null,
    val status: AttendanceRegistrationStatus = AttendanceRegistrationStatus.ACTIVE,
)

fun AttendanceRegistrationAttemptState.withActiveTarget(activeTarget: AttendanceTarget?): AttendanceRegistrationAttemptState {
    return copy(activeTarget = activeTarget)
}

fun AttendanceRegistrationAttemptState.withAttendanceAttemptToken(attendanceAttemptToken: String?): AttendanceRegistrationAttemptState {
    return copy(attendanceAttemptToken = attendanceAttemptToken)
}

fun AttendanceRegistrationAttemptState.withStatus(status: AttendanceRegistrationStatus): AttendanceRegistrationAttemptState {
    return copy(status = status)
}
