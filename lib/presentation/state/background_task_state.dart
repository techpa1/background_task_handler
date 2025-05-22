import 'package:equatable/equatable.dart';
import '../../domain/entities/task_entity.dart';

abstract class BackgroundTaskState extends Equatable {
  const BackgroundTaskState();

  @override
  List<Object?> get props => [];
}

class InitialTaskState extends BackgroundTaskState {}

class TaskRunningState extends BackgroundTaskState {
  final Task task;

  const TaskRunningState(this.task);

  @override
  List<Object?> get props => [task];
}

class TaskStoppedState extends BackgroundTaskState {}

class TaskErrorState extends BackgroundTaskState {
  final String message;

  const TaskErrorState(this.message);

  @override
  List<Object?> get props => [message];
}

class PermissionsState extends BackgroundTaskState {
  final Map<String, bool> permissions;

  const PermissionsState(this.permissions);

  @override
  List<Object?> get props => [permissions];
}
