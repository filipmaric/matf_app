package rs.ac.bg.matf.attendance.registration

sealed class AttendanceRegistrationChallengeLoadOutcome {
    data object LoadingChallenge : AttendanceRegistrationChallengeLoadOutcome()
    data object RefreshingData : AttendanceRegistrationChallengeLoadOutcome()
    data class Loaded(val response: AttendanceChallengeResponse) : AttendanceRegistrationChallengeLoadOutcome()
    data class Failed(val message: String) : AttendanceRegistrationChallengeLoadOutcome()
}
