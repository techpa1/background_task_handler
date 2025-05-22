import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PermissionHelper {
  static Future<void> showBatteryOptimizationDialog(
      BuildContext context) async {
    // Find the nearest Navigator
    final navigatorContext = Navigator.of(context);
    if (navigatorContext == null) {
      debugPrint('No Navigator found in context');
      return;
    }

    return showDialog(
      context: context,
      barrierDismissible: false,
      builder: (BuildContext dialogContext) {
        return Material(
          type: MaterialType.transparency,
          child: AlertDialog(
            title: const Text('Battery Optimization'),
            content: const Text(
              'To ensure the background service continues running, please disable battery optimization for this app.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(dialogContext).pop(),
                child: const Text('Cancel'),
              ),
              TextButton(
                onPressed: () async {
                  Navigator.of(dialogContext).pop();
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
          ),
        );
      },
    );
  }
}
