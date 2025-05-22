import '../../domain/entities/task_entity.dart';
import '../../domain/repositories/task_repository.dart';
import '../datasources/task_datasource.dart';
import '../../core/error/exceptions.dart';
import '../../core/error/failures.dart';

class TaskRepositoryImpl implements TaskRepository {
  final TaskDataSource dataSource;

  TaskRepositoryImpl(this.dataSource);

  @override
  Future<void> scheduleTask(int intervalInSeconds) async {
    try {
      await dataSource.scheduleTask(intervalInSeconds);
    } on PermissionDeniedException catch (e) {
      throw PermissionFailure(e.permission);
    } on ServiceStartException catch (e) {
      throw ServiceFailure(e.message);
    } on AlarmSchedulingException catch (e) {
      throw AlarmFailure(e.message);
    } on BackgroundTaskException catch (e) {
      throw ServiceFailure(e.message);
    }
  }

  @override
  Future<void> cancelTask() async {
    try {
      await dataSource.cancelTask();
    } on ServiceStartException catch (e) {
      throw ServiceFailure(e.message);
    } on BackgroundTaskException catch (e) {
      throw ServiceFailure(e.message);
    }
  }

  @override
  Future<Map<String, bool>> checkPermissions() async {
    try {
      return await dataSource.checkPermissions();
    } on PermissionDeniedException catch (e) {
      throw PermissionFailure(e.permission);
    } on BackgroundTaskException catch (e) {
      throw PermissionFailure(e.message);
    }
  }

  @override
  Future<void> requestPermissions() async {
    try {
      await dataSource.requestPermissions();
    } on PermissionDeniedException catch (e) {
      throw PermissionFailure(e.permission);
    } on BackgroundTaskException catch (e) {
      throw PermissionFailure(e.message);
    }
  }

  @override
  Future<Task?> getCurrentTask() async {
    // Implementation depends on how you want to track the current task
    // This could be stored in shared preferences or another persistence mechanism
    return null;
  }
}
