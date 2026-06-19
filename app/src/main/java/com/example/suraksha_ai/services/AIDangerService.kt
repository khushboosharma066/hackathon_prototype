package com.example.suraksha_ai.services

import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.math.abs

class AIDangerService {
    private var frameCount = 0
    private var lastDangerScore = 0.0
    private val dangerHistory = mutableListOf<Double>()
    
    // Simulate realistic danger patterns for demo
    suspend fun analyzeFrame(bitmap: Bitmap?): Double {
        delay(80) // Simulate processing time
        
        frameCount++
        
        // Create more realistic danger patterns
        val random = Random(System.currentTimeMillis())
        
        // Base danger level (usually low)
        var dangerScore = random.nextDouble() * 0.25
        
        // Simulate occasional spikes (like someone approaching)
        if (frameCount % 30 == 0 && random.nextDouble() < 0.15) {
            // Simulate threat approaching
            dangerScore = 0.5 + (random.nextDouble() * 0.3)
        }
        
        // Simulate sustained threat (following behavior)
        if (dangerHistory.isNotEmpty() && dangerHistory.last() > 0.5) {
            dangerScore = (dangerHistory.last() * 0.8) + (random.nextDouble() * 0.2)
        }
        
        // Add some continuity (danger doesn't jump randomly)
        if (dangerHistory.isNotEmpty()) {
            val lastScore = dangerHistory.last()
            dangerScore = (lastScore * 0.7) + (dangerScore * 0.3)
        }
        
        // Keep history (last 10 frames)
        dangerHistory.add(dangerScore)
        if (dangerHistory.size > 10) {
            dangerHistory.removeAt(0)
        }
        
        lastDangerScore = dangerScore
        return dangerScore.coerceIn(0.0, 1.0)
    }

    fun getDangerDetails(dangerScore: Double): DangerDetails {
        return when {
            dangerScore < 0.3 -> DangerDetails(
                level = "Safe",
                color = 0xFF4CAF50,
                threats = emptyList()
            )
            dangerScore < 0.6 -> DangerDetails(
                level = "Caution",
                color = 0xFFFF9800,
                threats = listOf("Person detected nearby")
            )
            else -> DangerDetails(
                level = "Danger",
                color = 0xFFDC143C,
                threats = listOf(
                    "Stranger too close",
                    "Possible following detected",
                    "Aggressive behavior detected"
                )
            )
        }
    }
}

data class DangerDetails(
    val level: String,
    val color: Long,
    val threats: List<String>
)

