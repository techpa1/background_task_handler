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
import android.util.Log

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
    private val manufacturer: String = Build.MANUFACTURER.lowercase()
    private val isOplusDevice = manufacturer.contains("oppo") || manufacturer.contains("oneplus")
    private val isXiaomiDevice = manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")
    private val isHuaweiDevice = manufacturer.contains("huawei") || manufacturer.contains("honor")
    private val isSamsungDevice = manufacturer.contains("samsung")
    private val isVivoDevice = manufacturer.contains("vivo")
    private val isRealmeDevice = manufacturer.contains("realme")

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "background_task_handler")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        alarmScheduler = AlarmScheduler(context)
    }

    private fun createBatteryOptimizationIntent(): Intent {
        return when {
            isOplusDevice -> {
                // OnePlus/Oppo specific settings
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("package_name", context.packageName)
                    putExtra("class_name", "com.android.settings.SubSettings")
                    putExtra("settings:show_fragment", "com.android.settings.fuelgauge.PowerUsageSummary")
                    putExtra("settings:show_fragment_title", "Battery")
                }
            }
            isXiaomiDevice -> {
                // Xiaomi/Redmi/Poco specific settings
                Intent().apply {
                    action = "miui.intent.action.OP_AUTO_START"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("package_name", context.packageName)
                    putExtra("class_name", "com.miui.securitycenter.operationguide.AppOperationGuideActivity")
                }
            }
            isHuaweiDevice -> {
                // Huawei/Honor specific settings
                Intent().apply {
                    action = "huawei.intent.action.HSM_BATTERY_SAVER"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("package_name", context.packageName)
                }
            }
            isSamsungDevice -> {
                // Samsung specific settings
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("package_name", context.packageName)
                    putExtra("class_name", "com.samsung.android.sm.ui.battery.BatteryActivity")
                }
            }
            isVivoDevice -> {
                // Vivo specific settings
                Intent().apply {
                    action = "vivo.intent.action.BACKGROUND_APP_MANAGER"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("package_name", context.packageName)
                }
            }
            isRealmeDevice -> {
                // Realme specific settings
                Intent().apply {
                    action = "com.coloros.action.OP_AUTO_START"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("package_name", context.packageName)
                }
            }
            else -> {
                // Standard battery optimization settings
                Intent().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = Uri.parse("package:${context.packageName}")
                    } else {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }
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
                    val intent = createBatteryOptimizationIntent()

                    // Try to start the activity
                    try {
                        context.startActivity(intent)
                        result.success(null)
                    } catch (e: Exception) {
                        // If the first attempt fails, try the alternative approach
                        Log.e("BackgroundTaskHandler", "Failed to open settings with primary intent: ${e.message}")
                        val fallbackIntent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", context.packageName, null)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        context.startActivity(fallbackIntent)
                        result.success(null)
                    }
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
        try {
            val intent = createBatteryOptimizationIntent()

            // Try to start the activity
            try {
                ContextCompat.startActivity(context, intent, null)
                result.success(true)
            } catch (e: Exception) {
                // If the first attempt fails, try the alternative approach
                Log.e("BackgroundTaskHandler", "Failed to open settings with primary intent: ${e.message}")
                val fallbackIntent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                ContextCompat.startActivity(context, fallbackIntent, null)
                result.success(true)
            }
        } catch (e: Exception) {
            result.error("ERROR", "Failed to request permissions", e.message)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
