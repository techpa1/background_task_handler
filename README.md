# Background Task Handler

A Flutter plugin for handling background tasks with proper permission management and foreground service support.

## Features

- Schedule and manage background tasks
- Handle foreground services with notifications
- Proper permission management
- Battery optimization handling
- Clean architecture implementation

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  background_task_handler: ^1.0.0
```

## Required Permissions

The plugin requires the following permissions to work properly:

### Android Manifest Permissions
Add these permissions to your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Runtime Permissions
The plugin requires the following runtime permissions:

1. **Notification Permission** (Android 13+)
   - Required for showing foreground service notifications
   - Request using `requestPermissions()`

2. **Exact Alarm Permission** (Android 12+)
   - Required for scheduling exact alarms
   - Request using `requestPermissions()`

3. **Battery Optimization Exemption**
   - Required for reliable background operation
   - Request using `showBatteryOptimizationDialog()`

## Usage

### 1. Initialize and Check Permissions

```dart
import 'package:background_task_handler/background_task_handler.dart';
import 'package:background_task_handler/core/utils/permission_helper.dart';

// Check and request permissions
Future<void> checkAndRequestPermissions() async {
  try {
    final permissions = await BackgroundTaskHandler.checkPermissions();
    
    // Request notification permission (Android 13+)
    if (permissions['notification'] == false) {
      await BackgroundTaskHandler.requestPermissions();
    }
    
    // Request exact alarm permission (Android 12+)
    if (permissions['exactAlarm'] == false) {
      await BackgroundTaskHandler.requestPermissions();
    }
    
    // Request battery optimization exemption
    if (permissions['batteryOptimization'] == false) {
      await PermissionHelper.showBatteryOptimizationDialog(context);
    }
  } catch (e) {
    // Handle errors
  }
}
```

### 2. Schedule a Background Task

```dart
// Schedule a task to run every 60 seconds
await BackgroundTaskHandler.scheduleTask(intervalInSeconds: 60);
```

### 3. Cancel a Background Task

```dart
await BackgroundTaskHandler.cancelTask();
```

## Permission Handling

### Battery Optimization Dialog
The plugin provides a built-in dialog for requesting battery optimization exemption:

```dart
await PermissionHelper.showBatteryOptimizationDialog(context);
```

This dialog:
- Explains why battery optimization needs to be disabled
- Provides a button to open system settings
- Helps users understand the importance of the permission

### Permission Status
You can check the status of all required permissions:

```dart
final permissions = await BackgroundTaskHandler.checkPermissions();
print('Notification permission: ${permissions['notification']}');
print('Exact alarm permission: ${permissions['exactAlarm']}');
print('Battery optimization: ${permissions['batteryOptimization']}');
```

## Error Handling

The plugin provides proper error handling for various scenarios:

- Permission denied errors
- Service start/stop failures
- Alarm scheduling failures
- Battery optimization issues

## Example

See the `example` directory for a complete implementation of the plugin, including:
- Permission handling
- Task scheduling
- Error handling
- UI implementation

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

