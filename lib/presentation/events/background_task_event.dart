import 'package:equatable/equatable.dart';

abstract class BackgroundTaskEvent extends Equatable {
  const BackgroundTaskEvent();

  @override
  List<Object?> get props => [];
}

class StartTaskEvent extends BackgroundTaskEvent {
  final int intervalInSeconds;

  const StartTaskEvent(this.intervalInSeconds);

  @override
  List<Object?> get props => [intervalInSeconds];
}

class StopTaskEvent extends BackgroundTaskEvent {}

class CheckPermissionsEvent extends BackgroundTaskEvent {}

class RequestPermissionsEvent extends BackgroundTaskEvent {}
