package com.example.suraksha_ai.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suraksha_ai.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val sosState by viewModel.sosState.collectAsState()
    val isRecording by viewModel.emergencyService.isRecording.collectAsState()

    // Red flash animation when SOS is active
    val infiniteTransition = rememberInfiniteTransition(label = "flash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (sosState.isActive && sosState.countdown == 0) 0.3f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PanicSafe", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    if (sosState.isActive && sosState.countdown == 0) {
                        Color.Red.copy(alpha = flashAlpha)
                    } else {
                        MaterialTheme.colorScheme.background
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // SOS Button
                SOSButton(
                    isActive = sosState.isActive,
                    countdown = sosState.countdown,
                    onTrigger = { viewModel.triggerSOS() },
                    onCancel = { viewModel.cancelSOS() }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Voice SOS Button
                VoiceSOSButton(
                    isListening = sosState.isVoiceListening,
                    recognizedText = sosState.recognizedText,
                    onStart = { viewModel.startVoiceListening() },
                    onStop = { viewModel.stopVoiceListening() }
                )

                if (isRecording) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Videocam,
                                "Recording",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording Video...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (sosState.emergencySent) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = "✓ Emergency Alert Sent!",
                            modifier = Modifier.padding(16.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SOSButton(
    isActive: Boolean,
    countdown: Int,
    onTrigger: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive && countdown > 0) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        if (isActive && countdown > 0) {
            // Countdown Text
            Text(
                text = countdown.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        } else if (isActive && countdown == 0) {
            // Emergency Activated
            Text(
                text = "EMERGENCY\nACTIVATED!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // SOS Button
        Button(
            onClick = if (isActive) onCancel else onTrigger,
            modifier = Modifier
                .size(200.dp)
                .scale(scale),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActive) Color(0xFFDC143C) else Color(0xFFDC143C)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            if (!isActive || countdown == 0) {
                Text(
                    text = if (isActive) "CANCEL" else "SOS",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun VoiceSOSButton(
    isListening: Boolean,
    recognizedText: String,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(
            onClick = if (isListening) onStop else onStart,
            modifier = Modifier
                .width(200.dp)
                .height(60.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isListening) Color(0xFFDC143C) else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Voice SOS",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isListening) "Listening..." else "Voice SOS",
                fontWeight = FontWeight.Medium
            )
        }
        if (isListening && recognizedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$recognizedText\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

