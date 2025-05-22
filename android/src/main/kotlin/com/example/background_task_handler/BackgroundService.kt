package com.example.background_task_handler

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import android.util.Log
import android.os.Process
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.app.AlarmManager
import android.os.SystemClock
import android.content.SharedPreferences
import android.view.WindowManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.os.Handler
import android.os.Looper
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle

class BackgroundService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isRunning = false
    private val CHANNEL_ID = "background_task_channel"
    private val NOTIFICATION_ID = 1
    private var notificationManager: NotificationManager? = null
    private var notificationCheckThread: Thread? = null
    private var manufacturer: String = Build.MANUFACTURER.lowercase()
    private var isOplusDevice = manufacturer.contains("oppo") || manufacturer.contains("oneplus")
    private var alarmManager: AlarmManager? = null
    private lateinit var prefs: SharedPreferences
    private val mainHandler = Handler(Looper.getMainLooper())
    private var notificationReceiver: BroadcastReceiver? = null

    companion object {
        const val EXTRA_IS_PERSISTENT = "is_persistent"
        private const val PREFS_NAME = "BackgroundServicePrefs"
        private const val KEY_IS_PERSISTENT = "is_persistent"
        private const val NOTIFICATION_CHECK_INTERVAL = 5000L // Check every 5 seconds
        private const val RESTART_DELAY = 1000L // 1 second delay before restart
        private const val OPLUS_CHECK_INTERVAL = 30000L // Check every 30 seconds for Oplus devices
        private const val ALARM_INTERVAL = 15 * 60 * 1000L // 15 minutes
        private const val OPLUS_PROCESS_CHECK_INTERVAL = 60000L // Check every minute for Oplus devices
        private const val ACTION_NOTIFICATION_REMOVED = "com.example.background_task_handler.NOTIFICATION_REMOVED"
    }

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        createNotificationChannel()
        acquireWakeLock()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        setServicePriority()
        registerNotificationReceiver()
        if (isOplusDevice) {
            startOplusProtection()
            startOplusProcessCheck()
        }
    }

    private fun registerNotificationReceiver() {
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == ACTION_NOTIFICATION_REMOVED) {
                    mainHandler.post {
                        updateNotification()
                    }
                }
            }
        }
        registerReceiver(notificationReceiver, IntentFilter(ACTION_NOTIFICATION_REMOVED))
    }

    private fun unregisterNotificationReceiver() {
        try {
            notificationReceiver?.let {
                unregisterReceiver(it)
            }
        } catch (e: Exception) {
            Log.e("BackgroundService", "Error unregistering notification receiver", e)
        }
        notificationReceiver = null
    }

    private fun startOplusProtection() {
        Thread {
            while (isRunning) {
                try {
                    if (isOplusDevice) {
                        // Check if our app is in the foreground
                        if (!isAppInForeground()) {
                            // If not in foreground, ensure our service is still running
                            if (!isServiceRunning()) {
                                Log.d("BackgroundService", "Service was killed on Oplus device, restarting...")
                                restartService()
                            }
                        }
                    }
                    Thread.sleep(OPLUS_CHECK_INTERVAL)
                } catch (e: InterruptedException) {
                    Log.d("BackgroundService", "Oplus protection thread interrupted")
                    break
                } catch (e: Exception) {
                    Log.e("BackgroundService", "Error in Oplus protection", e)
                }
            }
        }.start()
    }

    private fun startOplusProcessCheck() {
        Thread {
            while (isRunning) {
                try {
                    if (isOplusDevice) {
                        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                        val runningProcesses = activityManager.runningAppProcesses
                        val currentProcess = runningProcesses?.find { it.pid == Process.myPid() }
                        
                        if (currentProcess != null) {
                            // Ensure our process stays in foreground priority
                            if (currentProcess.importance > RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                                Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
                                // Try to set component enabled state
                                try {
                                    val componentName = ComponentName(this, BackgroundService::class.java)
                                    packageManager.setComponentEnabledSetting(
                                        componentName,
                                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                        PackageManager.DONT_KILL_APP
                                    )
                                } catch (e: Exception) {
                                    Log.e("BackgroundService", "Error setting component state", e)
                                }
                            }
                        }
                    }
                    Thread.sleep(OPLUS_PROCESS_CHECK_INTERVAL)
                } catch (e: InterruptedException) {
                    Log.d("BackgroundService", "Oplus process check thread interrupted")
                    break
                } catch (e: Exception) {
                    Log.e("BackgroundService", "Error in Oplus process check", e)
                }
            }
        }.start()
    }

    private fun isAppInForeground(): Boolean {
        try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 1000,
                time
            )
            if (stats != null) {
                val foregroundApp = stats.maxByOrNull { it.lastTimeUsed }
                return foregroundApp?.packageName == packageName
            }
        } catch (e: Exception) {
            Log.e("BackgroundService", "Error checking foreground app", e)
        }
        return false
    }

    private fun setServicePriority() {
        try {
            when {
                isOplusDevice -> {
                    // Oplus specific priority settings
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
                    // Try to set component enabled state
                    try {
                        val componentName = ComponentName(this, BackgroundService::class.java)
                        packageManager.setComponentEnabledSetting(
                            componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    } catch (e: Exception) {
                        Log.e("BackgroundService", "Error setting component state", e)
                    }
                }
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
                }
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
                }
                manufacturer.contains("samsung") -> {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND)
                }
            }
        } catch (e: Exception) {
            Log.e("BackgroundService", "Error setting service priority", e)
        }
    }

    private fun scheduleAlarm() {
        if (!isPersistentMode) return

        try {
            val intent = Intent(this, BackgroundService::class.java).apply {
                putExtra(EXTRA_IS_PERSISTENT, true)
                action = "com.example.background_task_handler.ALARM_TRIGGER"
            }
            val pendingIntent = PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel any existing alarms
            alarmManager?.cancel(pendingIntent)

            // Schedule new alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + ALARM_INTERVAL,
                    pendingIntent
                )
            } else {
                alarmManager?.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + ALARM_INTERVAL,
                    pendingIntent
                )
            }
            Log.d("BackgroundService", "Alarm scheduled for ${ALARM_INTERVAL/1000/60} minutes from now")
        } catch (e: Exception) {
            Log.e("BackgroundService", "Error scheduling alarm", e)
        }
    }

    private var isPersistentMode: Boolean
        get() = prefs.getBoolean(KEY_IS_PERSISTENT, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_PERSISTENT, value).apply()
        }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check if this is an alarm trigger
        val isAlarmTrigger = intent?.action == "com.example.background_task_handler.ALARM_TRIGGER"
        
        // Set persistent mode based on intent or existing state
        if (intent?.hasExtra(EXTRA_IS_PERSISTENT) == true) {
            isPersistentMode = intent.getBooleanExtra(EXTRA_IS_PERSISTENT, false)
        }
        
        if (!isRunning) {
            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification())
            startNotificationCheck()
            doBackgroundWork()
        } else {
            // If service is already running, update the notification
            updateNotification()
        }

        // Schedule next alarm if in persistent mode
        if (isPersistentMode) {
            scheduleAlarm()
        }
        
        return if (isPersistentMode) START_STICKY else START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Task Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for background task notifications"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                if (isOplusDevice) {
                    setImportance(NotificationManager.IMPORTANCE_HIGH)
                    // Oplus specific channel settings
                    enableVibration(false)
                    setShowBadge(false)
                    // Disable Oplus-specific features
                    setShowBadge(false)
                    enableLights(false)
                }
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val deleteIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(ACTION_NOTIFICATION_REMOVED),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Background Task Running")
            .setContentText(if (isPersistentMode) "Service is active (Persistent)" else "Service is active (One-time)")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(deleteIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        if (isRunning) {
            try {
                val notification = createNotification()
                notificationManager?.notify(NOTIFICATION_ID, notification)
                startForeground(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Log.e("BackgroundService", "Error updating notification", e)
                if (isPersistentMode) {
                    mainHandler.postDelayed({
                        restartService()
                    }, RESTART_DELAY)
                }
            }
        }
    }

    private fun startNotificationCheck() {
        notificationCheckThread?.interrupt()
        notificationCheckThread = Thread {
            while (isRunning) {
                try {
                    if (notificationManager?.getActiveNotifications()?.none { it.id == NOTIFICATION_ID } == true) {
                        Log.d("BackgroundService", "Notification was removed, recreating...")
                        mainHandler.post {
                            updateNotification()
                        }
                    }
                    Thread.sleep(NOTIFICATION_CHECK_INTERVAL)
                } catch (e: InterruptedException) {
                    Log.d("BackgroundService", "Notification check thread interrupted")
                    break
                } catch (e: Exception) {
                    Log.e("BackgroundService", "Error checking notification", e)
                }
            }
        }.apply {
            name = "NotificationCheckThread"
            start()
        }
    }

    private fun doBackgroundWork() {
        Thread {
            while (isRunning) {
                try {
                    Log.d("BackgroundService", "Working... (Mode: ${if (isPersistentMode) "Persistent" else "One-time"})")
                    if (isPersistentMode && !isServiceRunning()) {
                        restartService()
                    }
                    Thread.sleep(60000)
                } catch (e: InterruptedException) {
                    Log.e("BackgroundService", "Thread interrupted", e)
                    break
                }
            }
        }.start()
    }

    private fun isServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == this.javaClass.name }
    }

    private fun restartService() {
        if (!isPersistentMode) return

        try {
            val intent = Intent(applicationContext, BackgroundService::class.java).apply {
                putExtra(EXTRA_IS_PERSISTENT, true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e("BackgroundService", "Error restarting service", e)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BackgroundTaskHandler::WakeLock"
        ).apply {
            acquire(10*60*1000L /*10 minutes*/)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    override fun onDestroy() {
        isRunning = false
        notificationCheckThread?.interrupt()
        notificationCheckThread = null
        unregisterNotificationReceiver()
        releaseWakeLock()
        
        if (isPersistentMode) {
            // Schedule alarm before destroying service
            scheduleAlarm()
            
            // Add a small delay before restarting to prevent rapid restarts
            Thread {
                Thread.sleep(RESTART_DELAY)
                mainHandler.post {
                    restartService()
                }
            }.start()
        }
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
} 
