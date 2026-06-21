package com.example.matfapp.attendance.registration

import android.content.Context
import com.example.matfapp.R
import com.example.matfapp.attendance.AttendanceLocation

class AttendanceRegistrationGeofenceFormatter(
    private val context: Context,
) {
    fun buildMissingLocationsMessage(): String {
        return context.getString(R.string.attendance_scan_course_location_missing)
    }

    fun buildGeofenceDebugMessage(
        currentLatitude: Double,
        currentLongitude: Double,
        locations: List<AttendanceLocation>,
    ): String {
        val closest = locations.minByOrNull {
            distanceMeters(currentLatitude, currentLongitude, it.latitude, it.longitude)
        }
        val current = context.getString(
            R.string.attendance_scan_geofence_current_location,
            formatCoordinate(currentLatitude),
            formatCoordinate(currentLongitude),
        )
        val closestText = if (closest == null) {
            context.getString(R.string.attendance_scan_geofence_unknown)
        } else {
            buildString {
                append(context.getString(R.string.attendance_scan_geofence_nearest_location, closest.name))
                append(" (${formatCoordinate(closest.latitude)}, ${formatCoordinate(closest.longitude)})")
                append(context.getString(R.string.attendance_scan_geofence_distance, formatDistanceMeters(distanceMeters(
                    currentLatitude,
                    currentLongitude,
                    closest.latitude,
                    closest.longitude,
                ))))
                append(context.getString(R.string.attendance_scan_geofence_radius, formatDistanceMeters(closest.radiusMeters)))
            }
        }
        return buildString {
            append(context.getString(R.string.attendance_scan_geofence_block_title))
            append('\n')
            append(current)
            append('\n')
            append(closestText)
            append('\n')
            append(context.getString(R.string.attendance_scan_geofence_retry))
        }
    }

    private fun formatCoordinate(value: Double): String {
        return String.format(java.util.Locale.US, "%.6f", value)
    }

    private fun formatDistanceMeters(value: Double): String {
        return String.format(java.util.Locale.US, "%.1f", value)
    }
}
