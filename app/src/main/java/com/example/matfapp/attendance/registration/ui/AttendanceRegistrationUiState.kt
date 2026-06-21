package rs.ac.bg.matf.attendance.registration

data class AttendanceRegistrationUiState(
    val mode: AttendanceRegistrationScreenMode = AttendanceRegistrationScreenMode.Scanning,
    val message: String? = null,
    val progressBarVisible: Boolean = false,
    val previewVisible: Boolean = true,
    val eventPanelVisible: Boolean = false,
    val challengeChoicesVisible: Boolean = false,
    val buttonsEnabled: Boolean = true,
    val closeVisible: Boolean = true,
    val challengeChoicesErrorText: String? = null,
)

enum class AttendanceRegistrationScreenMode {
    Scanning,
    RefreshingData,
    CheckingLocation,
    AwaitingLocationPermission,
    LoadingChallenge,
    ChallengeShown,
    Success,
    Blocked,
    Expired,
}

fun AttendanceRegistrationUiState.asScanning(): AttendanceRegistrationUiState {
    return copy(
        mode = AttendanceRegistrationScreenMode.Scanning,
        progressBarVisible = false,
        previewVisible = true,
        eventPanelVisible = false,
        challengeChoicesVisible = false,
        challengeChoicesErrorText = null,
        message = null,
    )
}

fun AttendanceRegistrationUiState.asCheckingLocation(): AttendanceRegistrationUiState {
    return copy(
        mode = AttendanceRegistrationScreenMode.CheckingLocation,
        progressBarVisible = true,
        previewVisible = false,
        message = null,
    )
}

fun AttendanceRegistrationUiState.asLoadingChallenge(): AttendanceRegistrationUiState {
    return copy(
        mode = AttendanceRegistrationScreenMode.LoadingChallenge,
        progressBarVisible = true,
        previewVisible = false,
        eventPanelVisible = false,
        challengeChoicesVisible = false,
        challengeChoicesErrorText = null,
        message = null,
    )
}

fun AttendanceRegistrationUiState.asRefreshingData(): AttendanceRegistrationUiState {
    return copy(
        mode = AttendanceRegistrationScreenMode.RefreshingData,
        progressBarVisible = true,
        message = null,
    )
}

fun AttendanceRegistrationUiState.asAwaitingLocationPermission(): AttendanceRegistrationUiState {
    return copy(
        mode = AttendanceRegistrationScreenMode.AwaitingLocationPermission,
        progressBarVisible = false,
        message = null,
    )
}

fun AttendanceRegistrationUiState.asSuccess(message: String): AttendanceRegistrationUiState {
    return copy(
        mode = AttendanceRegistrationScreenMode.Success,
        progressBarVisible = false,
        previewVisible = false,
        eventPanelVisible = true,
        challengeChoicesVisible = false,
        buttonsEnabled = false,
        message = message,
        challengeChoicesErrorText = null,
    )
}

fun AttendanceRegistrationUiState.asBlocked(message: String, buttonsEnabled: Boolean = false): AttendanceRegistrationUiState {
    return copy(
        mode = AttendanceRegistrationScreenMode.Blocked,
        progressBarVisible = false,
        previewVisible = false,
        eventPanelVisible = false,
        challengeChoicesVisible = false,
        buttonsEnabled = buttonsEnabled,
        message = message,
        challengeChoicesErrorText = message,
    )
}

fun AttendanceRegistrationUiState.withMessage(message: String?): AttendanceRegistrationUiState {
    return copy(message = message)
}

fun AttendanceRegistrationUiState.withButtonsEnabled(enabled: Boolean): AttendanceRegistrationUiState {
    return copy(buttonsEnabled = enabled)
}

fun AttendanceRegistrationUiState.withProgressBarVisible(progressBarVisible: Boolean): AttendanceRegistrationUiState {
    return copy(progressBarVisible = progressBarVisible)
}
