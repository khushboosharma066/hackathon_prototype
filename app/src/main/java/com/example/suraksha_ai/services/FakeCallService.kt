package com.example.suraksha_ai.services

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat

class FakeCallService(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val vibrator: Vibrator? by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun startRingtone() {
        try {
            // Get default ringtone
            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer.create(context, ringtoneUri)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()

            // Start vibration pattern
            startVibrationPattern()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibrationPattern() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Ring pattern: vibrate 500ms, pause 1000ms, repeat
            val pattern = longArrayOf(0, 500, 1000, 500, 1000, 500)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 1000, 500, 1000, 500), 0)
        }
    }

    fun stopRingtone() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


