import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PermissionHelper {
  static Future<void> showBatteryOptimizationDialog(
      BuildContext context) async {
    return showDialog(
      context: context,
      builder: (BuildContext dialogContext) {
        return Builder(
          builder: (BuildContext builderContext) {
            return AlertDialog(
              title: const Text('Battery Optimization'),
              content: const Text(
                'To ensure the background service continues running, please disable battery optimization for this app.',
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(dialogContext),
                  child: const Text('Cancel'),
                ),
                TextButton(
                  onPressed: () async {
                    Navigator.pop(dialogContext);
                    try {
                      const platform = MethodChannel('background_task_handler');
                      await platform
                          .invokeMethod('openBatteryOptimizationSettings');
                    } on PlatformException catch (e) {
                      debugPrint('Error opening settings: ${e.message}');
                    }
                  },
                  child: const Text('Open Settings'),
                ),
              ],
            );
          },
        );
      },
    );
  }
}
