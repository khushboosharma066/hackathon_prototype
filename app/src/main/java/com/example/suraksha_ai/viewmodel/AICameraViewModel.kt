package com.example.suraksha_ai.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suraksha_ai.services.AIDangerService
import com.example.suraksha_ai.services.DangerDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraState(
    val dangerScore: Double = 0.0,
    val dangerDetails: DangerDetails? = null,
    val isThreatDetected: Boolean = false,
    val isRecording: Boolean = false
)

class AICameraViewModel(
    private val aiDangerService: AIDangerService
) : ViewModel() {

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    fun analyzeFrame(bitmap: Bitmap?) {
        viewModelScope.launch {
            val dangerScore = aiDangerService.analyzeFrame(bitmap)
            val dangerDetails = aiDangerService.getDangerDetails(dangerScore)

            val isThreat = dangerScore > 0.6

            _cameraState.value = CameraState(
                dangerScore = dangerScore,
                dangerDetails = dangerDetails,
                isThreatDetected = isThreat,
                isRecording = isThreat && _cameraState.value.isRecording
            )
        }
    }

    fun startRecording() {
        _cameraState.value = _cameraState.value.copy(isRecording = true)
    }

    fun stopRecording() {
        _cameraState.value = _cameraState.value.copy(isRecording = false)
    }

    fun dismissThreat() {
        _cameraState.value = _cameraState.value.copy(isThreatDetected = false)
    }
}

