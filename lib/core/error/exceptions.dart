class BackgroundTaskException implements Exception {
  final String message;
  final String? code;

  BackgroundTaskException(this.message, {this.code});

  @override
  String toString() =>
      'BackgroundTaskException: $message${code != null ? ' (Code: $code)' : ''}';
}

class PermissionDeniedException extends BackgroundTaskException {
  final String permission;

  PermissionDeniedException(this.permission)
      : super('Permission denied: $permission', code: 'PERMISSION_DENIED');
}

class ServiceStartException extends BackgroundTaskException {
  ServiceStartException(String message)
      : super(message, code: 'SERVICE_START_ERROR');
}

class AlarmSchedulingException extends BackgroundTaskException {
  AlarmSchedulingException(String message)
      : super(message, code: 'ALARM_SCHEDULING_ERROR');
}

class BatteryOptimizationException extends BackgroundTaskException {
  BatteryOptimizationException()
      : super('Battery optimization is enabled', code: 'BATTERY_OPTIMIZATION');
}

class InvalidStateException extends BackgroundTaskException {
  InvalidStateException(String message) : super(message, code: 'INVALID_STATE');
}

class ResourceCleanupException extends BackgroundTaskException {
  ResourceCleanupException(String message)
      : super(message, code: 'RESOURCE_CLEANUP_ERROR');
}
