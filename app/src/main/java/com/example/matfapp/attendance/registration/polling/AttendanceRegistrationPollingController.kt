package com.example.matfapp.attendance.registration

import android.os.Handler

class AttendanceRegistrationPollingController(
    private val uiHandler: Handler,
) {
    private var pollRunnable: Runnable? = null

    fun schedule(delayMs: Long, action: () -> Unit) {
        stop()
        pollRunnable = object : Runnable {
            override fun run() {
                pollRunnable = null
                action()
            }
        }
        uiHandler.postDelayed(pollRunnable!!, delayMs)
    }

    fun stop() {
        pollRunnable?.let {
            uiHandler.removeCallbacks(it)
        }
        pollRunnable = null
    }
}
