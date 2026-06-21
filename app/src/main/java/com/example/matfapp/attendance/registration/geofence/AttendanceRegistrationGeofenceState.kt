package com.example.matfapp.attendance.registration

import com.example.matfapp.attendance.AttendanceLocation

data class AttendanceRegistrationGeofenceState(
    val attendanceLocations: List<AttendanceLocation> = emptyList(),
    val attendanceGeofenceEnabled: Boolean = false,
    val attendanceLocationsLoaded: Boolean = false,
    val locationVerified: Boolean = false,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
)

fun AttendanceRegistrationGeofenceState.withAttendanceLocations(attendanceLocations: List<AttendanceLocation>): AttendanceRegistrationGeofenceState {
    return copy(attendanceLocations = attendanceLocations)
}

fun AttendanceRegistrationGeofenceState.withAttendanceGeofenceEnabled(attendanceGeofenceEnabled: Boolean): AttendanceRegistrationGeofenceState {
    return copy(attendanceGeofenceEnabled = attendanceGeofenceEnabled)
}

fun AttendanceRegistrationGeofenceState.withAttendanceLocationsLoaded(attendanceLocationsLoaded: Boolean): AttendanceRegistrationGeofenceState {
    return copy(attendanceLocationsLoaded = attendanceLocationsLoaded)
}

fun AttendanceRegistrationGeofenceState.withLocationVerified(locationVerified: Boolean): AttendanceRegistrationGeofenceState {
    return copy(locationVerified = locationVerified)
}

fun AttendanceRegistrationGeofenceState.withCurrentLocation(latitude: Double?, longitude: Double?): AttendanceRegistrationGeofenceState {
    return copy(currentLatitude = latitude, currentLongitude = longitude)
}
