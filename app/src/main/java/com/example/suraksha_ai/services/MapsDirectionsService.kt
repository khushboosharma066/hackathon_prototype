package com.example.suraksha_ai.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapsDirectionsService(private val context: Context) {

    suspend fun findNearestSafePoint(
        currentLocation: LatLng,
        safePoints: List<SafePoint>
    ): SafePoint? = withContext(Dispatchers.Default) {
        if (safePoints.isEmpty()) return@withContext null

        var nearest: SafePoint? = null
        var minDistance = Double.MAX_VALUE

        safePoints.forEach { point ->
            val distance = calculateDistance(currentLocation, point.location)
            if (distance < minDistance) {
                minDistance = distance
                nearest = point
            }
        }

        nearest
    }

    fun openGoogleMapsNavigation(from: LatLng, to: LatLng) {
        val uri = Uri.parse("google.navigation:q=${to.latitude},${to.longitude}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to web maps
            val webUri = Uri.parse("https://www.google.com/maps/dir/${from.latitude},${from.longitude}/${to.latitude},${to.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            point1.latitude, point1.longitude,
            point2.latitude, point2.longitude,
            results
        )
        return results[0].toDouble()
    }
}

data class SafePoint(
    val id: String,
    val name: String,
    val location: LatLng,
    val type: String, // "police", "hospital", "safe_zone"
    val phone: String? = null
)


