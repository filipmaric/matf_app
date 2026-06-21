package rs.ac.bg.matf.attendance.registration

sealed class AttendanceRegistrationChallengeSubmitOutcome {
    data object Submitting : AttendanceRegistrationChallengeSubmitOutcome()
    data object Success : AttendanceRegistrationChallengeSubmitOutcome()
    data class WrongNumber(val message: String) : AttendanceRegistrationChallengeSubmitOutcome()
    data class AttemptBlocked(val message: String) : AttendanceRegistrationChallengeSubmitOutcome()
    data class AttemptExpired(val message: String) : AttendanceRegistrationChallengeSubmitOutcome()
    data class OutsideClassTime(val message: String) : AttendanceRegistrationChallengeSubmitOutcome()
    data class Failed(val message: String) : AttendanceRegistrationChallengeSubmitOutcome()
}
