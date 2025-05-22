import 'package:flutter/services.dart';
import '../../core/utils/constants.dart';
import '../../core/error/exceptions.dart';

abstract class TaskDataSource {
  Future<void> scheduleTask(int intervalInSeconds);
  Future<void> cancelTask();
  Future<Map<String, bool>> checkPermissions();
  Future<void> requestPermissions();
}

class TaskDataSourceImpl implements TaskDataSource {
  final MethodChannel _channel;

  TaskDataSourceImpl()
      : _channel =
            const MethodChannel(BackgroundTaskConstants.methodChannelName);

  @override
  Future<void> scheduleTask(int intervalInSeconds) async {
    try {
      await _channel.invokeMethod(
        BackgroundTaskConstants.methodScheduleTask,
        {'interval': intervalInSeconds},
      );
    } on PlatformException catch (e) {
      switch (e.code) {
        case BackgroundTaskConstants.errorPermissionDenied:
          throw PermissionDeniedException(
              e.message ?? 'Unknown permission error');
        case BackgroundTaskConstants.errorServiceStart:
          throw ServiceStartException(e.message ?? 'Failed to start service');
        case BackgroundTaskConstants.errorAlarmScheduling:
          throw AlarmSchedulingException(
              e.message ?? 'Failed to schedule alarm');
        default:
          throw BackgroundTaskException(e.message ?? 'Unknown error',
              code: e.code);
      }
    }
  }

  @override
  Future<void> cancelTask() async {
    try {
      await _channel.invokeMethod(BackgroundTaskConstants.methodCancelTask);
    } on PlatformException catch (e) {
      throw ServiceStartException(e.message ?? 'Failed to cancel task');
    }
  }

  @override
  Future<Map<String, bool>> checkPermissions() async {
    try {
      final result = await _channel
          .invokeMethod(BackgroundTaskConstants.methodCheckPermissions);
      return Map<String, bool>.from(result);
    } on PlatformException catch (e) {
      throw PermissionDeniedException(
          e.message ?? 'Failed to check permissions');
    }
  }

  @override
  Future<void> requestPermissions() async {
    try {
      await _channel
          .invokeMethod(BackgroundTaskConstants.methodRequestPermissions);
    } on PlatformException catch (e) {
      throw PermissionDeniedException(
          e.message ?? 'Failed to request permissions');
    }
  }
}
