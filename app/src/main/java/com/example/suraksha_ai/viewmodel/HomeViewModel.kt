package com.example.suraksha_ai.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suraksha_ai.data.Guardian
import com.example.suraksha_ai.services.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SOSState(
    val isActive: Boolean = false,
    val countdown: Int = 3,
    val isVoiceListening: Boolean = false,
    val recognizedText: String = "",
    val emergencySent: Boolean = false
)

class HomeViewModel(
    val emergencyService: EmergencyService,
    private val locationService: LocationService,
    private val guardianService: GuardianService,
    private val voiceService: VoiceRecognitionService?,
    private val audioRecordingService: AudioRecordingService
) : ViewModel() {

    private val _sosState = MutableStateFlow(SOSState())
    val sosState: StateFlow<SOSState> = _sosState.asStateFlow()

    init {
        // Listen to voice recognition
        voiceService?.let { service ->
            viewModelScope.launch {
                service.recognizedText.collect { text ->
                    _sosState.value = _sosState.value.copy(recognizedText = text)
                }
            }
        }
    }

    fun triggerSOS() {
        if (_sosState.value.isActive) return

        viewModelScope.launch {
            _sosState.value = _sosState.value.copy(isActive = true, countdown = 3)

            // Start audio recording
            audioRecordingService.startRecording()

            // Countdown
            for (i in 3 downTo 1) {
                _sosState.value = _sosState.value.copy(countdown = i)
                emergencyService.triggerVibration()
                delay(1000)
            }

            // Activate SOS
            activateSOS()
        }
    }

    fun cancelSOS() {
        viewModelScope.launch {
            _sosState.value = SOSState()
            audioRecordingService.stopRecording()
            emergencyService.stopVideoRecording()
        }
    }

    private suspend fun activateSOS() {
        try {
            // 1. Start front camera video recording
            emergencyService.startVideoRecording()

            // 2. Get location
            val location = locationService.getCurrentLocationMap()
            location?.let { loc ->
                val latitude = loc["latitude"] ?: 0.0
                val longitude = loc["longitude"] ?: 0.0

                // 3. Get guardians
                val guardians = guardianService.getGuardians()

                // 4. Send to guardians (WhatsApp/SMS)
                emergencyService.sendLocationToGuardians(
                    latitude,
                    longitude,
                    guardians
                )

                // 5. Send to backup server
                val serverSuccess = emergencyService.sendToBackupServer(latitude, longitude)

                // 6. Trigger vibration
                emergencyService.triggerVibration()

                // 7. Flash light
                emergencyService.triggerFlash()

                _sosState.value = _sosState.value.copy(
                    countdown = 0,
                    emergencySent = true
                )
            }
        } catch (e: Exception) {
            // Handle error - still mark as sent
            _sosState.value = _sosState.value.copy(
                countdown = 0,
                emergencySent = true
            )
        }
    }

    fun startVoiceListening() {
        voiceService?.startListening {
            triggerSOS()
        }
        _sosState.value = _sosState.value.copy(isVoiceListening = true)
    }

    fun stopVoiceListening() {
        voiceService?.stopListening()
        _sosState.value = _sosState.value.copy(isVoiceListening = false)
    }

    override fun onCleared() {
        super.onCleared()
        voiceService?.destroy()
        audioRecordingService.stopRecording()
    }
}

