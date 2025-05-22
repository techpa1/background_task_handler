import 'package:equatable/equatable.dart';

abstract class Failure extends Equatable {
  final String message;
  final String? code;

  const Failure(this.message, {this.code});

  @override
  List<Object?> get props => [message, code];
}

class PermissionFailure extends Failure {
  final String permission;

  const PermissionFailure(this.permission)
      : super('Permission denied: $permission', code: 'PERMISSION_DENIED');

  @override
  List<Object?> get props => [permission, message, code];
}

class ServiceFailure extends Failure {
  const ServiceFailure(String message) : super(message, code: 'SERVICE_ERROR');
}

class AlarmFailure extends Failure {
  const AlarmFailure(String message) : super(message, code: 'ALARM_ERROR');
}

class BatteryOptimizationFailure extends Failure {
  const BatteryOptimizationFailure()
      : super('Battery optimization is enabled', code: 'BATTERY_OPTIMIZATION');
}

class StateFailure extends Failure {
  const StateFailure(String message) : super(message, code: 'STATE_ERROR');
}

class CleanupFailure extends Failure {
  const CleanupFailure(String message) : super(message, code: 'CLEANUP_ERROR');
}
