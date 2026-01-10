package com.tapme.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.tapme.app.utils.PreferencesManager
import android.util.Log
import com.tapme.app.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.provider.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var preferencesManager: PreferencesManager
    
    private val tokenInvalidReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.tapme.app.ACTION_TOKEN_INVALID") {
                Log.d("MainActivity", "Received token invalid broadcast. Re-registering...")
                registerUser()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesManager = PreferencesManager(this)
        
        // Initialize RetrofitClient with context
        RetrofitClient.init(applicationContext)

        // Register broadcast receiver for token invalid events
        val filter = IntentFilter("com.tapme.app.ACTION_TOKEN_INVALID")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(tokenInvalidReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(tokenInvalidReceiver, filter)
        }

        // Register/Login user on app start
        registerUser()

        // Setup navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setupWithNavController(navController)
        
        // Clear badge when app opens (user has seen taps)
        com.tapme.app.utils.BadgeManager.getInstance(this).clearUnreadCount()
        
        // Handle deep links (tapme://invite?code=@ABC123)
        handleDeepLink(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Check if token was cleared (e.g., due to 401 error) and re-register if needed
        if (preferencesManager.getToken() == null) {
            Log.d("MainActivity", "Token missing on resume. Re-registering...")
            registerUser()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(tokenInvalidReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered, ignore
        }
    }
    
    private fun handleDeepLink(intent: Intent) {
        val data = intent.data
        if (data != null && (data.scheme == "tapme" || data.host == "tapme.app")) {
            val shareHelper = com.tapme.app.utils.ShareHelper(this)
            val userCode = shareHelper.parseUserCodeFromDeepLink(data)
            
            if (userCode != null) {
                // Store code temporarily for WhitelistFragment to pick up
                preferencesManager.saveTempInviteCode(userCode)
                
                // Navigate to WhitelistFragment
                try {
                    navController.navigate(R.id.whitelistFragment)
                } catch (e: Exception) {
                    // Navigation might fail if not ready, fragment will check on load
                }
                
                android.widget.Toast.makeText(
                    this,
                    "Tap 'Add to Allowed Tappers' to add $userCode",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun registerUser() {
        // Get or generate device ID
        val deviceId = preferencesManager.getDeviceId() 
            ?: Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: "device-${System.currentTimeMillis()}"
        
        // Save device ID
        preferencesManager.saveDeviceId(deviceId)

        // Check if already registered (has token)
        val existingToken = preferencesManager.getToken()
        if (existingToken != null) {
            Log.d("MainActivity", "User already registered. Updating FCM token if available.")
            // Just update FCM token if available
            updateFcmToken(deviceId)
            return
        }
        
        // No token - need to register
        Log.d("MainActivity", "No token found. Registering user...")

        // Get FCM token (if Firebase is available) before registering
        try {
            com.google.firebase.FirebaseApp.getInstance()
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    Log.d("MainActivity", "FCM Registration Token: $fcmToken")
                    performRegistration(deviceId, fcmToken)
                } else {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                    // Register without FCM token
                    performRegistration(deviceId, null)
                }
            }
        } catch (e: IllegalStateException) {
            Log.w("MainActivity", "Firebase not initialized. Registering without FCM token.")
            performRegistration(deviceId, null)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing Firebase", e)
            performRegistration(deviceId, null)
        }
    }

    private fun updateFcmToken(deviceId: String) {
        try {
            com.google.firebase.FirebaseApp.getInstance()
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    Log.d("MainActivity", "FCM token updated: $fcmToken")
                    // Optionally update FCM token on backend (can be done later)
                }
            }
        } catch (e: Exception) {
            // Ignore FCM update errors
        }
    }

    private fun performRegistration(deviceId: String, fcmToken: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.register(
                    com.tapme.app.data.remote.RegisterRequest(
                        deviceId = deviceId,
                        fcmToken = fcmToken
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val authResponse = response.body()!!
                    // Save auth token
                    preferencesManager.saveToken(authResponse.token)
                    // Save user ID
                    preferencesManager.saveUserId(authResponse.user.id)
                    Log.d("MainActivity", "✅ User registered successfully. Token saved.")
                } else {
                    Log.e("MainActivity", "Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Registration error", e)
            }
        }
    }
}
