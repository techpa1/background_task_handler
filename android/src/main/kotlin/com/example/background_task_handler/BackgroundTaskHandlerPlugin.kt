package com.example.background_task_handler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BackgroundTaskHandlerPlugin */
class BackgroundTaskHandlerPlugin :
    FlutterPlugin,
    MethodCallHandler {
    // The MethodChannel that will the communication between Flutter and native Android
    //
    // This local reference serves to register the plugin with the Flutter Engine and unregister it
    // when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var alarmScheduler: AlarmScheduler

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "background_task_handler")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        alarmScheduler = AlarmScheduler(context)
    }

    override fun onMethodCall(
        call: MethodCall,
        result: Result
    ) {
        when (call.method) {
            "scheduleTask" -> {
                val interval = call.argument<Int>("interval") ?: 60
                val isPersistent = call.argument<Boolean>("isPersistent") ?: false
                scheduleTask(interval.toLong(), isPersistent, result)
            }
            "cancelTask" -> {
                cancelTask(result)
            }
            "checkPermissions" -> {
                checkPermissions(result)
            }
            "requestPermissions" -> {
                requestPermissions(result)
            }
            "openBatteryOptimizationSettings" -> {
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    result.success(null)
                } catch (e: Exception) {
                    result.error("ERROR", "Failed to open battery optimization settings", e.message)
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun scheduleTask(intervalInSeconds: Long, isPersistent: Boolean, result: Result) {
        try {
            val serviceIntent = Intent(context, BackgroundService::class.java).apply {
                putExtra(BackgroundService.EXTRA_IS_PERSISTENT, isPersistent)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            alarmScheduler.scheduleAlarm(intervalInSeconds)
            result.success(true)
        } catch (e: Exception) {
            result.error("SCHEDULE_ERROR", e.message, null)
        }
    }

    private fun cancelTask(result: Result) {
        try {
            val serviceIntent = Intent(context, BackgroundService::class.java)
            context.stopService(serviceIntent)
            alarmScheduler.cancelAlarm()
            result.success(true)
        } catch (e: Exception) {
            result.error("CANCEL_ERROR", e.message, null)
        }
    }

    private fun checkPermissions(result: Result) {
        val permissions = mutableMapOf<String, Boolean>()
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions["notification"] = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        
        // Check exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            permissions["exactAlarm"] = alarmManager.canScheduleExactAlarms()
        }
        
        // Check battery optimization
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        permissions["batteryOptimization"] = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        
        result.success(permissions)
    }

    private fun requestPermissions(result: Result) {
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ContextCompat.startActivity(context, intent, null)
        result.success(true)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
