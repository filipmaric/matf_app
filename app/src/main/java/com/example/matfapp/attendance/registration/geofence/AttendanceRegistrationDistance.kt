package com.example.matfapp.attendance.registration

fun distanceMeters(
    latitude1: Double,
    longitude1: Double,
    latitude2: Double,
    longitude2: Double,
): Double {
    val earthRadiusMeters = 6_371_000.0
    val lat1 = Math.toRadians(latitude1)
    val lon1 = Math.toRadians(longitude1)
    val lat2 = Math.toRadians(latitude2)
    val lon2 = Math.toRadians(longitude2)
    val deltaLat = lat2 - lat1
    val deltaLon = lon2 - lon1
    val a = kotlin.math.sin(deltaLat / 2.0) * kotlin.math.sin(deltaLat / 2.0) +
        kotlin.math.cos(lat1) * kotlin.math.cos(lat2) *
        kotlin.math.sin(deltaLon / 2.0) * kotlin.math.sin(deltaLon / 2.0)
    return 2.0 * earthRadiusMeters * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1.0 - a))
}
