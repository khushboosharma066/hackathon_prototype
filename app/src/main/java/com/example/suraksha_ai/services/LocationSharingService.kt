package com.example.suraksha_ai.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.suraksha_ai.data.Guardian
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationSharingService(private val context: Context) {

    suspend fun shareLocationToGuardians(
        location: Location?,
        guardians: List<Guardian>
    ) = withContext(Dispatchers.IO) {
        guardians.forEach { guardian ->
            if (isInternetAvailable()) {
                // Try WhatsApp first
                val whatsAppSent = sendViaWhatsApp(guardian.phone, location)
                if (!whatsAppSent) {
                    // Fallback to SMS
                    sendViaSMS(guardian.phone, location)
                }
            } else {
                // Offline mode - use SMS
                sendViaSMS(guardian.phone, location)
            }
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun sendViaWhatsApp(phoneNumber: String, location: Location?): Boolean {
        return try {
            val message = buildLocationMessage(location)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun sendViaSMS(phoneNumber: String, location: Location?) {
        try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val message = buildLocationMessage(location)
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size == 1) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } else {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildLocationMessage(location: Location?): String {
        return if (location != null) {
            val mapsUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            "🚨 EMERGENCY! I am not safe. This is my location: $mapsUrl"
        } else {
            "🚨 EMERGENCY! I am not safe. Unable to get location. Please call me immediately!"
        }
    }
}

