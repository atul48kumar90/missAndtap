package com.tapme.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tapme.app.domain.models.TapType
import com.tapme.app.ui.incoming.IncomingTapActivity
import com.tapme.app.utils.AppStateManager
import com.tapme.app.utils.SilentModeManager
import com.tapme.app.utils.SoundManager
import com.tapme.app.utils.BadgeManager

class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data, remoteMessage.notification)
        } else {
            // Show notification if no data payload
            remoteMessage.notification?.let {
                showNotification(it.title ?: "Tap Me", it.body ?: "", null)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // Send token to your backend server
        sendRegistrationToServer(token)
    }

    private fun handleDataMessage(data: Map<String, String>, notification: RemoteMessage.Notification?) {
        val type = data["type"]
        val toUserId = data["toUserId"] // Recipient user ID (current user)
        val fromUserId = data["fromUserId"] // Sender user ID
        val tapTypeStr = data["tapType"]
        val customEmoji = data["customEmoji"]
        val message = data["message"]
        val emoji = data["emoji"] ?: "❤️"
        
        when (type) {
            "tap" -> {
                // Check if custom emoji is provided
                val isCustomEmoji = !customEmoji.isNullOrBlank()
                
                // Get tap type (or use custom emoji)
                val tapType = if (isCustomEmoji) {
                    null // Custom emoji, no predefined type
                } else {
                    tapTypeStr?.let { TapType.fromString(it) } ?: TapType.LOVING_MISS
                }
                
                // Check if app is in foreground (online)
                val isAppForeground = AppStateManager.getInstance().isAppForeground()
                
                // Check silent mode
                val silentModeManager = SilentModeManager(this)
                val isSilentMode = silentModeManager.isSilentModeActive()
                
                if (isAppForeground && fromUserId != null) {
                    // App is in foreground - show full-screen like incoming call
                    // Play sound if not in silent mode
                    if (!isSilentMode && tapType != null) {
                        SoundManager.getInstance(this).playSound(tapType)
                    }
                    showFullScreenTap(fromUserId, tapTypeStr ?: "", customEmoji, message)
                } else {
                    // App is in background - show normal notification (silent if in silent mode)
                    if (!isSilentMode) {
                        if (tapType != null) {
                            vibrate(tapType)
                        } else {
                            // Default vibration for custom emoji
                            vibrate(TapType.LOVING_MISS)
                        }
                    }
                    
                    val title = if (isCustomEmoji) {
                        notification?.title ?: "You were missed $customEmoji"
                    } else {
                        notification?.title ?: tapType?.notificationTitle ?: "You were missed ❤️"
                    }
                    
                    // Use personalized message from notification (backend calculates it)
                    val body = notification?.body ?: if (message != null && message.isNotEmpty()) {
                        message
                    } else if (isCustomEmoji) {
                        "Someone is thinking of you"
                    } else {
                        tapType?.notificationBody ?: "Someone is thinking of you"
                    }
                    
                    // Show notification (will be silent if in silent mode)
                    showNotification(title, body, tapType, customEmoji, isSilentMode)
                    
                    // Increment badge count (creates FOMO)
                    BadgeManager.getInstance(this).incrementUnreadCount()
                }
                
                Log.d(TAG, "Received ${if (isCustomEmoji) "custom emoji" else tapType?.displayName ?: "tap"} notification from user: $fromUserId (foreground: $isAppForeground)")
            }
        }
    }
    
    private fun showFullScreenTap(fromUserId: String, tapType: String, customEmoji: String?, message: String?) {
        val intent = IncomingTapActivity.createIntent(
            this,
            fromUserId,
            tapType,
            customEmoji,
            message
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        
        // Also vibrate for full-screen
        val tapTypeEnum = if (customEmoji != null) {
            TapType.LOVING_MISS // Default vibration for custom emoji
        } else {
            TapType.fromString(tapType) ?: TapType.LOVING_MISS
        }
        vibrate(tapTypeEnum)
    }
    
    private fun showNotification(title: String, body: String, tapType: TapType?, customEmoji: String?, isSilent: Boolean = false) {
        val channelId = "tap_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Create notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tap Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when someone taps you"
                enableVibration(true)
                vibrationPattern = tapType?.vibrationPattern ?: TapType.LOVING_MISS.vibrationPattern
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // You can add custom icon later
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(if (isSilent) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
        
        // Only add sound and vibration if not in silent mode
        if (!isSilent) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder
                .setSound(defaultSoundUri)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
            
            // Add vibration pattern if available
            val vibrationPattern = tapType?.vibrationPattern ?: TapType.LOVING_MISS.vibrationPattern
            notificationBuilder.setVibrate(vibrationPattern)
        } else {
            // Silent mode: no sound, no vibration
            notificationBuilder.setDefaults(0)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun vibrate(tapType: TapType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                tapType.vibrationPattern,
                -1 // Don't repeat
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(tapType.vibrationPattern, -1)
        }
    }

    private fun showNotification(title: String, body: String, tapType: TapType?) {
        val channelId = "tap_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Create notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tap Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when someone taps you"
                enableVibration(true)
                vibrationPattern = tapType?.vibrationPattern
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // You can add custom icon later
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Add vibration pattern if available
        tapType?.let {
            notificationBuilder.setVibrate(it.vibrationPattern)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun sendRegistrationToServer(token: String) {
        // Send token to your backend
        // This should be called when token is refreshed
        Log.d(TAG, "Sending token to server: $token")
    }

    companion object {
        private const val TAG = "FcmService"
    }
}
