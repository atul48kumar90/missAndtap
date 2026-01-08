package com.tapme.app.ui.tap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tapme.app.R
import com.tapme.app.domain.models.TapType
import com.tapme.app.ui.tap.TapViewModel
import com.tapme.app.utils.DateFormatter
import com.google.android.material.card.MaterialCardView
import java.util.Date

class TapFragment : Fragment() {

    private lateinit var viewModel: TapViewModel
    private lateinit var lastTapTimeView: android.widget.TextView
    private lateinit var todayTapCountView: android.widget.TextView
    private lateinit var streakTextView: android.widget.TextView
    private lateinit var tapsRemainingTextView: android.widget.TextView
    private lateinit var cooldownTextView: android.widget.TextView
    private lateinit var loadingIndicator: android.widget.ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tap, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[TapViewModel::class.java]

        lastTapTimeView = view.findViewById(R.id.lastTapTime)
        todayTapCountView = view.findViewById(R.id.todayTapCount)
        streakTextView = view.findViewById(R.id.streakText)
        tapsRemainingTextView = view.findViewById(R.id.tapsRemainingText)
        cooldownTextView = view.findViewById(R.id.cooldownText)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)

        // Setup tap type buttons
        setupTapButtons(view)

        // Observe view model state
        viewModel.tapState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TapState.Loading -> {
                    loadingIndicator.visibility = View.VISIBLE
                }
                is TapState.Success -> {
                    loadingIndicator.visibility = View.GONE
                    updateUI(state)
                }
                is TapState.Error -> {
                    loadingIndicator.visibility = View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is TapState.NotPaired -> {
                    loadingIndicator.visibility = View.GONE
                    showNotPairedState(view)
                }
            }
        }
    }

    private fun setupTapButtons(view: View) {
        // Happy Miss
        view.findViewById<MaterialCardView>(R.id.btnHappyMiss).setOnClickListener {
            sendTap(TapType.HAPPY_MISS)
        }

        // Sad Miss
        view.findViewById<MaterialCardView>(R.id.btnSadMiss).setOnClickListener {
            sendTap(TapType.SAD_MISS)
        }

        // Naughty Miss
        view.findViewById<MaterialCardView>(R.id.btnNaughtyMiss).setOnClickListener {
            sendTap(TapType.NAUGHTY_MISS)
        }

        // Loving Miss
        view.findViewById<MaterialCardView>(R.id.btnLovingMiss).setOnClickListener {
            sendTap(TapType.LOVING_MISS)
        }

        // Excited Miss
        view.findViewById<MaterialCardView>(R.id.btnExcitedMiss).setOnClickListener {
            sendTap(TapType.EXCITED_MISS)
        }

        // Sleepy Miss
        view.findViewById<MaterialCardView>(R.id.btnSleepyMiss).setOnClickListener {
            sendTap(TapType.SLEEPY_MISS)
        }

        // Playful Miss
        view.findViewById<MaterialCardView>(R.id.btnPlayfulMiss).setOnClickListener {
            sendTap(TapType.PLAYFUL_MISS)
        }

        // Thinking Miss
        view.findViewById<MaterialCardView>(R.id.btnThinkingMiss).setOnClickListener {
            sendTap(TapType.THINKING_MISS)
        }

        // Custom Emoji
        view.findViewById<MaterialCardView>(R.id.btnCustomEmoji).setOnClickListener {
            showCustomEmojiDialog()
        }
    }

    private fun showCustomEmojiDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_emoji, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val emojiInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emojiInput)
        val emojiPreview = dialogView.findViewById<android.widget.TextView>(R.id.emojiPreview)
        val errorText = dialogView.findViewById<android.widget.TextView>(R.id.errorText)
        val btnSend = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSend)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)

        // Show emoji preview as user types
        emojiInput?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s?.toString() ?: ""
                if (text.isNotEmpty()) {
                    emojiPreview?.text = text
                    emojiPreview?.visibility = android.view.View.VISIBLE
                    errorText?.visibility = android.view.View.GONE
                } else {
                    emojiPreview?.visibility = android.view.View.GONE
                }
            }
        })

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        btnSend?.setOnClickListener {
            val emoji = emojiInput?.text?.toString()?.trim() ?: ""
            val message = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.messageInput)?.text?.toString()?.trim()
            
            if (emoji.isEmpty()) {
                errorText?.text = "Please enter an emoji"
                errorText?.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if (!com.tapme.app.utils.EmojiValidator.isValidEmoji(emoji)) {
                errorText?.text = "Please enter a valid emoji"
                errorText?.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            if (emoji.length > 10) {
                errorText?.text = "Maximum 10 characters allowed"
                errorText?.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            // Animate button press
            btnSend?.let {
                it.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
            
            // Send custom emoji tap with optional message
            sendCustomEmojiTap(emoji, message)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun sendCustomEmojiTap(customEmoji: String, message: String? = null) {
        viewModel.sendCustomEmojiTap(customEmoji, message)
    }
    
    private fun sendTapWithMessage(tapType: TapType, message: String? = null) {
        // Add gentle animation when sending tap
        val button = when (tapType) {
            TapType.HAPPY_MISS -> view?.findViewById<MaterialCardView>(R.id.btnHappyMiss)
            TapType.SAD_MISS -> view?.findViewById<MaterialCardView>(R.id.btnSadMiss)
            TapType.NAUGHTY_MISS -> view?.findViewById<MaterialCardView>(R.id.btnNaughtyMiss)
            TapType.LOVING_MISS -> view?.findViewById<MaterialCardView>(R.id.btnLovingMiss)
            TapType.EXCITED_MISS -> view?.findViewById<MaterialCardView>(R.id.btnExcitedMiss)
            TapType.SLEEPY_MISS -> view?.findViewById<MaterialCardView>(R.id.btnSleepyMiss)
            TapType.PLAYFUL_MISS -> view?.findViewById<MaterialCardView>(R.id.btnPlayfulMiss)
            TapType.THINKING_MISS -> view?.findViewById<MaterialCardView>(R.id.btnThinkingMiss)
        }
        
        // Animate button press
        button?.let {
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }
        
        viewModel.sendTap(tapType, message)
    }

    private fun updateUI(state: TapState.Success) {
        state.lastTapTime?.let {
            lastTapTimeView.text = "Last tap: ${DateFormatter.formatTimeAgo(Date(it))}"
            lastTapTimeView.visibility = View.VISIBLE
        } ?: run {
            lastTapTimeView.visibility = View.GONE
        }

        todayTapCountView.text = "Today: ${state.todayTapCount} taps"
        
        // Show streak if available
        state.streak?.let { streak ->
            if (streak > 0) {
                streakTextView.text = "🔥 $streak day streak!"
                streakTextView.visibility = View.VISIBLE
                
                // Gentle animation for streak
                streakTextView.alpha = 0f
                streakTextView.animate()
                    .alpha(1.0f)
                    .setDuration(500)
                    .start()
            } else {
                streakTextView.visibility = View.GONE
            }
        } ?: run {
            streakTextView.visibility = View.GONE
        }
        
        // Show remaining taps (creates scarcity)
        state.dailyRemaining?.let { remaining ->
            if (remaining >= 0) {
                tapsRemainingTextView.text = "💝 $remaining tap${if (remaining != 1) "s" else ""} remaining today"
                tapsRemainingTextView.visibility = View.VISIBLE
            } else {
                tapsRemainingTextView.visibility = View.GONE
            }
        } ?: run {
            tapsRemainingTextView.visibility = View.GONE
        }
        
        // Show cooldown if active (creates anticipation)
        state.cooldownMinutes?.let { cooldown ->
            if (cooldown > 0) {
                cooldownTextView.text = "⏰ Wait $cooldown minute${if (cooldown != 1) "s" else ""} before next tap"
                cooldownTextView.visibility = View.VISIBLE
            } else {
                cooldownTextView.visibility = View.GONE
            }
        } ?: run {
            cooldownTextView.visibility = View.GONE
        }
    }

    private fun showNotPairedState(view: View) {
        // Hide tap buttons, show message
        view.findViewById<View>(R.id.btnHappyMiss).visibility = View.GONE
        // ... hide other buttons
        // Show "Not paired" message
    }
}
