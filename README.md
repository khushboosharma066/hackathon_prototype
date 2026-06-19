# PanicSafe - Women's Safety App

A comprehensive Android mobile app built with **Kotlin + Jetpack Compose** focused on women's safety with AI-powered danger prediction, emergency SOS, and real-time safety features.

## 🎯 Features

### 1. 🔴 Dual SOS Trigger System
- Large red SOS button on home screen with 3-second animated countdown
- Voice activation ("Help me", "Save me")
- Vibration feedback and visual confirmation

### 2. 🚨 Emergency Workflow
- Sends live location to guardian contacts
- SMS fallback when offline
- Background audio/video recording (placeholders)
- Flash + vibration confirmation
- Encrypted evidence storage (placeholder)

### 3. 🤖 AI Danger Prediction Camera
- Real-time camera preview using CameraX
- ML-based danger detection (placeholder logic):
  - Stranger too close detection
  - Following behavior detection
  - Aggressive facial expressions
  - Weapon gesture detection
  - Suspicious group behavior
- Auto-records 10-second video on threat detection
- Visual danger level indicator
- Threat warning cards

### 4. 👨‍👩‍👧 Live Guardian Tracking
- Add/remove guardians stored in Room database
- One-tap "Share My Live Location"
- Real-time location sharing

### 5. 🗺️ Safe Zone Navigator
- Google Maps integration
- Shows nearest:
  - Police stations
  - Hospitals
  - Public safe zones
- Crime heatmap visualization
- Emergency navigation routes

### 6. ☎️ Fake Call System
- Realistic incoming call UI
- Custom caller name (Mom, Police, Friend, etc.)
- Trigger via hidden button, voice keyword, or shake gesture

### 7. 🧠 Routine Anomaly Detection
- Learns daily travel patterns (placeholder)
- Alerts for:
  - Unusual stops in unsafe areas
  - Route deviations
  - Late-night movement

### 8. 🚓 Police Patrolling Map
- Live patrolling zones visualization
- Crime heatmap with intensity levels
- Request patrol check feature

### 9. 🔐 Privacy & Security
- App lock with PIN/fingerprint
- Fake Calculator Mode
- Stealth mode quick access
- Encrypted media storage

## 🛠️ Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModels
- **Database**: Room
- **Camera**: CameraX
- **Maps**: Google Maps Compose
- **Location**: Google Play Services Location
- **Permissions**: Accompanist Permissions

## 📋 Prerequisites

1. **Android Studio** (Hedgehog or later)
2. **Android SDK** (API 24+)
3. **Google Maps API Key** (for map features)

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd suraksha_ai
```

### 2. Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable "Maps SDK for Android"
4. Create credentials (API Key)
5. Restrict the API key to Android apps (optional but recommended)

### 3. Configure API Key

Open `app/src/main/AndroidManifest.xml` and replace:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

### 4. Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Connect an Android device or start an emulator
4. Click "Run" or press `Shift+F10`

## 📱 Permissions Required

The app requires the following permissions:
- **Location** (Fine & Coarse) - For location sharing and maps
- **Camera** - For AI danger detection
- **Microphone** - For audio recording and voice SOS
- **SMS** - For emergency SMS fallback
- **Phone** - For fake call feature
- **Vibration** - For SOS feedback
- **Flashlight** - For emergency alerts
- **Biometric** - For app lock

All permissions are requested at runtime with user-friendly dialogs.

## 🏗️ Project Structure

```
app/src/main/java/com/example/suraksha_ai/
├── data/
│   ├── Guardian.kt              # Room entity
│   ├── GuardianDao.kt           # Database access
│   ├── PanicSafeDatabase.kt    # Room database
│   └── DatabaseModule.kt        # Database provider
├── services/
│   ├── EmergencyService.kt     # SOS and emergency workflow
│   ├── LocationService.kt       # Location management
│   └── AIDangerService.kt       # AI danger detection (placeholder)
├── viewmodel/
│   ├── HomeViewModel.kt         # Home screen state
│   ├── AICameraViewModel.kt     # Camera screen state
│   ├── GuardianViewModel.kt     # Guardian management
│   └── MapViewModel.kt          # Map screen state
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt        # Main SOS screen
│   │   ├── AICameraScreen.kt    # AI danger camera
│   │   ├── MapScreen.kt         # Safe zones map
│   │   ├── GuardianScreen.kt    # Guardian contacts
│   │   ├── SettingsScreen.kt    # Settings & privacy
│   │   ├── FakeCallScreen.kt    # Fake call feature
│   │   ├── RoutineAnomalyScreen.kt  # Routine detection
│   │   └── PolicePatrolScreen.kt    # Patrol map
│   └── theme/                   # App theme
└── MainActivity.kt              # Main entry point
```

## 🎨 UI/UX Features

- **Material 3 Design** with custom emergency color scheme
- **High-contrast** panic UI (red/white)
- **Large, accessible buttons** for emergency situations
- **Smooth animations** using Compose
- **Dark mode support**
- **Responsive layouts** for all screen sizes

## 🔧 Customization

### Adding Real ML Models

Replace placeholder logic in `AIDangerService.kt`:
```kotlin
// Current: Simulated danger detection
// Replace with: TensorFlow Lite or ML Kit integration
```

### Backend Integration

Update `EmergencyService.kt` to connect to your backend:
```kotlin
// Replace mock API call with real HTTP client
// Use Retrofit or Ktor for API calls
```

### Voice Recognition

Implement voice activation in `HomeViewModel.kt`:
```kotlin
// Add SpeechRecognizer for voice commands
// "Help me", "Save me", etc.
```

## 📝 Notes

- **AI Detection**: Currently uses placeholder logic. Replace with actual ML models for production.
- **Backend API**: Emergency server endpoint is mocked. Implement real API integration.
- **Recording**: Audio/video recording placeholders need MediaRecorder implementation.
- **Calculator Mode**: UI placeholder - implement full calculator functionality for stealth mode.

## 🤝 Contributing

This is a hackathon-ready app with placeholders for advanced features. To make it production-ready:

1. Integrate real ML models for danger detection
2. Implement backend API for emergency services
3. Add complete audio/video recording
4. Implement full calculator mode
5. Add voice recognition for SOS
6. Add shake gesture detection

## 📄 License

This project is created for educational and safety purposes.

## ⚠️ Important

This app is designed for emergency situations. Always test thoroughly before relying on it in real emergencies. Some features are placeholders and need full implementation for production use.

---

**Built with ❤️ for Women's Safety**

