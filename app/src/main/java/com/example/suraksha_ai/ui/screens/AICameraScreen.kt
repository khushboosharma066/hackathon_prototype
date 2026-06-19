package com.example.suraksha_ai.ui.screens

import android.Manifest
import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.suraksha_ai.viewmodel.AICameraViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AICameraScreen(
    viewModel: AICameraViewModel
) {
    val cameraState by viewModel.cameraState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (permissionsState.allPermissionsGranted) {
            cameraProvider = ProcessCameraProvider.getInstance(context).get()
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Danger Camera", fontWeight = FontWeight.Bold) },
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
                .background(Color.Black)
        ) {
            if (permissionsState.allPermissionsGranted && cameraProvider != null) {
                CameraPreview(
                    cameraProvider = cameraProvider!!,
                    lifecycleOwner = lifecycleOwner,
                    onImageCaptured = { bitmap ->
                        viewModel.analyzeFrame(bitmap)
                    }
                )
            } else {
                // Permission request UI
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Camera permission required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }

            // Danger Level Indicator
            DangerLevelIndicator(
                dangerScore = cameraState.dangerScore,
                dangerDetails = cameraState.dangerDetails,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )

            // Threat Warning Card
            if (cameraState.isThreatDetected) {
                ThreatWarningCard(
                    dangerDetails = cameraState.dangerDetails,
                    onDismiss = { viewModel.dismissThreat() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onImageCaptured: (android.graphics.Bitmap?) -> Unit
) {
    val preview = Preview.Builder().build()
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    val imageAnalyzer = ImageAnalysis.Analyzer { imageProxy ->
        // Convert ImageProxy to Bitmap (simplified)
        // In production, use proper image conversion
        onImageCaptured(null)
        imageProxy.close()
    }

    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(LocalContext.current), imageAnalyzer)

    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    LaunchedEffect(cameraProvider) {
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        } catch (e: Exception) {
            // Handle error
        }
    }

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context)
            preview.setSurfaceProvider(previewView.surfaceProvider)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun DangerLevelIndicator(
    dangerScore: Double,
    dangerDetails: com.example.suraksha_ai.services.DangerDetails?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Danger Level",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { dangerScore.toFloat() },
                modifier = Modifier.width(200.dp),
                color = Color(dangerDetails?.color ?: 0xFF4CAF50),
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(dangerScore * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            dangerDetails?.let {
                Text(
                    text = it.level,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(it.color)
                )
            }
        }
    }
}

@Composable
fun ThreatWarningCard(
    dangerDetails: com.example.suraksha_ai.services.DangerDetails?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDC143C)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "THREAT DETECTED",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            dangerDetails?.threats?.forEach { threat ->
                Text(
                    text = "• $threat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

