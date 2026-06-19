package com.example.suraksha_ai.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context
import kotlin.math.sqrt

class ShakeDetectionService(
    private val context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val shakeThreshold = 800f
    
    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    fun stop() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            
            if ((currentTime - lastUpdate) > 100) {
                val timeDiff = currentTime - lastUpdate
                lastUpdate = currentTime
                
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                val speed = sqrt((x - lastX) * (x - lastX) + 
                                (y - lastY) * (y - lastY) + 
                                (z - lastZ) * (z - lastZ)) / timeDiff * 10000
                
                if (speed > shakeThreshold) {
                    onShakeDetected()
                }
                
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

