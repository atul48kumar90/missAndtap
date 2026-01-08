package com.tapme.app.ui.incoming

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tapme.app.R
import com.tapme.app.data.remote.RetrofitClient
import com.tapme.app.domain.models.TapType
import com.tapme.app.utils.PreferencesManager
import kotlinx.coroutines.launch

class IncomingTapActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private var pairId: String? = null
    private var fromUserId: String? = null
    private var tapType: TapType? = null
    private var customEmoji: String? = null
    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make it full screen like incoming call
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_incoming_tap)
        
        preferencesManager = PreferencesManager(this)
        
        // Get data from intent
        pairId = intent.getStringExtra(EXTRA_PAIR_ID)
        fromUserId = intent.getStringExtra(EXTRA_FROM_USER_ID)
        val tapTypeStr = intent.getStringExtra(EXTRA_TAP_TYPE)
        customEmoji = intent.getStringExtra(EXTRA_CUSTOM_EMOJI)
        message = intent.getStringExtra(EXTRA_MESSAGE)
        tapType = tapTypeStr?.let { TapType.fromString(it) }
        
        setupUI()
        setupButtons()
    }

    private fun setupUI() {
        // Check if custom emoji is provided
        val isCustomEmoji = !customEmoji.isNullOrBlank()
        
        // Set background color based on tap type/mood (or default for custom emoji)
        val backgroundColor = if (isCustomEmoji) {
            // Default background for custom emoji (can be customized later)
            android.graphics.Color.parseColor("#000000")
        } else {
            tapType?.backgroundColor ?: android.graphics.Color.parseColor("#000000")
        }
        val rootLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)
        rootLayout.setBackgroundColor(backgroundColor)
        
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
        
        // Show celebration message briefly
        showCelebrationMessage()
        
        // Determine text color based on background brightness
        // Use white text for dark backgrounds, dark text for light backgrounds
        val textColor = if (isLightColor(backgroundColor)) {
            android.graphics.Color.parseColor("#000000") // Black for light backgrounds
        } else {
            android.graphics.Color.parseColor("#FFFFFF") // White for dark backgrounds
        }
        
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
        
        // TODO: Show caller name if available
        // For now, just show generic message
    }
    
    private fun isLightColor(color: Int): Boolean {
        // Calculate luminance to determine if color is light or dark
        val red = android.graphics.Color.red(color)
        val green = android.graphics.Color.green(color)
        val blue = android.graphics.Color.blue(color)
        
        // Using relative luminance formula
        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
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
    
    private fun showCelebrationMessage() {
        // Create personalized celebration message
        val celebrationMessages = listOf(
            "You're special! ❤️",
            "You were missed! ❤️",
            "You're the first person they thought of today! ❤️",
            "You're loved! ❤️"
        )
        val message = celebrationMessages.random()
        
        // Show brief celebration overlay
        val celebrationView = layoutInflater.inflate(R.layout.celebration_confetti, null)
        val container = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)
        
        val celebrationEmoji = celebrationView.findViewById<android.widget.TextView>(R.id.celebrationEmoji)
        val celebrationMessage = celebrationView.findViewById<android.widget.TextView>(R.id.celebrationMessage)
        val celebrationSubMessage = celebrationView.findViewById<android.widget.TextView>(R.id.celebrationSubMessage)
        
        celebrationEmoji.text = tapType?.emoji ?: customEmoji ?: "❤️"
        celebrationMessage.text = message
        celebrationSubMessage.text = message ?: "Someone is thinking of you"
        
        container.addView(celebrationView)
        
        // Animate celebration
        celebrationView.alpha = 0f
        celebrationView.animate()
            .alpha(1.0f)
            .setDuration(500)
            .withEndAction {
                celebrationView.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setStartDelay(2000)
                    .withEndAction {
                        container.removeView(celebrationView)
                    }
                    .start()
            }
            .start()
    }

    private fun sendReply() {
        val pairId = this.pairId
        if (pairId == null) {
            Toast.makeText(this, "Error: Pair not found", Toast.LENGTH_SHORT).show()
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
                // Send a "Loving Miss" tap back as reply
                val response = RetrofitClient.apiService.sendTap(
                    "Bearer $token",
                    com.tapme.app.data.remote.SendTapRequest(
                        pairId = pairId,
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Don't allow back button to dismiss (like incoming call)
        // User must tap Dismiss button
    }

    companion object {
        const val EXTRA_PAIR_ID = "extra_pair_id"
        const val EXTRA_FROM_USER_ID = "extra_from_user_id"
        const val EXTRA_TAP_TYPE = "extra_tap_type"
        const val EXTRA_CUSTOM_EMOJI = "extra_custom_emoji"
        const val EXTRA_MESSAGE = "extra_message"
        
        fun createIntent(
            context: android.content.Context,
            pairId: String,
            fromUserId: String,
            tapType: String,
            customEmoji: String? = null,
            message: String? = null
        ): Intent {
            return Intent(context, IncomingTapActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_PAIR_ID, pairId)
                putExtra(EXTRA_FROM_USER_ID, fromUserId)
                putExtra(EXTRA_TAP_TYPE, tapType)
                putExtra(EXTRA_CUSTOM_EMOJI, customEmoji)
                putExtra(EXTRA_MESSAGE, message)
            }
        }
    }
}
