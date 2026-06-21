package com.example.matfapp.attendance.registration

data class AttendanceRegistrationChallengeState(
    val event: AttendanceEventInfo? = null,
    val challenge: AttendanceChallengeInfo? = null,
    val currentChallengeResponse: AttendanceChallengeResponse? = null,
    val challengeRound: Long? = null,
    val blockedUntilNextRound: Long? = null,
    val buttonsEnabled: Boolean = true,
    val choicesVisible: Boolean = false,
)

fun AttendanceRegistrationChallengeState.withEvent(event: AttendanceEventInfo?): AttendanceRegistrationChallengeState {
    return copy(event = event)
}

fun AttendanceRegistrationChallengeState.withChallenge(challenge: AttendanceChallengeInfo?): AttendanceRegistrationChallengeState {
    return copy(challenge = challenge)
}

fun AttendanceRegistrationChallengeState.withCurrentChallengeResponse(currentChallengeResponse: AttendanceChallengeResponse?): AttendanceRegistrationChallengeState {
    return copy(currentChallengeResponse = currentChallengeResponse)
}

fun AttendanceRegistrationChallengeState.withChallengeRound(challengeRound: Long?): AttendanceRegistrationChallengeState {
    return copy(challengeRound = challengeRound)
}

fun AttendanceRegistrationChallengeState.withBlockedUntilNextRound(blockedUntilNextRound: Long?): AttendanceRegistrationChallengeState {
    return copy(blockedUntilNextRound = blockedUntilNextRound)
}

fun AttendanceRegistrationChallengeState.withButtonsEnabled(buttonsEnabled: Boolean): AttendanceRegistrationChallengeState {
    return copy(buttonsEnabled = buttonsEnabled)
}

fun AttendanceRegistrationChallengeState.withChoicesVisible(choicesVisible: Boolean): AttendanceRegistrationChallengeState {
    return copy(choicesVisible = choicesVisible)
}
