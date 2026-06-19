package com.example.suraksha_ai.services

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoRecordingService(private val context: Context) {
    private var recording: Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var currentVideoFile: File? = null

    suspend fun startFrontCameraRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                return@withContext Result.failure(Exception("Camera permission not granted"))
            }

            // Create output directory
            val outputDir = File(context.filesDir, "emergency_videos")
            if (!outputDir.exists()) outputDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val videoFile = File(outputDir, "SOS_$timestamp.mp4")
            currentVideoFile = videoFile

            // Get camera provider
            val cameraProvider = ProcessCameraProvider.getInstance(context).await()
            this@VideoRecordingService.cameraProvider = cameraProvider

            // Create quality selector
            val qualitySelector = QualitySelector.fromOrderedList(
                listOf(Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )

            // Create recorder
            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            // Select front camera
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Bind use cases
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                androidx.lifecycle.LifecycleOwner { },
                cameraSelector,
                videoCapture
            )

            // Start recording
            val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
                context.contentResolver,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
                .setContentValues(android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, "SOS_$timestamp.mp4")
                })
                .build()

            val fileOutputOptions = FileOutputOptions.Builder(videoFile).build()

            recording = recorder.output
                .prepareRecording(context, fileOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            // Recording started
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                // Recording completed successfully
                            }
                        }
                    }
                }

            Result.success(videoFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopRecording() {
        try {
            recording?.stop()
            recording = null
            cameraProvider?.unbindAll()
            cameraProvider = null
            videoCapture = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentVideoFile(): File? = currentVideoFile
}


