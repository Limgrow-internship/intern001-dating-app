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
import com.intern001.dating.common.notification.resolveTargetUserId
import com.intern001.dating.data.local.NotificationSettingsStorage
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.domain.model.Notification
import com.intern001.dating.domain.usecase.notification.SaveNotificationUseCase
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HeartOnMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var saveNotificationUseCase: SaveNotificationUseCase

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var notificationSettingsStorage: NotificationSettingsStorage

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val defaultSettings: Map<String, Boolean> = mapOf(
        NotificationSettingsStorage.KEY_SHOW_NOTIFICATIONS to true,
        NotificationSettingsStorage.KEY_NEW_MATCH to true,
        NotificationSettingsStorage.KEY_NEW_MESSAGE to true,
        NotificationSettingsStorage.KEY_LIKES to false,
        NotificationSettingsStorage.KEY_DISCOVERY_SUGGESTED to false,
        NotificationSettingsStorage.KEY_DISCOVERY_NEARBY to false,
        NotificationSettingsStorage.KEY_APP_INFO_SUGGESTED to false,
        NotificationSettingsStorage.KEY_APP_INFO_NEARBY to false,
    )

    companion object {
        private const val TAG = "HeartOnMessagingService"
        private const val CHANNEL_ID_LIKES = "likes"
        private const val CHANNEL_ID_MATCHES = "matches"
        private const val CHANNEL_ID_DEFAULT = "hearton_notifications"
        private const val CHANNEL_NAME_DEFAULT = "HeartOn Notifications"

        const val ACTION_CHAT_MESSAGE = "com.intern001.dating.ACTION_CHAT_MESSAGE"
        const val EXTRA_MATCH_ID = "matchId"
        const val EXTRA_SENDER_ID = "senderId"
        const val EXTRA_SENDER_NAME = "senderName"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_TIMESTAMP = "timestamp"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!isNotificationEnabled(remoteMessage.data)) {
            Log.d(TAG, "Skipping notification due to user settings")
            return
        }

        if (remoteMessage.data.isNotEmpty()) {
            val shouldShow = shouldShowNotification(remoteMessage.data)

            if (shouldShow) {
                handleDataMessage(remoteMessage.data)
            } else {
                saveNotificationToLocalStorageOnly(remoteMessage.data)
            }
        }

        remoteMessage.notification?.let {
            if (remoteMessage.data.isEmpty()) {
                sendNotification(it.title, it.body, remoteMessage.data)
            }
        }
    }

    override fun onNewToken(token: String) {
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
                    title = "It's a Match!",
                    message = "You and $matchedUserName liked each other — now it's time to say hi. Start your first chat!",
                    data = data,
                )
            }
            "message", "chat_message" -> {
                val senderName = data["senderName"] ?: data["title"] ?: "New message"
                val body = data["message"] ?: "You have a new message"
                broadcastNewChatMessage(data)
                sendNotification(
                    title = senderName,
                    message = body,
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

        saveNotificationToLocalStorage(title, message, data)

        val channelId = when (type) {
            "like" -> CHANNEL_ID_LIKES
            "match" -> CHANNEL_ID_MATCHES
            else -> CHANNEL_ID_DEFAULT
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type ?: "")

            when (type) {
                "like", "superlike" -> {
                    data["likerId"]?.let { putExtra("likerId", it) }
                    // Use navigate_to from backend, fallback to "notification" if not provided
                    putExtra("navigate_to", data["navigate_to"] ?: "notification")
                }
                "match" -> {
                    data["matchId"]?.let { putExtra("matchId", it) }
                    data["matchedUserId"]?.let { putExtra("matchedUserId", it) }
                    // Use navigate_to from backend, fallback to "chat" if not provided
                    putExtra("navigate_to", data["navigate_to"] ?: "chat")
                }
            }

            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

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

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification", e)
        }
    }

    private fun saveNotificationToLocalStorageOnly(data: Map<String, String>) {
        val type = data["type"]
        val title = when (type) {
            "like" -> "New Like!"
            "superlike" -> "New Super Like!"
            "match" -> "It's a Match!"
            else -> data["title"] ?: "New Notification"
        }
        val message = when (type) {
            "like" -> {
                val likerName = data["likerName"] ?: "Someone"
                "$likerName liked you"
            }
            "superlike" -> {
                val likerName = data["likerName"] ?: "Someone"
                "$likerName super liked you"
            }
            "match" -> {
                val matchedUserName = data["matchedUserName"] ?: "Someone"
                "You and $matchedUserName liked each other — now it's time to say hi. Start your first chat!"
            }
            else -> data["message"] ?: "You have a new notification"
        }
        saveNotificationToLocalStorage(title, message, data)
    }

    private fun saveNotificationToLocalStorage(
        title: String?,
        message: String?,
        data: Map<String, String>,
    ) {
        serviceScope.launch {
            try {
                val type = data["type"]
                val notificationType = when (type) {
                    "like" -> Notification.NotificationType.LIKE
                    "superlike" -> Notification.NotificationType.SUPERLIKE
                    "match" -> Notification.NotificationType.MATCH
                    "message", "chat_message" -> Notification.NotificationType.OTHER
                    "verification_success" -> Notification.NotificationType.VERIFICATION_SUCCESS
                    "verification_failed" -> Notification.NotificationType.VERIFICATION_FAILED
                    "premium_upgrade" -> Notification.NotificationType.PREMIUM_UPGRADE
                    else -> Notification.NotificationType.OTHER
                }

                val iconType = when (notificationType) {
                    Notification.NotificationType.LIKE,
                    Notification.NotificationType.SUPERLIKE,
                    -> Notification.NotificationIconType.HEART
                    Notification.NotificationType.MATCH -> Notification.NotificationIconType.MATCH
                    else -> Notification.NotificationIconType.MATCH
                }

                val actionData = Notification.NotificationActionData(
                    navigateTo = when (type) {
                        "like", "superlike" -> data["navigate_to"] ?: "notification"
                        "match" -> "chat"
                        "message", "chat_message" -> "chat"
                        else -> null
                    },
                    userId = when (type) {
                        "match" -> data["matchedUserId"] ?: data["userId"]
                        "message", "chat_message" -> data["senderId"] ?: data["userId"]
                        else -> data["userId"]
                    },
                    matchId = data["matchId"],
                    likerId = data["likerId"],
                    extraData = data,
                )

                // Group by matchId to avoid spamming multiple system notifications for the same chat
                val notificationId = when {
                    data["matchId"] != null -> data["matchId"]!!.hashCode().let { if (it < 0) -it else it }
                    type != null -> (type.hashCode()).let { if (it < 0) -it else it }
                    else -> System.currentTimeMillis().toInt()
                }

                val notification = Notification(
                    id = notificationId.toString(),
                    type = notificationType,
                    title = title ?: "HeartOn",
                    message = message ?: "",
                    timestamp = Date(),
                    isRead = false,
                    iconType = iconType,
                    actionData = actionData,
                )

                saveNotificationUseCase(notification)
                    .onFailure { error ->
                        Log.e(TAG, "Failed to save notification: ${error.message}", error)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save notification to local storage", e)
            }
        }
    }

    private fun getNotificationId(type: String?, data: Map<String, String>): Int {
        return when (type) {
            "like" -> {
                data["likerId"]?.hashCode()?.let {
                    if (it < 0) -it else it
                } ?: System.currentTimeMillis().toInt()
            }
            "match" -> {
                data["matchId"]?.hashCode()?.let {
                    if (it < 0) -it else it
                } ?: System.currentTimeMillis().toInt()
            }
            "message", "chat_message" -> {
                data["matchId"]?.hashCode()?.let {
                    if (it < 0) -it else it
                } ?: System.currentTimeMillis().toInt()
            }
            else -> System.currentTimeMillis().toInt()
        }
    }

    private fun broadcastNewChatMessage(data: Map<String, String>) {
        val matchId = data["matchId"] ?: return
        val intent = Intent(ACTION_CHAT_MESSAGE).apply {
            putExtra(EXTRA_MATCH_ID, matchId)
            putExtra(EXTRA_SENDER_ID, data["senderId"])
            putExtra(EXTRA_SENDER_NAME, data["senderName"])
            putExtra(EXTRA_MESSAGE, data["message"])
            putExtra(EXTRA_TIMESTAMP, data["timestamp"])
        }
        try {
            sendBroadcast(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to broadcast chat message update", e)
        }
    }

    private fun shouldShowNotification(data: Map<String, String>): Boolean {
        val type = data["type"]
        val currentUserId = tokenManager.getUserId()
        val targetUserId = data.resolveTargetUserId()

        if (currentUserId.isNullOrBlank()) {
            Log.w(TAG, "No cached userId; allowing notification type=$type")
            return true
        }

        val normalizedCurrentUserId = currentUserId.trim()

        if (targetUserId.isNullOrBlank()) {
            return when (type) {
                "match" -> {
                    val matchedUserId = data["matchedUserId"] ?: data["userId"]
                    matchedUserId.isNullOrBlank() ||
                        matchedUserId.trim().equals(normalizedCurrentUserId, ignoreCase = true)
                }
                else -> true
            }
        }

        val shouldShow = targetUserId.equals(normalizedCurrentUserId, ignoreCase = true)

        if (!shouldShow) {
            Log.d(
                TAG,
                "Skipping notification type=$type because targetUserId=$targetUserId does not match currentUserId=$normalizedCurrentUserId",
            )
        }

        return shouldShow
    }

    private fun isNotificationEnabled(data: Map<String, String>): Boolean {
        val masterEnabled = getSettingOrDefault(NotificationSettingsStorage.KEY_SHOW_NOTIFICATIONS)
        if (!masterEnabled) return false

        val normalizedType = data["type"]?.trim()?.lowercase(Locale.ROOT)

        val keyForType = when (normalizedType) {
            "match" -> NotificationSettingsStorage.KEY_NEW_MATCH
            "message", "chat", "chat_message" -> NotificationSettingsStorage.KEY_NEW_MESSAGE
            "like", "superlike" -> NotificationSettingsStorage.KEY_LIKES
            else -> null
        }

        val typeEnabled = keyForType?.let { getSettingOrDefault(it) } ?: true
        return typeEnabled
    }

    private fun getSettingOrDefault(key: String): Boolean {
        return notificationSettingsStorage.getSetting(key)
            ?: defaultSettings[key]
            ?: true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

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
