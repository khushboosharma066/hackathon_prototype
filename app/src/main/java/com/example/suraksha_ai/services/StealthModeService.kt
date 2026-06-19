package com.example.suraksha_ai.services

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class StealthModeService(private val context: Context) {
    private var onStealthTrigger: (() -> Unit)? = null

    fun setStealthTrigger(callback: () -> Unit) {
        onStealthTrigger = callback
    }

    fun triggerStealthMode() {
        onStealthTrigger?.invoke()
    }
}

@Composable
fun StealthModeHandler(
    onStealthTrigger: () -> Unit,
    onSOSTrigger: () -> Unit
) {
    val view = LocalView.current
    val gestureDetector = remember {
        GestureDetector(
            view.context,
            object : GestureDetector.SimpleOnGestureListener() {
                private var lastTapTime = 0L
                private val DOUBLE_TAP_TIMEOUT = 300L

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    onStealthTrigger()
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    onStealthTrigger()
                }

                override fun onDown(e: MotionEvent): Boolean {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < DOUBLE_TAP_TIMEOUT) {
                        onStealthTrigger()
                    }
                    lastTapTime = currentTime
                    return true
                }
            }
        )
    }

    DisposableEffect(Unit) {
        val touchListener = View.OnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
        view.setOnTouchListener(touchListener)
        onDispose {
            view.setOnTouchListener(null)
        }
    }
}


