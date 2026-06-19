import 'package:flutter/foundation.dart';

class AppState extends ChangeNotifier {
  bool _isSOSActive = false;
  bool _isRecording = false;
  bool _isPanicMode = false;
  bool _isCalculatorMode = false;
  double _dangerLevel = 0.0;
  bool _isLocationSharing = false;

  bool get isSOSActive => _isSOSActive;
  bool get isRecording => _isRecording;
  bool get isPanicMode => _isPanicMode;
  bool get isCalculatorMode => _isCalculatorMode;
  double get dangerLevel => _dangerLevel;
  bool get isLocationSharing => _isLocationSharing;

  void setSOSActive(bool value) {
    _isSOSActive = value;
    notifyListeners();
  }

  void setRecording(bool value) {
    _isRecording = value;
    notifyListeners();
  }

  void setPanicMode(bool value) {
    _isPanicMode = value;
    notifyListeners();
  }

  void setCalculatorMode(bool value) {
    _isCalculatorMode = value;
    notifyListeners();
  }

  void setDangerLevel(double value) {
    _dangerLevel = value;
    notifyListeners();
  }

  void setLocationSharing(bool value) {
    _isLocationSharing = value;
    notifyListeners();
  }
}

