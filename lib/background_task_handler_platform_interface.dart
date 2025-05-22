import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'background_task_handler_method_channel.dart';

abstract class BackgroundTaskHandlerPlatform extends PlatformInterface {
  /// Constructs a BackgroundTaskHandlerPlatform.
  BackgroundTaskHandlerPlatform() : super(token: _token);

  static final Object _token = Object();

  static BackgroundTaskHandlerPlatform _instance = MethodChannelBackgroundTaskHandler();

  /// The default instance of [BackgroundTaskHandlerPlatform] to use.
  ///
  /// Defaults to [MethodChannelBackgroundTaskHandler].
  static BackgroundTaskHandlerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BackgroundTaskHandlerPlatform] when
  /// they register themselves.
  static set instance(BackgroundTaskHandlerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    try {
      return _instance.getPlatformVersion();
    } catch (e) {
      throw UnimplementedError('platformVersion() has not been implemented.');
    }
  }
}
