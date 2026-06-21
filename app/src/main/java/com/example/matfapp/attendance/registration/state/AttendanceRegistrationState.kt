package com.example.matfapp.attendance.registration

data class AttendanceRegistrationState(
    val attempt: AttendanceRegistrationAttemptState = AttendanceRegistrationAttemptState(),
    val geofence: AttendanceRegistrationGeofenceState = AttendanceRegistrationGeofenceState(),
    val challenge: AttendanceRegistrationChallengeState = AttendanceRegistrationChallengeState(),
    val ui: AttendanceRegistrationUiState = AttendanceRegistrationUiState(),
)

fun AttendanceRegistrationState.withAttempt(transform: (AttendanceRegistrationAttemptState) -> AttendanceRegistrationAttemptState): AttendanceRegistrationState {
    return copy(attempt = transform(attempt))
}

fun AttendanceRegistrationState.withGeofence(transform: (AttendanceRegistrationGeofenceState) -> AttendanceRegistrationGeofenceState): AttendanceRegistrationState {
    return copy(geofence = transform(geofence))
}

fun AttendanceRegistrationState.withChallenge(transform: (AttendanceRegistrationChallengeState) -> AttendanceRegistrationChallengeState): AttendanceRegistrationState {
    return copy(challenge = transform(challenge))
}

fun AttendanceRegistrationState.withUi(transform: (AttendanceRegistrationUiState) -> AttendanceRegistrationUiState): AttendanceRegistrationState {
    return copy(ui = transform(ui))
}
