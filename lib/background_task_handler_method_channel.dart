import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'background_task_handler_platform_interface.dart';

/// An implementation of [BackgroundTaskHandlerPlatform] that uses method channels.
class MethodChannelBackgroundTaskHandler extends BackgroundTaskHandlerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('background_task_handler');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
