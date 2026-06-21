package com.example.matfapp.attendance.registration

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.matfapp.R
import com.example.matfapp.attendance.AttendanceLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class AttendanceRegistrationGeofencingController(
    private val activity: AttendanceRegistrationActivity,
    private val stateManager: AttendanceRegistrationStateManager,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationCancellationSource: CancellationTokenSource,
    private val formatter: AttendanceRegistrationGeofenceFormatter,
) {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 4022
    }

    fun configure(
        attendanceGeofenceEnabled: Boolean,
        attendanceLocations: List<AttendanceLocation>,
    ) {
        stateManager.updateGeofence { geofence ->
            geofence
                .withAttendanceGeofenceEnabled(attendanceGeofenceEnabled)
                .withAttendanceLocations(attendanceLocations)
                .withAttendanceLocationsLoaded(true)
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    fun handlePermissionResult(requestCode: Int, grantResults: IntArray): AttendanceRegistrationGeofencePermissionOutcome {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return AttendanceRegistrationGeofencePermissionOutcome.NotHandled
        if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            return AttendanceRegistrationGeofencePermissionOutcome.Granted
        }
        return AttendanceRegistrationGeofencePermissionOutcome.Denied
    }

    fun missingLocationsMessage(): String {
        return formatter.buildMissingLocationsMessage()
    }

    fun verifyLocation(onOutcome: (AttendanceRegistrationGeofenceOutcome) -> Unit) {
        if (!hasLocationPermission()) {
            onOutcome(AttendanceRegistrationGeofenceOutcome.PermissionRequired)
            return
        }

        onOutcome(AttendanceRegistrationGeofenceOutcome.CheckingStarted)

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationCancellationSource.token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    onOutcome(AttendanceRegistrationGeofenceOutcome.Blocked(activity.getString(R.string.attendance_scan_cannot_determine_location)))
                    return@addOnSuccessListener
                }
                val geofence = stateManager.state.geofence
                val ok = geofence.attendanceLocations.any { locationItem ->
                    distanceMeters(
                        location.latitude,
                        location.longitude,
                        locationItem.latitude,
                        locationItem.longitude,
                    ) <= locationItem.radiusMeters
                }
                if (ok) {
                    stateManager.updateGeofence { it.withLocationVerified(true).withCurrentLocation(location.latitude, location.longitude) }
                    onOutcome(AttendanceRegistrationGeofenceOutcome.Allowed(location.latitude, location.longitude))
                } else {
                    stateManager.updateGeofence { it.withLocationVerified(false).withCurrentLocation(location.latitude, location.longitude) }
                    onOutcome(AttendanceRegistrationGeofenceOutcome.Blocked(formatter.buildGeofenceDebugMessage(location.latitude, location.longitude, geofence.attendanceLocations)))
                }
            }
            .addOnFailureListener { error ->
                onOutcome(AttendanceRegistrationGeofenceOutcome.Blocked(error.message ?: activity.getString(R.string.attendance_scan_cannot_determine_location)))
            }
    }

    fun onDestroy() {
        locationCancellationSource.cancel()
    }

}
