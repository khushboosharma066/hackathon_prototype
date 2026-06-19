package com.example.suraksha_ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.suraksha_ai.data.DatabaseModule
import com.example.suraksha_ai.services.*
import com.example.suraksha_ai.ui.screens.*
import com.example.suraksha_ai.ui.theme.Suraksha_aiTheme
import com.example.suraksha_ai.viewmodel.*
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Suraksha_aiTheme {
                PanicSafeApp()
            }
        }
    }
}

@Composable
fun PanicSafeApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val database = DatabaseModule.provideDatabase(context)
    val emergencyService = EmergencyService(context)
    val locationService = LocationService(context)
    val guardianService = GuardianService(database.guardianDao())
    val aiDangerService = AIDangerService()
    val voiceService = try {
        VoiceRecognitionService(context)
    } catch (e: Exception) {
        null
    }
    val audioService = AudioRecordingService(context)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navItems = listOf(
                    NavItem("home", "Home", Icons.Default.Home),
                    NavItem("map", "Map", Icons.Default.Place),
                    NavItem("guardians", "Guardians", Icons.Default.Person),
                    NavItem("fake_call", "Fake Call", Icons.Default.Phone),
                    NavItem("settings", "Settings", Icons.Default.Settings)
                )

                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel {
                        HomeViewModel(
                            emergencyService,
                            locationService,
                            guardianService,
                            voiceService,
                            audioService
                        )
                    }
                )
            }
            composable("camera") {
                AICameraScreen(
                    viewModel = viewModel { AICameraViewModel(aiDangerService) }
                )
            }
            composable("map") {
                MapScreen(
                    viewModel = viewModel { MapViewModel(locationService) }
                )
            }
            composable("guardians") {
                GuardianScreen(
                    viewModel = viewModel { GuardianViewModel(database.guardianDao()) }
                )
            }
            composable("settings") {
                SettingsScreen()
            }
            composable("fake_call") {
                FakeCallScreen()
            }
            composable("routine_anomaly") {
                RoutineAnomalyScreen()
            }
            composable("police_patrol") {
                PolicePatrolScreen()
    }
        }
    }
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)