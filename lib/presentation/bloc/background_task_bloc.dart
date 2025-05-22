import 'package:background_task_handler/domain/entities/task_entity.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../data/repositories/task_repository_impl.dart';
import '../events/background_task_event.dart';
import '../state/background_task_state.dart';

class BackgroundTaskBloc
    extends Bloc<BackgroundTaskEvent, BackgroundTaskState> {
  final TaskRepositoryImpl repository;

  BackgroundTaskBloc(this.repository) : super(InitialTaskState()) {
    on<StartTaskEvent>(_onStartTask);
    on<StopTaskEvent>(_onStopTask);
    on<CheckPermissionsEvent>(_onCheckPermissions);
    on<RequestPermissionsEvent>(_onRequestPermissions);
  }

  Future<void> _onStartTask(
    StartTaskEvent event,
    Emitter<BackgroundTaskState> emit,
  ) async {
    try {
      await repository.scheduleTask(event.intervalInSeconds);
      emit(TaskRunningState(Task(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        intervalInSeconds: event.intervalInSeconds,
        lastRunTime: DateTime.now(),
        isRunning: true,
      )));
    } catch (e) {
      emit(TaskErrorState(e.toString()));
    }
  }

  Future<void> _onStopTask(
    StopTaskEvent event,
    Emitter<BackgroundTaskState> emit,
  ) async {
    try {
      await repository.cancelTask();
      emit(TaskStoppedState());
    } catch (e) {
      emit(TaskErrorState(e.toString()));
    }
  }

  Future<void> _onCheckPermissions(
    CheckPermissionsEvent event,
    Emitter<BackgroundTaskState> emit,
  ) async {
    try {
      final permissions = await repository.checkPermissions();
      emit(PermissionsState(permissions));
    } catch (e) {
      emit(TaskErrorState(e.toString()));
    }
  }

  Future<void> _onRequestPermissions(
    RequestPermissionsEvent event,
    Emitter<BackgroundTaskState> emit,
  ) async {
    try {
      await repository.requestPermissions();
      final permissions = await repository.checkPermissions();
      emit(PermissionsState(permissions));
    } catch (e) {
      emit(TaskErrorState(e.toString()));
    }
  }
}
