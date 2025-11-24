package com.intern001.dating.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.intern001.dating.MainActivity
import com.intern001.dating.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HeartOnMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationService: NotificationService

    companion object {
        private const val TAG = "HeartOnMessagingService"
        private const val CHANNEL_ID_LIKES = "likes"
        private const val CHANNEL_ID_MATCHES = "matches"
        private const val CHANNEL_ID_DEFAULT = "hearton_notifications"
        private const val CHANNEL_NAME_DEFAULT = "HeartOn Notifications"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message has data payload
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }

        // Check if message has notification payload
        remoteMessage.notification?.let {
            // If we already handled data message, don't send notification again
            if (remoteMessage.data.isEmpty()) {
                sendNotification(it.title, it.body, remoteMessage.data)
            }
        }
    }

    override fun onNewToken(token: String) {
        // New token created, need to send to server
        notificationService.onTokenRefresh(token)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]

        when (type) {
            "like" -> {
                val likerName = data["likerName"] ?: "Someone"
                sendNotification(
                    title = "New Like!",
                    message = "$likerName liked you",
                    data = data,
                )
            }
            "match" -> {
                val matchedUserName = data["matchedUserName"] ?: "Someone"
                sendNotification(
                    title = "It's a Match! 💕",
                    message = "You and $matchedUserName liked each other",
                    data = data,
                )
            }
            else -> {
                val title = data["title"] ?: "New Notification"
                val message = data["message"] ?: "You have a new notification"
                sendNotification(title, message, data)
            }
        }
    }

    private fun sendNotification(
        title: String?,
        message: String?,
        data: Map<String, String> = emptyMap(),
    ) {
        val type = data["type"]

        // Determine channel ID based on notification type
        val channelId = when (type) {
            "like" -> CHANNEL_ID_LIKES
            "match" -> CHANNEL_ID_MATCHES
            else -> CHANNEL_ID_DEFAULT
        }

        // Create intent based on notification type
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type ?: "")

            when (type) {
                "like" -> {
                    data["likerId"]?.let { putExtra("likerId", it) }
                    putExtra("navigate_to", "notification") // Navigate to notification screen
                }
                "match" -> {
                    data["matchId"]?.let { putExtra("matchId", it) }
                    data["matchedUserId"]?.let { putExtra("matchedUserId", it) }
                    putExtra("navigate_to", "chat") // Navigate to chat with matched user
                }
            }

            // Add all data for reference
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        // Get unique notification ID based on type and ID
        val notificationId = getNotificationId(type, data)

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_filled)
            .setContentTitle(title ?: "HeartOn")
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // TODO: Load large icon (user photo) if available
        // data["likerPhotoUrl"]?.let { photoUrl ->
        //     // Use Coil/Glide to load image asynchronously
        // }
        // data["matchedUserPhotoUrl"]?.let { photoUrl ->
        //     // Load matched user photo
        // }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification", e)
        }
    }

    /**
     * Get unique notification ID based on notification type and ID
     * This ensures same notification (same liker/match) replaces previous one
     */
    private fun getNotificationId(type: String?, data: Map<String, String>): Int {
        return when (type) {
            "like" -> {
                // Use likerId hash to ensure same liker replaces previous notification
                data["likerId"]?.hashCode()?.let {
                    // Ensure positive integer
                    if (it < 0) -it else it
                } ?: System.currentTimeMillis().toInt()
            }
            "match" -> {
                // Use matchId hash
                data["matchId"]?.hashCode()?.let {
                    if (it < 0) -it else it
                } ?: System.currentTimeMillis().toInt()
            }
            else -> System.currentTimeMillis().toInt()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Likes channel
            val likesChannel = NotificationChannel(
                CHANNEL_ID_LIKES,
                "Likes",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for new likes"
                enableVibration(true)
                enableLights(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            // Matches channel
            val matchesChannel = NotificationChannel(
                CHANNEL_ID_MATCHES,
                "Matches",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for new matches"
                enableVibration(true)
                enableLights(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            // Default channel
            val defaultChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                CHANNEL_NAME_DEFAULT,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications from HeartOn Dating App"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            try {
                notificationManager.createNotificationChannel(likesChannel)
                notificationManager.createNotificationChannel(matchesChannel)
                notificationManager.createNotificationChannel(defaultChannel)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create notification channels", e)
            }
        }
    }
}
