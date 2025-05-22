import 'package:flutter_test/flutter_test.dart';
import 'package:background_task_handler/background_task_handler_platform_interface.dart';
import 'package:background_task_handler/background_task_handler_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockBackgroundTaskHandlerPlatform
    with MockPlatformInterfaceMixin
    implements BackgroundTaskHandlerPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<Map<String, bool>> checkPermissions() => Future.value({
        'notification': true,
        'exactAlarm': true,
        'batteryOptimization': true,
      });
}

void main() {
  final BackgroundTaskHandlerPlatform initialPlatform =
      BackgroundTaskHandlerPlatform.instance;

  test('$MethodChannelBackgroundTaskHandler is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelBackgroundTaskHandler>());
  });

  test('checkPermissions returns correct values', () async {
    MockBackgroundTaskHandlerPlatform fakePlatform =
        MockBackgroundTaskHandlerPlatform();
    BackgroundTaskHandlerPlatform.instance = fakePlatform;

    final permissions = await fakePlatform.checkPermissions();
    expect(permissions['notification'], true);
    expect(permissions['exactAlarm'], true);
    expect(permissions['batteryOptimization'], true);
  });
}
