package com.example.suraksha_ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.suraksha_ai.services.MapsDirectionsService
import com.example.suraksha_ai.services.SafePoint
import com.example.suraksha_ai.viewmodel.MapViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel
) {
    val mapState by viewModel.mapState.collectAsState()
    val defaultLocation = LatLng(28.6139, 77.2090) // Default to Delhi
    val context = LocalContext.current
    val mapsDirectionsService = remember { MapsDirectionsService(context) }
    val scope = rememberCoroutineScope()
    var nearestSafePoint by remember { mutableStateOf<SafePoint?>(null) }

    LaunchedEffect(Unit) {
        viewModel.updateCurrentLocation()
    }

    LaunchedEffect(mapState.currentLocation) {
        mapState.currentLocation?.let { currentLoc ->
            // Find nearest safe point
            val allSafePoints = buildList {
                mapState.policeStations.forEach { station ->
                    add(SafePoint(station.id, station.name, station.location, "police", station.phone))
                }
                mapState.hospitals.forEach { hospital ->
                    add(SafePoint(hospital.id, hospital.name, hospital.location, "hospital", hospital.phone))
                }
                mapState.safeZones.forEach { zone ->
                    add(SafePoint(zone.id, zone.name, zone.location, zone.type, null))
                }
            }
            scope.launch {
                nearestSafePoint = mapsDirectionsService.findNearestSafePoint(currentLoc, allSafePoints)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safe Zones & Map", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { viewModel.updateCurrentLocation() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, "My Location")
                }
                nearestSafePoint?.let { safePoint ->
                    FloatingActionButton(
                        onClick = {
                            mapState.currentLocation?.let { currentLoc ->
                                mapsDirectionsService.openGoogleMapsNavigation(currentLoc, safePoint.location)
                            }
                        },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(Icons.Default.Navigation, "Navigate to Nearest Safe Point")
                    }
                }
                FloatingActionButton(
                    onClick = { viewModel.requestPatrolCheck() },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Security, "Request Patrol")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Google Map
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    mapState.currentLocation ?: defaultLocation,
                    14f
                )
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Current Location Marker
                mapState.currentLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Your Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }

                // Highlight nearest safe point
                nearestSafePoint?.let { safePoint ->
                    Marker(
                        state = MarkerState(position = safePoint.location),
                        title = "Nearest: ${safePoint.name}",
                        snippet = "Tap to navigate",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            when (safePoint.type) {
                                "police" -> BitmapDescriptorFactory.HUE_BLUE
                                "hospital" -> BitmapDescriptorFactory.HUE_RED
                                else -> BitmapDescriptorFactory.HUE_GREEN
                            }
                        )
                    )
                }

                // Police Stations
                mapState.policeStations.forEach { station ->
                    if (nearestSafePoint?.id != station.id) {
                        Marker(
                            state = MarkerState(position = station.location),
                            title = station.name,
                            snippet = "Phone: ${station.phone}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                        )
                    }
                }

                // Hospitals
                mapState.hospitals.forEach { hospital ->
                    if (nearestSafePoint?.id != hospital.id) {
                        Marker(
                            state = MarkerState(position = hospital.location),
                            title = hospital.name,
                            snippet = "Phone: ${hospital.phone}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                // Safe Zones
                mapState.safeZones.forEach { zone ->
                    if (nearestSafePoint?.id != zone.id) {
                        Marker(
                            state = MarkerState(position = zone.location),
                            title = zone.name,
                            snippet = zone.type,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                        )
                    }
                }

                // Crime Heatmap (circles)
                mapState.crimeHeatmap.forEach { point ->
                    Circle(
                        center = point.location,
                        radius = 500.0,
                        fillColor = Color.Red.copy(alpha = point.intensity * 0.3f),
                        strokeColor = Color.Red.copy(alpha = point.intensity),
                        strokeWidth = 2f
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
                        text = "Nearby Safe Zones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    nearestSafePoint?.let { safePoint ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Navigation, "Nearest", tint = Color(0xFF4CAF50))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Nearest: ${safePoint.name}",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = safePoint.type,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        mapState.currentLocation?.let { currentLoc ->
                                            mapsDirectionsService.openGoogleMapsNavigation(currentLoc, safePoint.location)
                                        }
                                    }
                                ) {
                                    Text("Navigate")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalPolice, "Police", tint = Color.Blue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${mapState.policeStations.size} Police Stations")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, "Hospital", tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${mapState.hospitals.size} Hospitals")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, "Safe Zone", tint = Color.Green)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${mapState.safeZones.size} Safe Zones")
                    }
                }
            }
        }
    }
}

