import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:background_task_handler/background_task_handler.dart';
import 'package:background_task_handler/core/utils/permission_helper.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _permissionStatus = 'Checking permissions...';
  bool _isTaskRunning = false;
  bool _isPersistentMode = false;

  @override
  void initState() {
    super.initState();
    // Schedule permission check after the first frame
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkAndRequestPermissions();
    });
  }

  Future<void> _checkAndRequestPermissions() async {
    try {
      final permissions = await BackgroundTaskHandler.checkPermissions();

      if (permissions['notification'] == false) {
        await BackgroundTaskHandler.requestPermissions();
      }

      if (permissions['exactAlarm'] == false) {
        await BackgroundTaskHandler.requestPermissions();
      }

      if (permissions['batteryOptimization'] == false) {
        if (mounted) {
          // Wait for the next frame to ensure MaterialApp is fully initialized
          await Future.delayed(const Duration(milliseconds: 100));
          if (mounted) {
            // Use the context from the Scaffold
            final scaffoldContext = context;
            if (Navigator.of(scaffoldContext) != null) {
              await PermissionHelper.showBatteryOptimizationDialog(
                  scaffoldContext);
            }
          }
        }
      }

      if (!mounted) return;
      setState(() {
        _permissionStatus = 'Permissions granted: ${permissions.toString()}';
      });
    } on PlatformException catch (e) {
      if (!mounted) return;
      setState(() {
        _permissionStatus = 'Failed to check permissions: ${e.message}';
      });
    }
  }

  Future<void> _startTask() async {
    try {
      await BackgroundTaskHandler.scheduleTask(
        intervalInSeconds: 60,
        isPersistent: _isPersistentMode,
      );
      if (!mounted) return;
      setState(() {
        _isTaskRunning = true;
        _permissionStatus =
            'Task started successfully (${_isPersistentMode ? "Persistent" : "One-time"} mode)';
      });
    } on PlatformException catch (e) {
      if (!mounted) return;
      setState(() {
        _permissionStatus = 'Failed to start task: ${e.message}';
      });
    }
  }

  Future<void> _stopTask() async {
    try {
      await BackgroundTaskHandler.cancelTask();
      if (!mounted) return;
      setState(() {
        _isTaskRunning = false;
        _permissionStatus = 'Task stopped successfully';
      });
    } on PlatformException catch (e) {
      if (!mounted) return;
      setState(() {
        _permissionStatus = 'Failed to stop task: ${e.message}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Background Task Handler Example',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('en'), // English
      ],
      home: Builder(
        builder: (context) => Scaffold(
          appBar: AppBar(
            title: const Text('Background Task Handler Example'),
          ),
          body: Center(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    _permissionStatus,
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.bodyLarge,
                  ),
                  const SizedBox(height: 20),
                  SwitchListTile(
                    title: const Text('Persistent Mode'),
                    subtitle: const Text(
                        'Service will restart if killed and after reboot'),
                    value: _isPersistentMode,
                    onChanged: (value) {
                      setState(() {
                        _isPersistentMode = value;
                      });
                    },
                  ),
                  const SizedBox(height: 20),
                  ElevatedButton(
                    onPressed: _isTaskRunning ? null : _startTask,
                    child: const Text('Start Background Task'),
                  ),
                  const SizedBox(height: 10),
                  ElevatedButton(
                    onPressed: _isTaskRunning ? _stopTask : null,
                    child: const Text('Stop Background Task'),
                  ),
                  const SizedBox(height: 20),
                  TextButton(
                    onPressed: () =>
                        PermissionHelper.showBatteryOptimizationDialog(context),
                    child: const Text('Open Battery Optimization Settings'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
