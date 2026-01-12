package com.tapme.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest

object PermissionHelper {
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            // Android 12 and below: notifications are enabled by default
            true
        }
    }
    
    /**
     * Request notification permission (Android 13+)
     * Returns true if permission request was triggered, false if already granted or not needed
     */
    fun requestNotificationPermission(activity: androidx.appcompat.app.AppCompatActivity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!areNotificationsEnabled(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
                true
            } else {
                false // Already granted
            }
        } else {
            false // Not needed on Android 12 and below
        }
    }
    
    /**
     * Open app notification settings
     */
    fun openNotificationSettings(context: Context) {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${context.packageName}")
                }
            }
        }
        context.startActivity(intent)
    }
    
    /**
     * Open battery optimization settings
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app info
            openAppSettings(context)
        }
    }
    
    /**
     * Request battery optimization exemption
     */
    fun requestBatteryOptimizationExemption(activity: androidx.appcompat.app.AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                // Some devices don't support this intent
                openBatteryOptimizationSettings(activity)
            }
        }
    }
    
    /**
     * Open full-screen intent permission settings (Android 10+)
     */
    fun openFullScreenIntentSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to app settings
            openAppSettings(context)
        }
    }
    
    /**
     * Open app settings
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
    
    const val REQUEST_NOTIFICATION_PERMISSION = 1001
}
