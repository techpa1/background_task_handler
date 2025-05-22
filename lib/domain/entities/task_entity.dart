import 'package:equatable/equatable.dart';

/// Represents a background task entity with its properties and state.
class Task extends Equatable {
  /// Unique identifier for the task
  final String id;

  /// Interval in seconds between task executions
  final int intervalInSeconds;

  /// Timestamp of the last task execution
  final DateTime lastRunTime;

  /// Whether the task is currently running
  final bool isRunning;

  /// Creates a new [Task] instance.
  const Task({
    required this.id,
    required this.intervalInSeconds,
    required this.lastRunTime,
    this.isRunning = false,
  });

  /// Creates a copy of this [Task] with the given fields replaced with the new values.
  Task copyWith({
    String? id,
    int? intervalInSeconds,
    DateTime? lastRunTime,
    bool? isRunning,
  }) {
    return Task(
      id: id ?? this.id,
      intervalInSeconds: intervalInSeconds ?? this.intervalInSeconds,
      lastRunTime: lastRunTime ?? this.lastRunTime,
      isRunning: isRunning ?? this.isRunning,
    );
  }

  @override
  List<Object?> get props => [id, intervalInSeconds, lastRunTime, isRunning];
}
