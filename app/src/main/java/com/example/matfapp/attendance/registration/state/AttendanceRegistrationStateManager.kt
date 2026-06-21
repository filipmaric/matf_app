package rs.ac.bg.matf.attendance.registration

import android.os.Bundle

class AttendanceRegistrationStateManager {
    var state: AttendanceRegistrationState = AttendanceRegistrationState()
        private set

    companion object {
        private const val STATE_TARGET_KIND = "state_target_kind"
        private const val STATE_TARGET_EVENT_ID = "state_target_event_id"
        private const val STATE_TARGET_EVENT_DATE = "state_target_event_date"
        private const val STATE_TARGET_JOIN_TOKEN = "state_target_join_token"
        private const val STATE_ATTENDANCE_ATTEMPT_TOKEN = "state_attendance_attempt_token"
    }

    fun updateState(transform: (AttendanceRegistrationState) -> AttendanceRegistrationState) {
        state = transform(state)
    }

    fun updateAttempt(transform: (AttendanceRegistrationAttemptState) -> AttendanceRegistrationAttemptState) {
        updateState { it.withAttempt(transform) }
    }

    fun updateGeofence(transform: (AttendanceRegistrationGeofenceState) -> AttendanceRegistrationGeofenceState) {
        updateState { it.withGeofence(transform) }
    }

    fun updateChallenge(transform: (AttendanceRegistrationChallengeState) -> AttendanceRegistrationChallengeState) {
        updateState { it.withChallenge(transform) }
    }

    fun updateUiState(transform: (AttendanceRegistrationUiState) -> AttendanceRegistrationUiState) {
        updateState { it.withUi(transform) }
    }

    fun saveInstanceState(outState: Bundle) {
        val target = state.attempt.activeTarget ?: return
        outState.putString(STATE_TARGET_KIND, target.kind)
        outState.putLong(STATE_TARGET_EVENT_ID, target.eventId)
        outState.putString(STATE_TARGET_EVENT_DATE, target.eventDate)
        outState.putString(STATE_TARGET_JOIN_TOKEN, target.joinToken)
        outState.putString(STATE_ATTENDANCE_ATTEMPT_TOKEN, state.attempt.attendanceAttemptToken)
    }

    fun restoreInstanceState(savedInstanceState: Bundle?): Boolean {
        if (savedInstanceState == null) return false
        val kind = savedInstanceState.getString(STATE_TARGET_KIND).orEmpty()
        val eventId = savedInstanceState.getLong(STATE_TARGET_EVENT_ID, 0L)
        val eventDate = savedInstanceState.getString(STATE_TARGET_EVENT_DATE).orEmpty()
        val joinToken = savedInstanceState.getString(STATE_TARGET_JOIN_TOKEN).orEmpty()
        val attendanceAttemptToken = savedInstanceState.getString(STATE_ATTENDANCE_ATTEMPT_TOKEN).orEmpty()
        if (kind.isBlank() || eventId <= 0L || eventDate.isBlank() || joinToken.isBlank()) return false

        state = state.withAttempt {
            it.withActiveTarget(AttendanceTarget(kind, eventId, eventDate, joinToken))
                .withAttendanceAttemptToken(attendanceAttemptToken.ifBlank { null })
                .withStatus(AttendanceRegistrationStatus.ACTIVE)
        }
        return true
    }
}
