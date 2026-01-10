package com.tapme.app.ui.incoming

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tapme.app.R
import com.tapme.app.data.remote.RetrofitClient
import com.tapme.app.domain.models.TapType
import com.tapme.app.utils.PreferencesManager
import com.tapme.app.utils.SoundManager
import kotlinx.coroutines.launch

class IncomingTapActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private var fromUserId: String? = null
    private var tapType: TapType? = null
    private var customEmoji: String? = null
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make it full screen like incoming call
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_incoming_tap)
        
        preferencesManager = PreferencesManager(this)
        
        // Get data from intent
        fromUserId = intent.getStringExtra(EXTRA_FROM_USER_ID)
        val tapTypeStr = intent.getStringExtra(EXTRA_TAP_TYPE)
        customEmoji = intent.getStringExtra(EXTRA_CUSTOM_EMOJI)
        message = intent.getStringExtra(EXTRA_MESSAGE)
        tapType = tapTypeStr?.let { TapType.fromString(it) }
        
        setupUI()
        setupButtons()
        setupBackPressHandler()
        
        // Play sound for tap type (if not in silent mode)
        tapType?.let {
            val silentModeManager = com.tapme.app.utils.SilentModeManager(this)
            if (!silentModeManager.isSilentModeActive()) {
                SoundManager.getInstance(this).playSound(it)
            }
        }
    }
    
    private fun setupBackPressHandler() {
        // Prevent back button from dismissing (like incoming call)
        // User must tap Dismiss button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - back button is disabled
            }
        })
    }

    private fun setupUI() {
        // Check if custom emoji is provided
        val isCustomEmoji = !customEmoji.isNullOrBlank()
        
        // Set gradient background based on tap type/mood
        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)
        val gradientDrawableRes = if (isCustomEmoji) {
            // Cool gradient for custom emoji (purple/blue)
            R.drawable.bg_gradient_default
        } else {
            // Get gradient based on tap type
            when (tapType) {
                TapType.HAPPY_MISS -> R.drawable.bg_gradient_happy
                TapType.SAD_MISS -> R.drawable.bg_gradient_sad
                TapType.NAUGHTY_MISS -> R.drawable.bg_gradient_naughty
                TapType.LOVING_MISS -> R.drawable.bg_gradient_loving
                TapType.EXCITED_MISS -> R.drawable.bg_gradient_excited
                TapType.SLEEPY_MISS -> R.drawable.bg_gradient_sleepy
                TapType.PLAYFUL_MISS -> R.drawable.bg_gradient_playful
                TapType.THINKING_MISS -> R.drawable.bg_gradient_thinking
                null -> R.drawable.bg_gradient_default
            }
        }
        
        // Apply gradient background drawable
        rootLayout.background = androidx.core.content.ContextCompat.getDrawable(this, gradientDrawableRes)
        
        // Get representative background color for text color calculation (use first gradient color)
        // This is used to determine if text should be light or dark
        val backgroundColor = when (gradientDrawableRes) {
            R.drawable.bg_gradient_happy -> android.graphics.Color.parseColor("#f6d365") // Gold - light
            R.drawable.bg_gradient_sad -> android.graphics.Color.parseColor("#4facfe") // Blue - light
            R.drawable.bg_gradient_naughty -> android.graphics.Color.parseColor("#ff0844") // Red - dark
            R.drawable.bg_gradient_loving -> android.graphics.Color.parseColor("#ff9a9e") // Pink - light
            R.drawable.bg_gradient_excited -> android.graphics.Color.parseColor("#ffa726") // Orange - light
            R.drawable.bg_gradient_sleepy -> android.graphics.Color.parseColor("#667eea") // Purple - dark
            R.drawable.bg_gradient_playful -> android.graphics.Color.parseColor("#00c9ff") // Turquoise - light
            R.drawable.bg_gradient_thinking -> android.graphics.Color.parseColor("#868f96") // Gray - dark
            else -> android.graphics.Color.parseColor("#667eea") // Default purple - dark
        }
        
        // Gentle fade-in animation
        rootLayout.alpha = 0f
        rootLayout.animate()
            .alpha(1.0f)
            .setDuration(300)
            .start()
        
        // Animate emoji appearance with celebration
        val emojiText = findViewById<android.widget.TextView>(R.id.emojiText)
        emojiText.scaleX = 0f
        emojiText.scaleY = 0f
        emojiText.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction {
                emojiText.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .setInterpolator(android.view.animation.OvershootInterpolator())
                    .start()
            }
            .start()
        
        // Determine text color based on background brightness
        // Use white text for dark backgrounds, dark text for light backgrounds
        val textColor = if (isLightColor(backgroundColor)) {
            Color.parseColor("#000000") // Black for light backgrounds
        } else {
            Color.parseColor("#FFFFFF") // White for dark backgrounds
        }
        
        // Show brief celebration message inline
        showCelebrationMessage(textColor)
        
        // Set emoji and message based on tap type or custom emoji
        if (isCustomEmoji) {
            findViewById<android.widget.TextView>(R.id.emojiText).text = customEmoji
            findViewById<android.widget.TextView>(R.id.titleText).apply {
                text = "You were missed $customEmoji"
                setTextColor(textColor)
            }
            findViewById<android.widget.TextView>(R.id.messageText).apply {
                text = message ?: "Someone is thinking of you"
                setTextColor(textColor)
            }
        } else {
            findViewById<android.widget.TextView>(R.id.emojiText).text = tapType?.emoji ?: "❤️"
            findViewById<android.widget.TextView>(R.id.titleText).apply {
                text = tapType?.notificationTitle ?: "You were missed ❤️"
                setTextColor(textColor)
            }
            findViewById<android.widget.TextView>(R.id.messageText).apply {
                text = message ?: (tapType?.notificationBody ?: "Someone is thinking of you")
                setTextColor(textColor)
            }
        }
        
        // Keep caller identity anonymous for mystery and intrigue
    }
    
    private fun showCelebrationMessage(textColor: Int) {
        // Create personalized celebration message
        val celebrationMessages = listOf(
            "You're special! ❤️",
            "You were missed! ❤️",
            "You're the first person they thought of today! ❤️",
            "You're loved! ❤️"
        )
        val message = celebrationMessages.random()
        
        val celebrationTextView = findViewById<android.widget.TextView>(R.id.celebrationMessage)
        celebrationTextView.text = message
        celebrationTextView.setTextColor(textColor)
        celebrationTextView.visibility = android.view.View.VISIBLE
        
        // Animate celebration: fade in, hold, fade out
        celebrationTextView.alpha = 0f
        celebrationTextView.animate()
            .alpha(1.0f)
            .setDuration(400)
            .withEndAction {
                celebrationTextView.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .setStartDelay(2000) // Show for 2 seconds
                    .withEndAction {
                        celebrationTextView.visibility = android.view.View.GONE
                    }
                    .start()
            }
            .start()
    }
    
    private fun isLightColor(color: Int): Boolean {
        // Calculate luminance to determine if color is light or dark
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        
        // Using relative luminance formula
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255.0
        return luminance > 0.5 // Light if luminance > 50%
    }

    private fun setupButtons() {
        // Miss You Too button
        findViewById<android.widget.Button>(R.id.btnMissYouToo).setOnClickListener {
            sendReply()
        }
        
        // Dismiss button
        findViewById<android.widget.Button>(R.id.btnDismiss).setOnClickListener {
            finish()
        }
        
        // Optional reaction buttons
        val reactionContainer = findViewById<android.widget.LinearLayout>(R.id.reactionContainer)
        val btnReactionHeart = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReactionHeart)
        val btnReactionSmile = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReactionSmile)
        val btnReactionLaugh = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReactionLaugh)
        
        // Show reactions (optional feature - user can choose to react or not)
        reactionContainer.visibility = android.view.View.VISIBLE
        
        btnReactionHeart?.setOnClickListener {
            sendReaction("❤️")
        }
        
        btnReactionSmile?.setOnClickListener {
            sendReaction("😊")
        }
        
        btnReactionLaugh?.setOnClickListener {
            sendReaction("😂")
        }
    }
    
    private fun sendReaction(emoji: String) {
        // TODO: Implement reaction API call
        // For now, just show a toast and close
        Toast.makeText(this, "Reacted with $emoji", Toast.LENGTH_SHORT).show()
        finish()
    }
    

    private fun sendReply() {
        val fromUserId = this.fromUserId
        if (fromUserId == null) {
            Toast.makeText(this, "Error: Sender not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val token = preferencesManager.getToken()
        if (token == null) {
            Toast.makeText(this, "Not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // Get allowed tappers to find the sender's user code
                val tappersResponse = RetrofitClient.apiService.getAllowedTappers("Bearer $token")
                if (!tappersResponse.isSuccessful || tappersResponse.body()?.success != true) {
                    if (tappersResponse.code() == 429) {
                        Toast.makeText(this@IncomingTapActivity, "Too many requests. Please try again later.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@IncomingTapActivity, "Could not find sender", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                    return@launch
                }

                val sender = tappersResponse.body()!!.allowedTappers.find { it.tapperUserId == fromUserId }
                if (sender == null) {
                    Toast.makeText(this@IncomingTapActivity, "Sender not in your allowed list", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // Send a "Loving Miss" tap back as reply
                val response = RetrofitClient.apiService.sendTap(
                    "Bearer $token",
                    com.tapme.app.data.remote.SendTapRequest(
                        toUserCode = sender.tapperUserCode,
                        tapType = TapType.LOVING_MISS.name
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@IncomingTapActivity, "Miss you too ❤️", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@IncomingTapActivity, "Failed to send reply", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@IncomingTapActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        const val EXTRA_FROM_USER_ID = "extra_from_user_id"
        const val EXTRA_TAP_TYPE = "extra_tap_type"
        const val EXTRA_CUSTOM_EMOJI = "extra_custom_emoji"
        const val EXTRA_MESSAGE = "extra_message"
        
        fun createIntent(
            context: android.content.Context,
            fromUserId: String,
            tapType: String,
            customEmoji: String? = null,
            message: String? = null
        ): Intent {
            return Intent(context, IncomingTapActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_FROM_USER_ID, fromUserId)
                putExtra(EXTRA_TAP_TYPE, tapType)
                putExtra(EXTRA_CUSTOM_EMOJI, customEmoji)
                putExtra(EXTRA_MESSAGE, message)
            }
        }
    }
}
