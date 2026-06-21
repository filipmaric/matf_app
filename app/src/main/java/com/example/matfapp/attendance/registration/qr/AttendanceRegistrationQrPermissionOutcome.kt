package com.example.matfapp.attendance.registration

sealed class AttendanceRegistrationQrPermissionOutcome {
    data object NotHandled : AttendanceRegistrationQrPermissionOutcome()
    data object Granted : AttendanceRegistrationQrPermissionOutcome()
    data object Denied : AttendanceRegistrationQrPermissionOutcome()
}
