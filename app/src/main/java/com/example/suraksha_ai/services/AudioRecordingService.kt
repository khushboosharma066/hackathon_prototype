package com.example.suraksha_ai.services

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class AudioRecordingService(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isRecording = false
    
    suspend fun startRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val outputDir = File(context.getExternalFilesDir(null), "recordings")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            outputFile = File(outputDir, "emergency_${System.currentTimeMillis()}.m4a")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                
                prepare()
                start()
                isRecording = true
            }
            
            Result.success(outputFile?.absolutePath ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                if (isRecording) {
                    stop()
                    release()
                }
            }
            mediaRecorder = null
            isRecording = false
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    fun isRecording(): Boolean = isRecording
}

