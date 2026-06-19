package com.example.suraksha_ai.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suraksha_ai.services.ShakeDetectionService
import com.example.suraksha_ai.services.FakeCallService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeCallScreen() {
    val context = LocalContext.current
    var isCallActive by remember { mutableStateOf(false) }
    var callerName by remember { mutableStateOf("Mom") }
    var callerNumber by remember { mutableStateOf("+1 234 567 8900") }
    
    val fakeCallService = remember { FakeCallService(context) }
    
    // Shake detection
    val shakeService = remember {
        ShakeDetectionService(context) {
            isCallActive = true
            fakeCallService.startRingtone()
        }
    }
    
    LaunchedEffect(Unit) {
        shakeService.start()
    }
    
    LaunchedEffect(isCallActive) {
        if (isCallActive) {
            fakeCallService.startRingtone()
        } else {
            fakeCallService.stopRingtone()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            shakeService.stop()
            fakeCallService.stopRingtone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fake Call", fontWeight = FontWeight.Bold) },
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
                .background(if (isCallActive) Color.Black else MaterialTheme.colorScheme.background)
        ) {
            if (isCallActive) {
                IncomingCallUI(
                    callerName = callerName,
                    callerNumber = callerNumber,
                    onAnswer = { 
                        isCallActive = false
                        fakeCallService.stopRingtone()
                    },
                    onDecline = { 
                        isCallActive = false
                        fakeCallService.stopRingtone()
                    }
                )
            } else {
                FakeCallSetup(
                    callerName = callerName,
                    callerNumber = callerNumber,
                    onCallerNameChange = { callerName = it },
                    onCallerNumberChange = { callerNumber = it },
                    onTriggerCall = { 
                        isCallActive = true
                        fakeCallService.startRingtone()
                    }
                )
            }
        }
    }
}

@Composable
fun IncomingCallUI(
    callerName: String,
    callerNumber: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Caller Avatar
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(200.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Caller",
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Caller Name
        Text(
            text = callerName,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Caller Number
        Text(
            text = callerNumber,
            fontSize = 20.sp,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Incoming call",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Decline Button
            FloatingActionButton(
                onClick = onDecline,
                modifier = Modifier.size(64.dp),
                containerColor = Color(0xFFDC143C)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Decline",
                    tint = Color.White
                )
            }

            // Answer Button
            FloatingActionButton(
                onClick = onAnswer,
                modifier = Modifier.size(64.dp),
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Answer",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FakeCallSetup(
    callerName: String,
    callerNumber: String,
    onCallerNameChange: (String) -> Unit,
    onCallerNumberChange: (String) -> Unit,
    onTriggerCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Configure Fake Call",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetButton("Mom", onClick = {
                onCallerNameChange("Mom")
                onCallerNumberChange("+1 234 567 8900")
            })
            PresetButton("Police", onClick = {
                onCallerNameChange("Police Officer")
                onCallerNumberChange("100")
            })
            PresetButton("Friend", onClick = {
                onCallerNameChange("Best Friend")
                onCallerNumberChange("+1 234 567 8901")
            })
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = callerName,
            onValueChange = onCallerNameChange,
            label = { Text("Caller Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = callerNumber,
            onValueChange = onCallerNumberChange,
            label = { Text("Caller Number") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Phone, null) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onTriggerCall,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Call, "Trigger Call")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Trigger Fake Call", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Trigger Methods:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Shake your device", style = MaterialTheme.typography.bodySmall)
                Text("• Tap the button above", style = MaterialTheme.typography.bodySmall)
                Text("• Voice command: 'Fake call'", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun RowScope.PresetButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f)
    ) {
        Text(label)
    }
}

