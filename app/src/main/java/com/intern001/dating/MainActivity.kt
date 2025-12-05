package com.intern001.dating

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.intern001.dating.data.billing.BillingManager
import com.intern001.dating.data.firebase.FirebaseConfigHelper
import com.intern001.dating.data.local.TokenManager
import com.intern001.dating.databinding.ActivityMainBinding
import com.intern001.dating.domain.model.Notification
import com.intern001.dating.domain.usecase.notification.SaveNotificationUseCase
import com.intern001.dating.presentation.common.viewmodel.BaseActivity
import com.intern001.dating.presentation.common.viewmodel.ChatListViewModel
import com.intern001.dating.presentation.navigation.navigateToChatDetail
import com.intern001.dating.presentation.navigation.navigateToDatingMode
import com.intern001.dating.presentation.navigation.navigateToNotification
import com.intern001.dating.presentation.navigation.navigateToProfileDetail
import com.intern001.dating.presentation.ui.chat.ChatSharedViewModel
import com.intern001.dating.presentation.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var saveNotificationUseCase: SaveNotificationUseCase

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var binding: ActivityMainBinding
    private var isNavigatingProgrammatically = false
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val chatListViewModel: ChatListViewModel by viewModels()
    private val chatSharedViewModel: ChatSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        setupNavigation()
        setupBottomNavigation()
        setupDestinationListener()
        initializePremiumFirebaseIfNeeded()

        preloadChatListData()

        activityScope.launch {
            delay(100)
            var handled = handleNotificationIntent(intent)
            if (!handled) {
                handled = handleNotificationAction(intent)
            }
            if (!handled) {
                handleDeepLink(intent)
            }
        }

        if (intent.getBooleanExtra("open_home", false)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment())
                .commit()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED -> {
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001,
                    )
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001,
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                // Permission result handled
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        activityScope.launch {
            delay(100)
            var handled = handleNotificationIntent(intent)
            if (!handled) {
                handled = handleNotificationAction(intent)
            }
            if (!handled) {
                handleDeepLink(intent)
            }
        }
    }

    private fun handleNotificationAction(intent: Intent?): Boolean {
        val action = intent?.action ?: return false

        if (!action.startsWith("com.intern001.dating.")) {
            return false
        }

        saveNotificationFromIntent(intent)

        val type = intent.getStringExtra("type")
        val likerId = intent.getStringExtra("likerId")
        val matchedUserId = intent.getStringExtra("matchedUserId")
        val navigateTo = intent.getStringExtra("navigate_to")

        return when (action) {
            "com.intern001.dating.OPEN_DATING_MODE" -> {
                val userId = likerId ?: intent.getStringExtra("userId")
                activityScope.launch {
                    delay(200)
                    try {
                        navController.navigateToDatingMode(userId)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to navigate to dating mode", e)
                    }
                }
                true
            }
            "com.intern001.dating.OPEN_PROFILE" -> {
                val userId = likerId ?: intent.getStringExtra("userId")
                if (!userId.isNullOrBlank()) {
                    activityScope.launch {
                        delay(200)
                        try {
                            navController.navigateToProfileDetail(userId)
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Failed to navigate to profile detail", e)
                        }
                    }
                    true
                } else {
                    false
                }
            }
            "com.intern001.dating.OPEN_CHAT" -> {
                val userId = matchedUserId ?: intent.getStringExtra("userId")
                if (!userId.isNullOrBlank()) {
                    activityScope.launch {
                        delay(200)
                        try {
                            navController.navigateToChatDetail(userId)
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Failed to navigate to chat detail", e)
                        }
                    }
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        if (action != null && action.startsWith("datingapp://")) {
            try {
                val uri = android.net.Uri.parse(action)
                if (uri.scheme == "datingapp" && uri.host == "dating") {
                    val userId = uri.pathSegments?.firstOrNull()
                    if (!userId.isNullOrBlank()) {
                        activityScope.launch {
                            delay(200)
                            try {
                                navController.navigateToProfileDetail(userId)
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to navigate to profile detail from deep link", e)
                            }
                        }
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to parse deep link from action: $action", e)
            }
        }

        val data = intent.data
        if (data != null && data.scheme == "datingapp" && data.host == "dating") {
            val userId = data.pathSegments?.firstOrNull()
            if (!userId.isNullOrBlank()) {
                activityScope.launch {
                    delay(200)
                    try {
                        navController.navigateToProfileDetail(userId)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to navigate to profile detail from deep link", e)
                    }
                }
            }
        }
    }

    private fun handleNotificationIntent(intent: Intent?): Boolean {
        val notificationType = intent?.getStringExtra("notification_type")
        val navigateTo = intent?.getStringExtra("navigate_to")

        if (notificationType.isNullOrEmpty()) {
            return false
        }

        saveNotificationFromIntent(intent)

        try {
            when (notificationType) {
                "like", "superlike" -> {
                    val likerId = intent?.getStringExtra("likerId")
                    when (navigateTo) {
                        "dating_mode" -> {
                            navController.navigateToDatingMode(likerId)
                        }
                        "profile" -> {
                            likerId?.let {
                                navController.navigateToProfileDetail(it)
                            } ?: run {
                                navController.navigateToNotification()
                            }
                        }
                        "notification" -> {
                            navController.navigateToNotification()
                        }
                        else -> {
                            likerId?.let {
                                navController.navigateToProfileDetail(it)
                            } ?: run {
                                navController.navigateToNotification()
                            }
                        }
                    }
                }
                "match" -> {
                    val matchId = intent?.getStringExtra("matchId")
                    val matchedUserId = intent?.getStringExtra("matchedUserId")
                    when (navigateTo) {
                        "chat" -> {
                            matchedUserId?.let {
                                navController.navigateToChatDetail(it)
                            } ?: run {
                                navController.navigate(R.id.chatListFragment)
                            }
                        }
                        "match_detail" -> {
                            matchedUserId?.let {
                                navController.navigateToChatDetail(it)
                            } ?: run {
                                navController.navigate(R.id.chatListFragment)
                            }
                        }
                        else -> {
                            matchedUserId?.let {
                                navController.navigateToChatDetail(it)
                            } ?: run {
                                navController.navigate(R.id.chatListFragment)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error handling notification navigation", e)
            try {
                navController.navigateToNotification()
            } catch (ex: Exception) {
                Log.e("MainActivity", "Failed to navigate to notification screen", ex)
            }
        }
        return true
    }

    private fun saveNotificationFromIntent(intent: Intent?) {
        val notificationType = intent?.getStringExtra("notification_type") ?: return
        val title = intent?.getStringExtra("title")
        val message = intent?.getStringExtra("message")

        val data = mutableMapOf<String, String>()
        intent?.extras?.keySet()?.forEach { key ->
            intent.getStringExtra(key)?.let { value ->
                data[key] = value
            }
        }
        data["type"] = notificationType

        activityScope.launch(Dispatchers.IO) {
            try {
                val type = data["type"]
                val notificationType = when (type) {
                    "like" -> Notification.NotificationType.LIKE
                    "superlike" -> Notification.NotificationType.SUPERLIKE
                    "match" -> Notification.NotificationType.MATCH
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
                    else -> Notification.NotificationIconType.SETTINGS
                }

                val finalTitle = title ?: when (type) {
                    "like" -> "New Like!"
                    "superlike" -> "New Super Like!"
                    "match" -> "It's a Match!"
                    else -> "New Notification"
                }

                val finalMessage = message ?: when (type) {
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
                    else -> "You have a new notification"
                }

                val actionData = Notification.NotificationActionData(
                    navigateTo = when (type) {
                        "like", "superlike" -> data["navigate_to"] ?: "notification"
                        "match" -> "chat"
                        else -> null
                    },
                    userId = when (type) {
                        "match" -> data["matchedUserId"] ?: data["userId"]
                        else -> data["userId"]
                    },
                    matchId = data["matchId"],
                    likerId = data["likerId"],
                    extraData = data,
                )

                val notificationId = "notif_${System.currentTimeMillis()}_${type ?: "other"}"

                val notification = Notification(
                    id = notificationId,
                    type = notificationType,
                    title = finalTitle,
                    message = finalMessage,
                    timestamp = Date(),
                    isRead = false,
                    iconType = iconType,
                    actionData = actionData,
                )

                saveNotificationUseCase(notification)
                    .onFailure { error ->
                        Log.e("MainActivity", "Failed to save notification from intent: ${error.message}", error)
                    }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to save notification from intent", e)
            }
        }
    }

    override fun getNavHostFragmentId() = R.id.nav_host_fragment

    private fun setupBottomNavigation() {
        binding.bottomNavigation.visibility = View.GONE

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (isNavigatingProgrammatically) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.homeFragment,
                R.id.chatListFragment,
                R.id.notificationFragment,
                R.id.profileFragment,
                -> {
                    navController.navigate(item.itemId)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDestinationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNavDestinations = setOf(
                R.id.homeFragment,
                R.id.chatListFragment,
                R.id.notificationFragment,
                R.id.profileFragment,
            )

            val shouldShow = showBottomNavDestinations.contains(destination.id)
            binding.bottomNavigation.visibility = if (shouldShow) View.VISIBLE else View.GONE

            if (shouldShow) {
                isNavigatingProgrammatically = true
                binding.bottomNavigation.selectedItemId = destination.id
                isNavigatingProgrammatically = false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun hideBottomNavigation(hide: Boolean) {
        binding.bottomNavigation.visibility = if (hide) View.GONE else View.VISIBLE
    }

    private fun initializePremiumFirebaseIfNeeded() {
        activityScope.launch(Dispatchers.IO) {
            try {
                delay(2000)
                if (billingManager.hasActiveSubscription()) {
                    FirebaseConfigHelper.initializeFirebaseForPremium(this@MainActivity)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to initialize premium Firebase on startup", e)
            }
        }
    }

    /**
     * Preload chat list data (matches và conversations) khi app khởi động
     */
    private fun preloadChatListData() {
        activityScope.launch(Dispatchers.IO) {
            try {
                // Đợi một chút để đảm bảo token đã sẵn sàng
                delay(500)

                val token = tokenManager.getAccessTokenAsync()
                if (token != null) {
                    Log.d("MainActivity", "Preloading chat list data...")

                    chatListViewModel.preloadMatches()

                    var attempts = 0
                    while (attempts < 30 && chatListViewModel.matches.value.isEmpty() && chatListViewModel.isLoading.value) {
                        delay(100)
                        attempts++
                    }

                    val matches = chatListViewModel.matches.value
                    if (matches.isNotEmpty()) {
                        Log.d("MainActivity", "Matches loaded, preloading messages for ${matches.size} matches...")

                        matches.take(5).forEachIndexed { index, match ->
                            chatSharedViewModel.preloadMessages(match.matchId)
                            if (index < 4) {
                                delay(200)
                            }
                        }

                        Log.d("MainActivity", "Chat list data preloaded successfully")
                    } else {
                        Log.d("MainActivity", "No matches found or still loading")
                    }
                } else {
                    Log.d("MainActivity", "No token available, skipping chat list preload")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to preload chat list data", e)
            }
        }
    }
}
