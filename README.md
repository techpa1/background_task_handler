# Background Task Handler

A robust Flutter plugin for handling background tasks with proper permission management, foreground service support, and persistent background execution.

## Features

- 🚀 **Background Task Management**
  - Schedule and manage background tasks with configurable intervals
  - Support for both one-time and persistent background tasks
  - Automatic task rescheduling after device reboot
  - Reliable background execution even when the app is closed

- 🔔 **Foreground Service Support**
  - Persistent foreground service with notification
  - Automatic notification recreation if removed
  - Customizable notification content and appearance
  - Low-priority notifications to minimize user disturbance

- 🔒 **Permission Management**
  - Comprehensive permission handling for Android
  - Battery optimization exemption management
  - Notification permission handling (Android 13+)
  - Exact alarm permission handling (Android 12+)

- ⚡ **Performance & Reliability**
  - Wake lock management for reliable execution
  - Battery optimization handling
  - Service persistence in persistent mode
  - Automatic service recovery

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  background_task_handler: ^1.0.0
```

## Required Permissions

### Android Manifest Permissions
Add these permissions to your `android/app/src/main/AndroidManifest.xml`:

```xml
<!-- Required for foreground service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

<!-- Required for background execution -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Required for battery optimization -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />

<!-- Required for exact alarms -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />

<!-- Required for notifications -->
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
await BackgroundTaskHandler.scheduleTask(
  intervalInSeconds: 60,
  isPersistent: true, // Set to true for persistent mode
);
```

### 3. Cancel a Background Task

```dart
await BackgroundTaskHandler.cancelTask();
```

## Features in Detail

### Background Task Modes

1. **One-time Mode**
   - Task runs for the specified interval
   - Stops when the interval is complete
   - Suitable for short-term background operations

2. **Persistent Mode**
   - Task continues running indefinitely
   - Automatically restarts if killed
   - Survives device reboots
   - Suitable for long-term background operations

### Notification Handling

- **Persistent Notifications**
  - Cannot be dismissed by the user
  - Automatically recreated if removed
  - Low priority to minimize user disturbance
  - Customizable content and appearance

- **Notification Channel**
  - Low importance channel
  - No sound or vibration
  - No badge
  - Public visibility

### Permission Handling

1. **Battery Optimization**
   - Shows a dialog explaining the importance
   - Direct link to system settings
   - Handles user rejection gracefully

2. **Notification Permission**
   - Handles Android 13+ requirements
   - Shows system permission dialog
   - Graceful fallback if denied

3. **Exact Alarm Permission**
   - Handles Android 12+ requirements
   - Shows system permission dialog
   - Graceful fallback if denied

## Limitations and Considerations

1. **Battery Impact**
   - Persistent mode may impact battery life
   - Wake lock is used for reliable execution
   - Consider using one-time mode for less critical tasks

2. **Android Version Compatibility**
   - Some features require specific Android versions
   - Permission handling varies by Android version
   - Graceful fallbacks for older versions

3. **Device Manufacturer Restrictions**
   - Some manufacturers have aggressive battery optimization
   - May require manual settings adjustment
   - Not all devices support exact alarms

4. **Background Execution Limits**
   - Android may restrict background execution
   - Battery optimization may affect reliability
   - Consider using foreground service for critical tasks

## Error Handling

The plugin provides proper error handling for various scenarios:

- Permission denied errors
- Service start/stop failures
- Alarm scheduling failures
- Battery optimization issues
- Resource cleanup errors

## Example

See the `example` directory for a complete implementation of the plugin, including:
- Permission handling
- Task scheduling
- Service management
- Error handling
- UI implementation

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

If you encounter any issues or have questions, please:
1. Check the [documentation](https://github.com/techpa1/background_task_handler)
2. Search [existing issues](https://github.com/techpa1/background_task_handler/issues)
3. Create a new issue if needed
