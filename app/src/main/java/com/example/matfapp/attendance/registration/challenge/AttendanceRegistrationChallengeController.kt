package com.example.matfapp.attendance.registration

import com.example.matfapp.auth.ApiException
import com.example.matfapp.R
import java.util.concurrent.ExecutorService

class AttendanceRegistrationChallengeController(
    private val activity: AttendanceRegistrationActivity,
    private val bearerToken: String,
    private val stateManager: AttendanceRegistrationStateManager,
    private val api: AttendanceApi,
    private val networkExecutor: ExecutorService,
    private val pollingController: AttendanceRegistrationPollingController,
    private val errorFormatter: AttendanceRegistrationErrorFormatter,
) {
    companion object {
        private const val MIN_POLL_DELAY_MS = 1000L
        private const val POLL_RATIO = 0.9
    }

    fun loadBootstrapChallenge(
        onOutcome: (AttendanceRegistrationChallengeLoadOutcome) -> Unit,
        refreshing: Boolean = false,
    ) {
        val target = stateManager.state.attempt.activeTarget ?: return
        val attendanceAttemptToken = stateManager.state.attempt.attendanceAttemptToken?.takeIf { it.isNotBlank() }
        onOutcome(
            if (refreshing && stateManager.state.challenge.currentChallengeResponse != null) {
                AttendanceRegistrationChallengeLoadOutcome.RefreshingData
            } else {
                AttendanceRegistrationChallengeLoadOutcome.LoadingChallenge
            }
        )
        networkExecutor.execute {
            try {
                val response = attendanceAttemptToken?.let { token -> api.getChallenge(bearerToken, target, token) }
                    ?: api.getChallenge(bearerToken, target)
                activity.runOnUiThread { onOutcome(AttendanceRegistrationChallengeLoadOutcome.Loaded(response)) }
            } catch (error: ApiException) {
                activity.runOnUiThread { onOutcome(AttendanceRegistrationChallengeLoadOutcome.Failed(parseErrorMessage(error))) }
            } catch (error: Exception) {
                activity.runOnUiThread {
                    onOutcome(AttendanceRegistrationChallengeLoadOutcome.Failed(error.message ?: activity.getString(R.string.attendance_scan_error)))
                }
            }
        }
    }

    fun startPolling(onOutcome: (AttendanceRegistrationChallengeLoadOutcome) -> Unit) {
        scheduleNextPoll(onOutcome)
    }

    fun stopPolling() {
        pollingController.stop()
    }

    private fun scheduleNextPoll(
        onOutcome: (AttendanceRegistrationChallengeLoadOutcome) -> Unit,
    ) {
        val currentResponse = stateManager.state.challenge.currentChallengeResponse ?: return
        val delayMs = computePollDelayMs(currentResponse.challenge.expiresIn)
        pollingController.schedule(delayMs) {
            if (
                stateManager.state.attempt.status != AttendanceRegistrationStatus.ACTIVE ||
                stateManager.state.attempt.activeTarget == null
            ) {
                return@schedule
            }
            loadBootstrapChallenge(
                refreshing = true,
                onOutcome = { outcome ->
                    onOutcome(outcome)
                    if (outcome is AttendanceRegistrationChallengeLoadOutcome.Loaded) {
                        scheduleNextPoll(onOutcome)
                    }
                },
            )
        }
    }

    private fun computePollDelayMs(expiresInSeconds: Int): Long {
        val proposedDelay = (expiresInSeconds * 1000.0 * POLL_RATIO).toLong()
        return maxOf(MIN_POLL_DELAY_MS, proposedDelay)
    }

    fun submitSelectedCode(
        selectedCode: Int,
        latitude: Double?,
        longitude: Double?,
        onOutcome: (AttendanceRegistrationChallengeSubmitOutcome) -> Unit,
    ) {
        val target = stateManager.state.attempt.activeTarget ?: return
        val attendanceAttemptToken = stateManager.state.attempt.attendanceAttemptToken?.takeIf { it.isNotBlank() }
        onOutcome(AttendanceRegistrationChallengeSubmitOutcome.Submitting)
        networkExecutor.execute {
            try {
                api.submit(
                    token = bearerToken,
                    target = target,
                    selectedCode = selectedCode,
                    attendanceAttemptToken = attendanceAttemptToken,
                    latitude = latitude,
                    longitude = longitude,
                )
                activity.runOnUiThread { onOutcome(AttendanceRegistrationChallengeSubmitOutcome.Success) }
            } catch (error: ApiException) {
                activity.runOnUiThread { onOutcome(mapSubmitError(error)) }
            } catch (error: Exception) {
                activity.runOnUiThread {
                    onOutcome(AttendanceRegistrationChallengeSubmitOutcome.Failed(error.message ?: activity.getString(R.string.attendance_scan_error)))
                }
            }
        }
    }

    fun stopChallengePolling() {
        pollingController.stop()
    }

    private fun parseErrorMessage(error: ApiException): String {
        return parseServerErrorBody(error.body) ?: error.errorMessage ?: error.body
    }

    private fun parseServerErrorBody(body: String): String? = errorFormatter.parseServerErrorBody(body)

    private fun mapSubmitError(error: ApiException): AttendanceRegistrationChallengeSubmitOutcome {
        val message = parseErrorMessage(error)
        return when (AttendanceRegistrationPolicy.parseFailureReason(error)) {
            AttendanceRegistrationFailureReason.WrongChallengeNumber -> AttendanceRegistrationChallengeSubmitOutcome.WrongNumber(message)
            AttendanceRegistrationFailureReason.AttemptBlocked -> AttendanceRegistrationChallengeSubmitOutcome.AttemptBlocked(message)
            AttendanceRegistrationFailureReason.AttemptExpired -> AttendanceRegistrationChallengeSubmitOutcome.AttemptExpired(message)
            AttendanceRegistrationFailureReason.OutsideClassTime -> AttendanceRegistrationChallengeSubmitOutcome.OutsideClassTime(message)
            else -> AttendanceRegistrationChallengeSubmitOutcome.Failed(message)
        }
    }
}
