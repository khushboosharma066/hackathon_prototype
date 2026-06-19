package com.example.suraksha_ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suraksha_ai.services.LocationService
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapState(
    val currentLocation: LatLng? = null,
    val safeZones: List<SafeZone> = emptyList(),
    val policeStations: List<PoliceStation> = emptyList(),
    val hospitals: List<Hospital> = emptyList(),
    val crimeHeatmap: List<CrimePoint> = emptyList()
)

data class SafeZone(
    val id: String,
    val name: String,
    val location: LatLng,
    val type: String
)

data class PoliceStation(
    val id: String,
    val name: String,
    val location: LatLng,
    val phone: String
)

data class Hospital(
    val id: String,
    val name: String,
    val location: LatLng,
    val phone: String
)

data class CrimePoint(
    val location: LatLng,
    val intensity: Float // 0.0 - 1.0
)

class MapViewModel(
    private val locationService: LocationService
) : ViewModel() {

    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    init {
        loadSafeZones()
        loadPoliceStations()
        loadHospitals()
        loadCrimeHeatmap()
    }

    fun updateCurrentLocation() {
        viewModelScope.launch {
            val location = locationService.getCurrentLocation()
            location?.let {
                _mapState.value = _mapState.value.copy(
                    currentLocation = LatLng(it.latitude, it.longitude)
                )
            }
        }
    }

    private fun loadSafeZones() {
        viewModelScope.launch {
            val currentLoc = _mapState.value.currentLocation
            if (currentLoc != null) {
                val apiService = com.example.suraksha_ai.services.ApiService()
                val result = apiService.getSafeZones(currentLoc.latitude, currentLoc.longitude)
                result.getOrNull()?.let { zones ->
                    _mapState.value = _mapState.value.copy(
                        safeZones = zones.map { zone ->
                            SafeZone(zone.id, zone.name, LatLng(zone.latitude, zone.longitude), zone.type)
                        }
                    )
                } ?: run {
                    // Fallback to mock data
                    _mapState.value = _mapState.value.copy(
                        safeZones = listOf(
                            SafeZone("1", "Community Center", LatLng(28.6139, 77.2090), "Public"),
                            SafeZone("2", "Shopping Mall", LatLng(28.5355, 77.3910), "Commercial"),
                        )
                    )
                }
            }
        }
    }

    private fun loadPoliceStations() {
        // Mock data
        _mapState.value = _mapState.value.copy(
            policeStations = listOf(
                PoliceStation("1", "Central Police Station", LatLng(28.7041, 77.1025), "100"),
                PoliceStation("2", "North Police Station", LatLng(28.7041, 77.1025), "100"),
            )
        )
    }

    private fun loadHospitals() {
        // Mock data
        _mapState.value = _mapState.value.copy(
            hospitals = listOf(
                Hospital("1", "City Hospital", LatLng(28.6139, 77.2090), "102"),
                Hospital("2", "Emergency Medical Center", LatLng(28.5355, 77.3910), "102"),
            )
        )
    }

    private fun loadCrimeHeatmap() {
        // Mock crime data
        _mapState.value = _mapState.value.copy(
            crimeHeatmap = listOf(
                CrimePoint(LatLng(28.6139, 77.2090), 0.7f),
                CrimePoint(LatLng(28.5355, 77.3910), 0.5f),
            )
        )
    }

    fun requestPatrolCheck() {
        viewModelScope.launch {
            val currentLoc = _mapState.value.currentLocation
            if (currentLoc != null) {
                val apiService = com.example.suraksha_ai.services.ApiService()
                val request = com.example.suraksha_ai.services.PatrolRequest(
                    latitude = currentLoc.latitude,
                    longitude = currentLoc.longitude,
                    details = "User requested patrol check"
                )
                apiService.requestPatrol(request)
            }
        }
    }

    fun findNearestSafePoint(): com.example.suraksha_ai.services.SafePoint? {
        val currentLoc = _mapState.value.currentLocation ?: return null
        val allSafePoints = buildList {
            _mapState.value.policeStations.forEach { station ->
                add(com.example.suraksha_ai.services.SafePoint(station.id, station.name, station.location, "police", station.phone))
            }
            _mapState.value.hospitals.forEach { hospital ->
                add(com.example.suraksha_ai.services.SafePoint(hospital.id, hospital.name, hospital.location, "hospital", hospital.phone))
            }
            _mapState.value.safeZones.forEach { zone ->
                add(com.example.suraksha_ai.services.SafePoint(zone.id, zone.name, zone.location, zone.type, null))
            }
        }
        // Find nearest logic would be in MapsDirectionsService
        return allSafePoints.firstOrNull()
    }
}

