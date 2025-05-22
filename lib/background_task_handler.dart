import 'package:background_task_handler/background_task_handler_platform_interface.dart';
import 'dart:async';
import 'package:flutter/services.dart';

import 'data/repositories/task_repository_impl.dart';
import 'data/datasources/task_datasource.dart';
import 'presentation/bloc/background_task_bloc.dart';
import 'presentation/events/background_task_event.dart';
import 'presentation/state/background_task_state.dart';

class BackgroundTaskHandler {
  static final TaskRepositoryImpl _repository =
      TaskRepositoryImpl(TaskDataSourceImpl());
  static BackgroundTaskBloc? _bloc;
  static const MethodChannel _channel =
      MethodChannel('background_task_handler');

  /// Get the singleton instance of [BackgroundTaskBloc]
  static BackgroundTaskBloc get bloc {
    _bloc ??= BackgroundTaskBloc(_repository);
    return _bloc!;
  }

  /// Schedule a background task to run at the specified interval.
  ///
  /// [intervalInSeconds] The interval in seconds between task executions.
  /// [isPersistent] If true, the service will restart if killed and after device reboot.
  ///                If false, the service will stop when killed.
  static Future<bool> scheduleTask({
    required int intervalInSeconds,
    bool isPersistent = false,
  }) async {
    try {
      final bool result = await _channel.invokeMethod('scheduleTask', {
        'interval': intervalInSeconds,
        'isPersistent': isPersistent,
      });
      return result;
    } on PlatformException catch (e) {
      throw Exception('Failed to schedule task: ${e.message}');
    }
  }

  /// Cancel the currently scheduled background task.
  static Future<bool> cancelTask() async {
    try {
      final bool result = await _channel.invokeMethod('cancelTask');
      return result;
    } on PlatformException catch (e) {
      throw Exception('Failed to cancel task: ${e.message}');
    }
  }

  /// Check the status of required permissions.
  /// Returns a map with the following keys:
  /// - notification: bool (Android 13+)
  /// - exactAlarm: bool (Android 12+)
  /// - batteryOptimization: bool
  static Future<Map<String, bool>> checkPermissions() async {
    try {
      final Map<dynamic, dynamic> result =
          await _channel.invokeMethod('checkPermissions');
      return Map<String, bool>.from(result);
    } on PlatformException catch (e) {
      throw Exception('Failed to check permissions: ${e.message}');
    }
  }

  /// Request the required permissions.
  static Future<bool> requestPermissions() async {
    try {
      final bool result = await _channel.invokeMethod('requestPermissions');
      return result;
    } on PlatformException catch (e) {
      throw Exception('Failed to request permissions: ${e.message}');
    }
  }

  /// Dispose the bloc when it's no longer needed
  static void dispose() {
    _bloc?.close();
    _bloc = null;
  }

  static Future<String?> getPlatformVersion() async {
    return BackgroundTaskHandlerPlatform.instance.getPlatformVersion();
  }
}
