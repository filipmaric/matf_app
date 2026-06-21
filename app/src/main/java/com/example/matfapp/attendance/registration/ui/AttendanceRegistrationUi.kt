package com.example.matfapp.attendance.registration

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.matfapp.R

class AttendanceRegistrationUi(
    private val context: Context,
    private val previewView: View,
    private val progressBar: ProgressBar,
    private val statusText: TextView,
    private val messageText: TextView,
    private val eventPanel: LinearLayout,
    private val eventTitle: TextView,
    private val eventDetails: TextView,
    private val challengeChoicesContainer: LinearLayout,
    private val closeButton: Button,
) {
    fun render(
        state: AttendanceRegistrationState,
        onSelected: (Int) -> Unit,
    ) {
        val ui = state.ui
        progressBar.visibility = if (ui.progressBarVisible) View.VISIBLE else View.GONE
        statusText.text = when (ui.mode) {
            AttendanceRegistrationScreenMode.Scanning -> context.getString(R.string.attendance_scan_scanning)
            AttendanceRegistrationScreenMode.RefreshingData -> context.getString(R.string.attendance_scan_loading_data)
            AttendanceRegistrationScreenMode.CheckingLocation -> context.getString(R.string.attendance_scan_checking_location)
            AttendanceRegistrationScreenMode.AwaitingLocationPermission -> context.getString(R.string.attendance_scan_location_permission_required)
            AttendanceRegistrationScreenMode.LoadingChallenge -> context.getString(R.string.attendance_scan_loading_data)
            AttendanceRegistrationScreenMode.ChallengeShown -> context.getString(R.string.attendance_scan_choose_number)
            AttendanceRegistrationScreenMode.Success -> context.getString(R.string.attendance_scan_success)
            AttendanceRegistrationScreenMode.Blocked -> context.getString(R.string.attendance_scan_blocked)
            AttendanceRegistrationScreenMode.Expired -> context.getString(R.string.attendance_scan_expired)
        }
        previewView.visibility = if (ui.previewVisible) View.VISIBLE else View.GONE
        closeButton.visibility = if (ui.closeVisible) View.VISIBLE else View.GONE
        renderMessage(ui)
        renderEvent(state.challenge.event, ui.eventPanelVisible)
        renderChallengeChoices(state.challenge, ui, onSelected)
    }

    private fun renderEvent(event: AttendanceEventInfo?, visible: Boolean) {
        if (!visible || event == null) {
            eventPanel.visibility = View.GONE
            return
        }
        val title = event.courseName ?: event.description ?: context.getString(R.string.app_title)
        val date = event.eventDate ?: event.reservationDate.orEmpty()
        val details = buildList {
            if (date.isNotBlank()) add(context.getString(R.string.attendance_scan_event_date_label, date))
            event.roomName?.takeIf { it.isNotBlank() }?.let {
                add(context.getString(R.string.attendance_scan_event_room_label, it))
            }
            if (event.startSlot > 0 || event.endSlot > 0) {
                add(
                    context.getString(
                        R.string.attendance_scan_event_time_label,
                        "${pad2(event.startSlot)}:00 - ${pad2(event.endSlot)}:00",
                    ),
                )
            }
            event.teacherName?.takeIf { it.isNotBlank() }?.let {
                add(context.getString(R.string.attendance_scan_event_teacher_label, it))
            }
        }.joinToString(" | ")

        eventTitle.text = title
        eventDetails.text = details
        eventPanel.visibility = View.VISIBLE
    }

    private fun renderChallengeChoices(
        challengeState: AttendanceRegistrationChallengeState,
        ui: AttendanceRegistrationUiState,
        onSelected: (Int) -> Unit,
    ) {
        challengeChoicesContainer.removeAllViews()
        if (!ui.challengeChoicesVisible || challengeState.challenge == null) {
            challengeChoicesContainer.visibility = View.GONE
            return
        }

        ui.challengeChoicesErrorText?.let { errorText ->
            challengeChoicesContainer.visibility = View.VISIBLE
            val errorView = TextView(context).apply {
                text = errorText
                setTextColor(ContextCompat.getColor(context, R.color.error_text))
                textSize = 15f
            }
            challengeChoicesContainer.addView(errorView)
            return
        }

        challengeChoicesContainer.visibility = View.VISIBLE
        challengeState.challenge.challengeChoices.forEach { choice ->
            val button = Button(context).apply {
                text = choice.toString()
                isAllCaps = false
                isEnabled = ui.buttonsEnabled && challengeState.buttonsEnabled
                setOnClickListener { onSelected(choice) }
            }
            challengeChoicesContainer.addView(button)
        }
    }

    private fun renderMessage(ui: AttendanceRegistrationUiState) {
        val message = ui.message
        if (message.isNullOrBlank()) {
            messageText.visibility = View.GONE
            messageText.text = ""
            return
        }

        messageText.visibility = View.VISIBLE
        messageText.text = message
        if (ui.mode == AttendanceRegistrationScreenMode.Success) {
            messageText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            messageText.textSize = 18f
            messageText.setTypeface(messageText.typeface, Typeface.BOLD)
            messageText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_light))
        } else {
            messageText.setTextColor(ContextCompat.getColor(context, R.color.error_text))
            messageText.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            messageText.textSize = 15f
            messageText.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun pad2(value: Int): String {
        return String.format("%02d", value)
    }
}
