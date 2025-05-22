package com.example.background_task_handler

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import android.util.Log

class BackgroundService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isRunning = false
    private val CHANNEL_ID = "background_task_channel"
    private val NOTIFICATION_ID = 1

    companion object {
        const val EXTRA_IS_PERSISTENT = "is_persistent"
        private var isPersistentMode = false
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isPersistentMode = intent?.getBooleanExtra(EXTRA_IS_PERSISTENT, false) ?: false
        
        if (!isRunning) {
            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification())
            doBackgroundWork()
        }
        
        return if (isPersistentMode) START_STICKY else START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Task Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for background task notifications"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Background Task Running")
            .setContentText(if (isPersistentMode) "Service is active (Persistent)" else "Service is active (One-time)")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(isPersistentMode)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun doBackgroundWork() {
        Thread {
            while (isRunning) {
                try {
                    Log.d("BackgroundService", "Working... (Mode: ${if (isPersistentMode) "Persistent" else "One-time"})")
                    Thread.sleep(60000) // Sleep for 1 minute
                } catch (e: InterruptedException) {
                    Log.e("BackgroundService", "Thread interrupted", e)
                    break
                }
            }
        }.start()
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
        releaseWakeLock()
        
        if (isPersistentMode) {
            // Restart the service if it gets destroyed in persistent mode
            val intent = Intent(applicationContext, BackgroundService::class.java).apply {
                putExtra(EXTRA_IS_PERSISTENT, true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
} 