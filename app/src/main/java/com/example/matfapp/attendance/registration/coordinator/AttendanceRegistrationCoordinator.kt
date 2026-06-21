package com.example.matfapp.attendance.registration

import com.example.matfapp.R

class AttendanceRegistrationCoordinator(
    private val activity: AttendanceRegistrationActivity,
    private val stateManager: AttendanceRegistrationStateManager,
    private val qrController: AttendanceRegistrationQrController,
    private val geofencingController: AttendanceRegistrationGeofencingController,
    private val challengeController: AttendanceRegistrationChallengeController,
    private val renderView: () -> Unit,
) {
    fun start() {
        qrController.start(::handleQrOutcome)
        renderView()
    }

    fun restore(savedInstanceState: android.os.Bundle?): Boolean {
        if (!stateManager.restoreInstanceState(savedInstanceState)) {
            return false
        }
        val target = stateManager.state.attempt.activeTarget ?: return false
        onTargetDetected(target)
        return true
    }

    fun onDestroy() {
        geofencingController.onDestroy()
        challengeController.stopPolling()
        qrController.close()
    }

    fun onQrPermissionResult(requestCode: Int, grantResults: IntArray): Boolean {
        return when (qrController.handlePermissionResult(requestCode, grantResults)) {
            AttendanceRegistrationQrPermissionOutcome.NotHandled -> false
            AttendanceRegistrationQrPermissionOutcome.Granted -> {
                qrController.start(::handleQrOutcome)
                true
            }
            AttendanceRegistrationQrPermissionOutcome.Denied -> {
                updateUiAndRender { it.withMessage(activity.getString(R.string.attendance_scan_no_camera)) }
                true
            }
        }
    }

    fun onGeofencePermissionResult(requestCode: Int, grantResults: IntArray): Boolean {
        return when (geofencingController.handlePermissionResult(requestCode, grantResults)) {
            AttendanceRegistrationGeofencePermissionOutcome.NotHandled -> false
            AttendanceRegistrationGeofencePermissionOutcome.Granted -> {
                verifyGeofenceAndShowChallenge()
                true
            }
            AttendanceRegistrationGeofencePermissionOutcome.Denied -> {
                showBlocked(activity.getString(R.string.attendance_scan_needs_location))
                true
            }
        }
    }

    fun onTargetDetected(target: AttendanceTarget) {
        stateManager.updateAttempt { attempt ->
            attempt
                .withActiveTarget(target)
                .withStatus(AttendanceRegistrationStatus.ACTIVE)
        }
        stateManager.updateGeofence { geofence ->
            geofence
                .withLocationVerified(false)
                .withCurrentLocation(null, null)
        }
        stateManager.updateChallenge { challenge ->
            challenge
                .withChoicesVisible(false)
                .withButtonsEnabled(true)
                .withChallengeRound(null)
                .withBlockedUntilNextRound(null)
                .withEvent(null)
                .withChallenge(null)
                .withCurrentChallengeResponse(null)
        }
        challengeController.loadBootstrapChallenge(::handleChallengeLoadOutcome)
    }

    fun onSelectedCode(selectedCode: Int) {
        if (stateManager.state.attempt.status != AttendanceRegistrationStatus.ACTIVE) return
        if (stateManager.state.geofence.attendanceGeofenceEnabled && !stateManager.state.geofence.locationVerified) {
            updateUiAndRender { it.withMessage(activity.getString(R.string.attendance_scan_invalid_submit_state)) }
            return
        }

        challengeController.submitSelectedCode(
            selectedCode = selectedCode,
            latitude = stateManager.state.geofence.currentLatitude,
            longitude = stateManager.state.geofence.currentLongitude,
            onOutcome = ::handleChallengeSubmitOutcome,
        )
    }

    private fun handleQrOutcome(outcome: AttendanceRegistrationQrOutcome) {
        when (outcome) {
            AttendanceRegistrationQrOutcome.PermissionRequired -> {
                updateUiAndRender { it.withMessage(activity.getString(R.string.attendance_scan_no_camera)) }
            }
            AttendanceRegistrationQrOutcome.ScanningStarted -> {
                updateUiAndRender { it.asScanning() }
            }
            is AttendanceRegistrationQrOutcome.TargetDetected -> {
                qrController.stop()
                onTargetDetected(outcome.target)
            }
            is AttendanceRegistrationQrOutcome.InvalidCode -> {
                updateUiAndRender { it.withMessage(outcome.message) }
            }
        }
    }

    private fun handleChallengeLoadOutcome(outcome: AttendanceRegistrationChallengeLoadOutcome) {
        when (outcome) {
            AttendanceRegistrationChallengeLoadOutcome.LoadingChallenge -> {
                updateUiAndRender { it.asLoadingChallenge() }
            }
            AttendanceRegistrationChallengeLoadOutcome.RefreshingData -> {
                updateUiAndRender { it.asRefreshingData() }
            }
            is AttendanceRegistrationChallengeLoadOutcome.Loaded -> handleLoadedChallenge(outcome.response)
            is AttendanceRegistrationChallengeLoadOutcome.Failed -> showBlocked(outcome.message)
        }
    }

    private fun handleLoadedChallenge(response: AttendanceChallengeResponse) {
        val shouldStartPolling = stateManager.state.challenge.currentChallengeResponse == null
        stateManager.updateAttempt { attempt ->
            attempt.withAttendanceAttemptToken(
                response.attendanceAttemptToken?.takeIf { it.isNotBlank() } ?: attempt.attendanceAttemptToken,
            )
        }
        stateManager.updateChallenge { challenge ->
            challenge.withCurrentChallengeResponse(response)
        }
        geofencingController.configure(
            attendanceGeofenceEnabled = response.attendanceGeofenceEnabled,
            attendanceLocations = response.attendanceLocations,
        )
        if (shouldStartPolling) {
            challengeController.startPolling(::handleChallengeLoadOutcome)
        }
        if (!response.attendanceGeofenceEnabled) {
            showChallenge(response)
            return
        }
        if (response.attendanceLocations.isEmpty()) {
            showBlocked(geofencingController.missingLocationsMessage())
            return
        }
        if (stateManager.state.geofence.locationVerified) {
            showChallenge(response)
            return
        }
        verifyGeofenceAndShowChallenge(response)
    }

    private fun verifyGeofenceAndShowChallenge(response: AttendanceChallengeResponse? = null) {
        geofencingController.verifyLocation { outcome ->
            when (outcome) {
                AttendanceRegistrationGeofenceOutcome.PermissionRequired -> {
                    updateUiAndRender { it.asAwaitingLocationPermission() }
                    geofencingController.requestLocationPermission()
                }
                AttendanceRegistrationGeofenceOutcome.CheckingStarted -> {
                    updateUiAndRender { it.asCheckingLocation() }
                }
                is AttendanceRegistrationGeofenceOutcome.Allowed -> {
                    val currentResponse = response ?: stateManager.state.challenge.currentChallengeResponse
                    if (currentResponse != null) {
                        showChallenge(currentResponse)
                    }
                }
                is AttendanceRegistrationGeofenceOutcome.Blocked -> showBlocked(outcome.message)
            }
        }
    }

    private fun showChallenge(response: AttendanceChallengeResponse) {
        stateManager.updateAttempt { attempt ->
            attempt.withAttendanceAttemptToken(
                response.attendanceAttemptToken?.takeIf { it.isNotBlank() } ?: attempt.attendanceAttemptToken,
            )
        }
        var nextFrozenUntil: Long? = null
        stateManager.updateChallenge { challenge ->
            nextFrozenUntil = if (
                challenge.blockedUntilNextRound != null &&
                response.challenge.bucket > challenge.blockedUntilNextRound!!
            ) {
                null
            } else {
                challenge.blockedUntilNextRound
            }
            challenge
                .withEvent(response.event)
                .withChallenge(response.challenge)
                .withCurrentChallengeResponse(response)
                .withChallengeRound(response.challenge.bucket)
                .withBlockedUntilNextRound(nextFrozenUntil)
                .withButtonsEnabled(nextFrozenUntil == null)
                .withChoicesVisible(true)
        }
        stateManager.updateUiState { ui ->
            ui.copy(
                mode = AttendanceRegistrationScreenMode.ChallengeShown,
                progressBarVisible = false,
                previewVisible = false,
                eventPanelVisible = true,
                challengeChoicesVisible = true,
                buttonsEnabled = nextFrozenUntil == null,
                message = null,
                challengeChoicesErrorText = null,
            )
        }
        renderView()
    }

    private fun handleChallengeSubmitOutcome(outcome: AttendanceRegistrationChallengeSubmitOutcome) {
        when (outcome) {
            AttendanceRegistrationChallengeSubmitOutcome.Submitting -> {
                updateUiAndRender { it.withProgressBarVisible(true) }
            }
            AttendanceRegistrationChallengeSubmitOutcome.Success -> {
                challengeController.stopChallengePolling()
                stateManager.updateAttempt { it.withStatus(AttendanceRegistrationStatus.SUCCESS) }
                updateUiAndRender { it.asSuccess(activity.getString(R.string.attendance_scan_success_note)) }
            }
            is AttendanceRegistrationChallengeSubmitOutcome.WrongNumber -> freezeUntilNextRound(outcome.message)
            is AttendanceRegistrationChallengeSubmitOutcome.AttemptBlocked -> showBlocked(outcome.message)
            is AttendanceRegistrationChallengeSubmitOutcome.AttemptExpired -> showBlocked(outcome.message)
            is AttendanceRegistrationChallengeSubmitOutcome.OutsideClassTime -> {
                challengeController.stopChallengePolling()
                updateUiAndRender { it.withProgressBarVisible(false).withMessage(outcome.message) }
            }
            is AttendanceRegistrationChallengeSubmitOutcome.Failed -> {
                updateUiAndRender { it.withProgressBarVisible(false).withMessage(outcome.message) }
            }
        }
    }

    private fun freezeUntilNextRound(message: String) {
        stateManager.updateChallenge { challenge ->
            challenge.withButtonsEnabled(false).withBlockedUntilNextRound(stateManager.state.challenge.challengeRound)
        }
        updateUiAndRender {
            it.withProgressBarVisible(false)
                .withButtonsEnabled(false)
                .withMessage(message)
        }
    }

    private fun showBlocked(message: String) {
        stateManager.updateAttempt { it.withStatus(AttendanceRegistrationStatus.BLOCKED) }
        updateUiAndRender { it.asBlocked(message) }
        challengeController.stopChallengePolling()
        qrController.stop()
    }

    private fun updateUiAndRender(transform: (AttendanceRegistrationUiState) -> AttendanceRegistrationUiState) {
        stateManager.updateUiState(transform)
        renderView()
    }
}
