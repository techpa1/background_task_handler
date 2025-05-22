class BackgroundTaskConstants {
  static const String methodChannelName = 'background_task_handler';
  static const String alarmTriggerAction =
      'com.example.background_task_handler.ALARM_TRIGGER';
  static const String notificationChannelId = 'background_task_channel';
  static const String notificationChannelName = 'Background Task Channel';
  static const int notificationId = 1;
  static const int defaultIntervalInSeconds = 60;
  static const int wakeLockTimeout = 10 * 60 * 1000; // 10 minutes

  // Error codes
  static const String errorPermissionDenied = 'PERMISSION_DENIED';
  static const String errorServiceStart = 'SERVICE_START_ERROR';
  static const String errorAlarmScheduling = 'ALARM_SCHEDULING_ERROR';
  static const String errorBatteryOptimization = 'BATTERY_OPTIMIZATION';
  static const String errorInvalidState = 'INVALID_STATE';
  static const String errorResourceCleanup = 'RESOURCE_CLEANUP_ERROR';

  // Permission keys
  static const String permissionNotification = 'notification';
  static const String permissionExactAlarm = 'exactAlarm';
  static const String permissionBatteryOptimization = 'batteryOptimization';

  // Method channel methods
  static const String methodScheduleTask = 'scheduleTask';
  static const String methodCancelTask = 'cancelTask';
  static const String methodCheckPermissions = 'checkPermissions';
  static const String methodRequestPermissions = 'requestPermissions';
}
