import 'package:geolocator/geolocator.dart';
import 'package:permission_handler/permission_handler.dart';

class LocationService {
  Future<bool> checkPermissions() async {
    final status = await Permission.location.status;
    if (status.isDenied) {
      final result = await Permission.location.request();
      return result.isGranted;
    }
    return status.isGranted;
  }

  Future<Map<String, double>> getCurrentLocation() async {
    try {
      final hasPermission = await checkPermissions();
      if (!hasPermission) {
        throw Exception('Location permission denied');
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      return {
        'latitude': position.latitude,
        'longitude': position.longitude,
      };
    } catch (e) {
      throw Exception('Failed to get location: $e');
    }
  }

  Stream<Position> getLocationStream() {
    return Geolocator.getPositionStream(
      locationSettings: const LocationSettings(
        accuracy: LocationAccuracy.high,
        distanceFilter: 10,
      ),
    );
  }
}

