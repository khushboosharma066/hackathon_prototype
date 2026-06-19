package com.example.suraksha_ai.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoiceRecognitionService(private val context: Context) {
    private val speechRecognizer: SpeechRecognizer? = SpeechRecognizer.createSpeechRecognizer(context)
    
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()
    
    private val sosKeywords = listOf(
        "help me", "save me", "emergency", "sos", "help", "danger",
        "rescue", "assist", "panic", "threat"
    )
    
    fun startListening(onSOSDetected: () -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }
            
            override fun onBeginningOfSpeech() {}
            
            override fun onRmsChanged(rmsdB: Float) {}
            
            override fun onBufferReceived(buffer: ByteArray?) {}
            
            override fun onEndOfSpeech() {
                _isListening.value = false
            }
            
            override fun onError(error: Int) {
                _isListening.value = false
            }
            
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                _recognizedText.value = text
                
                // Check for SOS keywords
                if (sosKeywords.any { keyword -> text.contains(keyword) }) {
                    onSOSDetected()
                }
                
                _isListening.value = false
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.lowercase() ?: ""
                _recognizedText.value = text
                
                if (sosKeywords.any { keyword -> text.contains(keyword) }) {
                    onSOSDetected()
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        
        speechRecognizer?.setRecognitionListener(listener)
        speechRecognizer?.startListening(intent)
    }
    
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    fun destroy() {
        speechRecognizer?.destroy()
    }
}

