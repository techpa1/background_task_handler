import '../entities/task_entity.dart';
import '../../core/error/failures.dart';

abstract class TaskRepository {
  /// Schedule a background task with the specified interval
  ///
  /// Throws [PermissionFailure] if required permissions are not granted
  /// Throws [ServiceFailure] if service fails to start
  /// Throws [AlarmFailure] if alarm scheduling fails
  Future<void> scheduleTask(int intervalInSeconds);

  /// Cancel the currently running background task
  ///
  /// Throws [ServiceFailure] if service fails to stop
  /// Throws [AlarmFailure] if alarm fails to cancel
  Future<void> cancelTask();

  /// Check if all required permissions are granted
  ///
  /// Returns a map of permission keys to their granted status
  Future<Map<String, bool>> checkPermissions();

  /// Request required permissions
  ///
  /// Throws [PermissionFailure] if permissions are denied
  Future<void> requestPermissions();

  /// Get the current task status
  ///
  /// Returns null if no task is running
  Future<Task?> getCurrentTask();
}
