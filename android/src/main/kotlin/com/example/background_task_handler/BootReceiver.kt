package com.example.background_task_handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if we should start the service in persistent mode
            val shouldStartPersistent = intent.getBooleanExtra(BackgroundService.EXTRA_IS_PERSISTENT, false)
            
            if (shouldStartPersistent) {
                Log.d("BootReceiver", "Device booted, starting service in persistent mode")
                val serviceIntent = Intent(context, BackgroundService::class.java).apply {
                    putExtra(BackgroundService.EXTRA_IS_PERSISTENT, true)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                Log.d("BootReceiver", "Device booted, but service is not in persistent mode")
            }
        }
    }
} 