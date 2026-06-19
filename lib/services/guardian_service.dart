import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

class GuardianService {
  static const String _guardiansKey = 'guardians_list';

  Future<List<Map<String, String>>> getGuardians() async {
    final prefs = await SharedPreferences.getInstance();
    final guardiansJson = prefs.getString(_guardiansKey);
    
    if (guardiansJson == null) {
      return [];
    }
    
    final List<dynamic> decoded = json.decode(guardiansJson);
    return decoded.map((item) => Map<String, String>.from(item)).toList();
  }

  Future<void> addGuardian(String name, String phone, String relation) async {
    final guardians = await getGuardians();
    guardians.add({
      'name': name,
      'phone': phone,
      'relation': relation,
    });
    
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_guardiansKey, json.encode(guardians));
  }

  Future<void> removeGuardian(int index) async {
    final guardians = await getGuardians();
    if (index >= 0 && index < guardians.length) {
      guardians.removeAt(index);
      
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_guardiansKey, json.encode(guardians));
    }
  }

  Future<void> updateGuardian(int index, String name, String phone, String relation) async {
    final guardians = await getGuardians();
    if (index >= 0 && index < guardians.length) {
      guardians[index] = {
        'name': name,
        'phone': phone,
        'relation': relation,
      };
      
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_guardiansKey, json.encode(guardians));
    }
  }
}

