import '../../domain/entities/task_entity.dart';

/// A model class that extends [Task] entity with JSON serialization capabilities.
class TaskModel extends Task {
  /// Creates a new [TaskModel] instance.
  const TaskModel({
    required String id,
    required int intervalInSeconds,
    required DateTime lastRunTime,
    bool isRunning = false,
  }) : super(
          id: id,
          intervalInSeconds: intervalInSeconds,
          lastRunTime: lastRunTime,
          isRunning: isRunning,
        );

  /// Creates a [TaskModel] from a [Task] entity.
  factory TaskModel.fromEntity(Task task) {
    return TaskModel(
      id: task.id,
      intervalInSeconds: task.intervalInSeconds,
      lastRunTime: task.lastRunTime,
      isRunning: task.isRunning,
    );
  }

  /// Creates a [TaskModel] from a JSON map.
  factory TaskModel.fromJson(Map<String, dynamic> json) {
    return TaskModel(
      id: json['id'] as String,
      intervalInSeconds: json['intervalInSeconds'] as int,
      lastRunTime: DateTime.parse(json['lastRunTime'] as String),
      isRunning: json['isRunning'] as bool,
    );
  }

  /// Converts this [TaskModel] to a JSON map.
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'intervalInSeconds': intervalInSeconds,
      'lastRunTime': lastRunTime.toIso8601String(),
      'isRunning': isRunning,
    };
  }
}
