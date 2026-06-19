import 'dart:async';
import 'package:http/http.dart' as http;
import 'package:sms_advanced/sms_advanced.dart';
import 'package:vibration/vibration.dart';
import 'package:torch_light/torch_light.dart';
import 'location_service.dart';
import 'guardian_service.dart';

class EmergencyService {
  final LocationService _locationService;
  final GuardianService _guardianService;
  Timer? _countdownTimer;
  int _countdownSeconds = 3;

  EmergencyService({
    LocationService? locationService,
    GuardianService? guardianService,
  })  : _locationService = locationService ?? LocationService(),
        _guardianService = guardianService ?? GuardianService();

  Future<void> triggerSOS({
    required Function(int) onCountdown,
    required VoidCallback onActivated,
  }) async {
    _countdownSeconds = 3;
    
    // Start countdown
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      _countdownSeconds--;
      onCountdown(_countdownSeconds);
      
      if (_countdownSeconds <= 0) {
        timer.cancel();
        _activateSOS();
        onActivated();
      }
    });
  }

  void cancelSOS() {
    _countdownTimer?.cancel();
    _countdownTimer = null;
  }

  Future<void> _activateSOS() async {
    try {
      // Get current location
      final position = await _locationService.getCurrentLocation();
      
      // Vibrate device
      await Vibration.vibrate(duration: 1000);
      
      // Flash light
      try {
        await TorchLight.enableTorch();
        Future.delayed(const Duration(milliseconds: 500), () {
          TorchLight.disableTorch();
        });
      } catch (e) {
        // Torch not available
      }

      // Send location to guardians
      await _sendLocationToGuardians(position);

      // Send to backup server
      await _sendToBackupServer(position);

      // Send SMS fallback
      await _sendSMSFallback(position);
    } catch (e) {
      // Handle error
    }
  }

  Future<void> _sendLocationToGuardians(Map<String, double> position) async {
    final guardians = await _guardianService.getGuardians();
    final message = 
        '🚨 EMERGENCY SOS 🚨\n'
        'Location: https://maps.google.com/?q=${position['latitude']},${position['longitude']}\n'
        'Time: ${DateTime.now().toIso8601String()}';

    for (var guardian in guardians) {
      try {
        final sms = SmsSender();
        await sms.sendSms(
          SmsMessage(guardian['phone'] ?? '', message),
        );
      } catch (e) {
        // Handle SMS error
      }
    }
  }

  Future<void> _sendToBackupServer(Map<String, double> position) async {
    try {
      final response = await http.post(
        Uri.parse('https://api.panicsafe.com/emergency'), // Dummy API
        headers: {'Content-Type': 'application/json'},
        body: {
          'latitude': position['latitude'],
          'longitude': position['longitude'],
          'timestamp': DateTime.now().toIso8601String(),
          'danger_level': 'HIGH',
        },
      ).timeout(const Duration(seconds: 5));
    } catch (e) {
      // Offline - will use SMS fallback
    }
  }

  Future<void> _sendSMSFallback(Map<String, double> position) async {
    // SMS fallback for offline scenarios
    final guardians = await _guardianService.getGuardians();
    final message = 
        '🚨 EMERGENCY SOS 🚨\n'
        'Location: ${position['latitude']}, ${position['longitude']}\n'
        'Time: ${DateTime.now().toString()}';

    for (var guardian in guardians) {
      try {
        final sms = SmsSender();
        await sms.sendSms(
          SmsMessage(guardian['phone'] ?? '', message),
        );
      } catch (e) {
        // Handle error
      }
    }
  }

  Future<void> startRecording() async {
    // Placeholder for audio/video recording
    // In production, use camera and record packages
  }

  Future<void> stopRecording() async {
    // Stop recording
  }
}

