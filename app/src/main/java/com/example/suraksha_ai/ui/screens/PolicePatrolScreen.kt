package com.example.suraksha_ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicePatrolScreen() {
    val defaultLocation = LatLng(28.6139, 77.2090)
    
    // Mock patrol zones
    val patrolZones = remember {
        listOf(
            LatLng(28.6139, 77.2090),
            LatLng(28.5355, 77.3910),
            LatLng(28.7041, 77.1025),
        )
    }

    // Mock crime heatmap data
    val crimePoints = remember {
        listOf(
            LatLng(28.6139, 77.2090) to 0.8f,
            LatLng(28.5355, 77.3910) to 0.6f,
            LatLng(28.7041, 77.1025) to 0.4f,
        )
    }

    var showRequestDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Police Patrolling Map", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRequestDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Security, "Request Patrol")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Patrol Zone Markers
                patrolZones.forEach { zone ->
                    Marker(
                        state = MarkerState(position = zone),
                        title = "Active Patrol Zone"
                    )
                }

                // Crime Heatmap
                crimePoints.forEach { (location, intensity) ->
                    Circle(
                        center = location,
                        radius = 800.0,
                        fillColor = Color.Red.copy(alpha = intensity * 0.3f),
                        strokeColor = Color.Red.copy(alpha = intensity),
                        strokeWidth = 3f
                    )
                }
            }

            // Info Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Patrol Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, "Patrol", tint = Color.Blue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${patrolZones.size} Active Patrol Zones")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, "Crime", tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${crimePoints.size} High-Risk Areas")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showRequestDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Security, "Request")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Request Patrol Check")
                    }
                }
            }
        }
    }

    if (showRequestDialog) {
        RequestPatrolDialog(
            onDismiss = { showRequestDialog = false },
            onRequest = {
                // Handle patrol request
                showRequestDialog = false
            }
        )
    }
}

@Composable
fun RequestPatrolDialog(
    onDismiss: () -> Unit,
    onRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Police Patrol") },
        text = {
            Column {
                Text("Request a police patrol check in your area. Your location will be shared with local authorities.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Additional Details (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onRequest) {
                Text("Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

