package com.example.suraksha_ai.services

import android.content.Context
import android.location.Location
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.suraksha_ai.data.Guardian
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EmergencyService(private val context: Context) {
    private val vibrator: Vibrator? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private val videoRecordingService = VideoRecordingService(context)
    private val locationSharingService = LocationSharingService(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    fun triggerVibration() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(1000)
        }
    }

    fun triggerFlash() {
        // Placeholder for flashlight control
        // In production, use Camera2 API or flashlight manager
    }

    suspend fun startVideoRecording(): Result<String> {
        return try {
            val result = videoRecordingService.startFrontCameraRecording()
            if (result.isSuccess) {
                _isRecording.value = true
            }
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopVideoRecording() {
        videoRecordingService.stopRecording()
        _isRecording.value = false
    }

    suspend fun sendLocationToGuardians(
        latitude: Double,
        longitude: Double,
        guardians: List<Guardian>
    ) {
        val location = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        locationSharingService.shareLocationToGuardians(location, guardians)
    }

    suspend fun sendToBackupServer(latitude: Double, longitude: Double): Boolean {
        return try {
            val apiService = ApiService()
            val request = EmergencyRequest(
                latitude = latitude,
                longitude = longitude,
                timestamp = System.currentTimeMillis(),
                dangerLevel = "HIGH"
            )
            
            val result = apiService.sendEmergency(request)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    fun startAudioRecording() {
        // Placeholder for audio recording
        // In production, use MediaRecorder or AudioRecord
    }

    fun stopAudioRecording() {
        // Stop recording
    }
}

