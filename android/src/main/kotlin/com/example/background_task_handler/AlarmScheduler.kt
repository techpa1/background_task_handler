package com.example.background_task_handler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val intent = Intent(context, AlarmReceiver::class.java).apply {
        action = "com.example.background_task_handler.ALARM_TRIGGER"
    }

    fun scheduleAlarm(intervalInSeconds: Long = 60) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    scheduleExactAlarm(pendingIntent, intervalInSeconds)
                } else {
                    Log.w("AlarmScheduler", "Cannot schedule exact alarms")
                    scheduleInexactAlarm(pendingIntent, intervalInSeconds)
                }
            } else {
                scheduleExactAlarm(pendingIntent, intervalInSeconds)
            }
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Security exception while scheduling alarm: ${e.message}")
        }
    }

    private fun scheduleExactAlarm(pendingIntent: PendingIntent, intervalInSeconds: Long) {
        val triggerTime = System.currentTimeMillis() + (intervalInSeconds * 1000)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun scheduleInexactAlarm(pendingIntent: PendingIntent, intervalInSeconds: Long) {
        val triggerTime = System.currentTimeMillis() + (intervalInSeconds * 1000)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancelAlarm() {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
} 