package com.tapme.app.ui.tap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.tapme.app.R
import com.tapme.app.domain.models.TapType
import com.tapme.app.ui.tap.TapViewModel
import com.tapme.app.utils.DateFormatter
import com.tapme.app.utils.AnticipationTimer
import com.tapme.app.utils.PatternManager
import com.google.android.material.card.MaterialCardView
import java.util.Date

class TapFragment : Fragment() {

    private lateinit var viewModel: TapViewModel
    private lateinit var lastTapTimeView: android.widget.TextView
    private lateinit var todayTapCountView: android.widget.TextView
    private lateinit var streakTextView: android.widget.TextView
    private lateinit var tapsRemainingTextView: android.widget.TextView
    private lateinit var cooldownTextView: android.widget.TextView
    private lateinit var tapPatternText: android.widget.TextView
    private lateinit var loadingIndicator: android.widget.ProgressBar
    private lateinit var recipientList: androidx.recyclerview.widget.RecyclerView
    private lateinit var noRecipientsText: TextView
    private lateinit var tapButtonsScrollView: android.widget.ScrollView
    private var recipientAdapter: RecipientAdapter? = null
    private val anticipationTimer = AnticipationTimer()

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

        // Stats are now in the stats card at the top
        lastTapTimeView = view.findViewById(R.id.lastTapTime)
        todayTapCountView = view.findViewById(R.id.todayTapCount)
        streakTextView = view.findViewById(R.id.streakText)
        tapsRemainingTextView = view.findViewById(R.id.tapsRemainingText)
        cooldownTextView = view.findViewById(R.id.cooldownText)
        tapPatternText = view.findViewById(R.id.tapPatternText)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        recipientList = view.findViewById(R.id.recipientList)
        noRecipientsText = view.findViewById(R.id.noRecipientsText)
        tapButtonsScrollView = view.findViewById(R.id.tapButtonsScrollView)
        
        // Initialize visibility for new layout
        streakTextView.visibility = View.GONE
        tapsRemainingTextView.visibility = View.GONE
        cooldownTextView.visibility = View.GONE

        // Setup recipient list with horizontal scrolling
        recipientList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recipientList.setHasFixedSize(false)
        // Add padding on the end to make it clear there's scrollable content (handled in XML)

        // Setup tap type buttons
        setupTapButtons(view)

        // Observe allowed tappers
        viewModel.allowedTappers.observe(viewLifecycleOwner) { tappers ->
            updateRecipientList(tappers)
        }

        // Observe selected recipient
        viewModel.selectedRecipient.observe(viewLifecycleOwner) { recipient ->
            recipientAdapter?.let { adapter ->
                adapter.notifyDataSetChanged()
            }
            // Show/hide tap buttons based on selection
            if (recipient != null) {
                tapButtonsScrollView.visibility = View.VISIBLE
                enableTapButtons(view, true)
            } else {
                tapButtonsScrollView.visibility = View.GONE
                enableTapButtons(view, false)
            }
        }

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
                is TapState.NoRecipients -> {
                    loadingIndicator.visibility = View.GONE
                    noRecipientsText.visibility = View.VISIBLE
                    recipientList.visibility = View.GONE
                    tapButtonsScrollView.visibility = View.GONE
                }
            }
        }
    }

    private fun updateRecipientList(tappers: List<com.tapme.app.data.remote.AllowedTapper>) {
        if (tappers.isEmpty()) {
            recipientList.visibility = View.GONE
            noRecipientsText.visibility = View.VISIBLE
            tapButtonsScrollView.visibility = View.GONE
        } else {
            recipientList.visibility = View.VISIBLE
            noRecipientsText.visibility = View.GONE
            recipientAdapter = RecipientAdapter(
                tappers,
                viewModel.selectedRecipient.value,
                { recipient -> viewModel.selectRecipient(recipient) }
            )
            recipientList.adapter = recipientAdapter
        }
    }

    private fun enableTapButtons(view: View, enabled: Boolean) {
        val buttons = listOf(
            R.id.btnHappyMiss, R.id.btnSadMiss, R.id.btnNaughtyMiss, R.id.btnLovingMiss,
            R.id.btnExcitedMiss, R.id.btnSleepyMiss, R.id.btnPlayfulMiss, R.id.btnThinkingMiss,
            R.id.btnRomanticMiss, R.id.btnCustomEmoji
        )
        buttons.forEach { buttonId ->
            view.findViewById<View>(buttonId)?.isEnabled = enabled
            view.findViewById<View>(buttonId)?.alpha = if (enabled) 1.0f else 0.5f
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh stats and allowed tappers when fragment becomes visible
        viewModel.refreshStats()
        viewModel.loadAllowedTappers()
        
        // Start anticipation timer with last tap time
        val currentState = viewModel.tapState.value
        if (currentState is TapState.Success) {
            currentState.lastTapTime?.let { timestampLong ->
                // lastTapTime is already a Long (milliseconds since epoch)
                anticipationTimer.start(timestampLong) { timeText ->
                    lastTapTimeView.text = "Last tap: $timeText"
                }
            }
        }
        
        // Check tap patterns and show "It's usually tap time!" if applicable
        checkTapPatterns()
    }
    
    private fun checkTapPatterns() {
        val patternManager = PatternManager.getInstance(requireContext())
        
        // Check if it's "tap time"
        patternManager.checkTapTime { isTapTime, message ->
            if (isTapTime && message != null) {
                tapPatternText.text = message
                tapPatternText.visibility = android.view.View.VISIBLE
            } else {
                // Show pattern insights if available
                patternManager.getPatterns { patterns ->
                    patterns?.insights?.firstOrNull()?.let { insight ->
                        tapPatternText.text = insight.message
                        tapPatternText.visibility = android.view.View.VISIBLE
                    } ?: run {
                        tapPatternText.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        anticipationTimer.stop()
    }

    private fun setupTapButtons(view: View) {
        // Happy Miss
        view.findViewById<MaterialCardView>(R.id.btnHappyMiss).setOnClickListener {
            viewModel.sendTap(TapType.HAPPY_MISS)
        }

        // Sad Miss
        view.findViewById<MaterialCardView>(R.id.btnSadMiss).setOnClickListener {
            viewModel.sendTap(TapType.SAD_MISS)
        }

        // Naughty Miss
        view.findViewById<MaterialCardView>(R.id.btnNaughtyMiss).setOnClickListener {
            viewModel.sendTap(TapType.NAUGHTY_MISS)
        }

        // Loving Miss
        view.findViewById<MaterialCardView>(R.id.btnLovingMiss).setOnClickListener {
            viewModel.sendTap(TapType.LOVING_MISS)
        }

        // Excited Miss
        view.findViewById<MaterialCardView>(R.id.btnExcitedMiss).setOnClickListener {
            viewModel.sendTap(TapType.EXCITED_MISS)
        }

        // Sleepy Miss
        view.findViewById<MaterialCardView>(R.id.btnSleepyMiss).setOnClickListener {
            viewModel.sendTap(TapType.SLEEPY_MISS)
        }

        // Playful Miss
        view.findViewById<MaterialCardView>(R.id.btnPlayfulMiss).setOnClickListener {
            viewModel.sendTap(TapType.PLAYFUL_MISS)
        }

        // Thinking Miss
        view.findViewById<MaterialCardView>(R.id.btnThinkingMiss).setOnClickListener {
            viewModel.sendTap(TapType.THINKING_MISS)
        }

        // Romantic Miss (using LOVING_MISS type for now)
        view.findViewById<MaterialCardView>(R.id.btnRomanticMiss)?.setOnClickListener {
            viewModel.sendTap(TapType.LOVING_MISS)
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
            .setCancelable(true)
            .create()

        // Apply rounded corners to dialog
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            dialog.window?.statusBarColor = android.graphics.Color.TRANSPARENT
        }

        val emojiInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emojiInput)
        val emojiPreview = dialogView.findViewById<android.widget.TextView>(R.id.emojiPreview)
        val emojiPlaceholder = dialogView.findViewById<android.widget.TextView>(R.id.emojiPlaceholder)
        val emojiPreviewHint = dialogView.findViewById<android.widget.TextView>(R.id.emojiPreviewHint)
        val errorText = dialogView.findViewById<android.widget.TextView>(R.id.errorText)
        val btnSend = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSend)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)

        // Show emoji preview as user types
        emojiInput?.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s?.toString()?.trim() ?: ""
                if (text.isNotEmpty()) {
                    emojiPreview?.text = text
                    emojiPreview?.visibility = android.view.View.VISIBLE
                    emojiPlaceholder?.visibility = android.view.View.GONE
                    emojiPreviewHint?.text = "${text.length}/10"
                    errorText?.visibility = android.view.View.GONE
                    
                    // Animate emoji preview appearance
                    emojiPreview?.alpha = 0f
                    emojiPreview?.animate()?.apply {
                        alpha(1f)
                        scaleX(1.1f)
                        scaleY(1.1f)
                        duration = 200
                        withEndAction {
                            emojiPreview?.animate()?.apply {
                                scaleX(1f)
                                scaleY(1f)
                                duration = 150
                                start()
                            }
                        }
                        start()
                    }
                } else {
                    emojiPreview?.visibility = android.view.View.GONE
                    emojiPlaceholder?.visibility = android.view.View.VISIBLE
                    emojiPreviewHint?.text = "Preview"
                }
            }
        })

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        btnSend.setOnClickListener {
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
        // Enable all tap buttons (user is paired) - restore full opacity and clickability
        val cards = listOf(
            view?.findViewById<View>(R.id.btnHappyMiss),
            view?.findViewById<View>(R.id.btnSadMiss),
            view?.findViewById<View>(R.id.btnNaughtyMiss),
            view?.findViewById<View>(R.id.btnLovingMiss),
            view?.findViewById<View>(R.id.btnExcitedMiss),
            view?.findViewById<View>(R.id.btnSleepyMiss),
            view?.findViewById<View>(R.id.btnPlayfulMiss),
            view?.findViewById<View>(R.id.btnThinkingMiss),
            view?.findViewById<View>(R.id.btnRomanticMiss),
            view?.findViewById<View>(R.id.btnCustomEmoji)
        )
        
        cards.forEach { card ->
            card?.isClickable = true
            card?.isFocusable = true
            card?.alpha = 1.0f // Restore full opacity
        }
        
        // Hide "not paired" card
        view?.findViewById<com.google.android.material.card.MaterialCardView>(R.id.notPairedCard)?.visibility = View.GONE
        
        // Update last tap time
        state.lastTapTime?.let {
            lastTapTimeView.text = "Last tap: ${DateFormatter.formatTimeAgo(Date(it))}"
            lastTapTimeView.visibility = View.VISIBLE
        } ?: run {
            lastTapTimeView.text = "Last tap: Never"
            lastTapTimeView.visibility = View.VISIBLE
        }

        // Update today's tap count (now in stats card)
        todayTapCountView.text = "${state.todayTapCount}"
        
        // Show streak if available (now in stats card)
        val streakContainer = view?.findViewById<ViewGroup>(R.id.streakContainer)
        val divider1 = view?.findViewById<View>(R.id.divider1)
        
        state.streak?.let { streak ->
            if (streak > 0) {
                streakTextView.text = "🔥 $streak"
                streakTextView.visibility = View.VISIBLE // Make sure it's visible
                streakContainer?.visibility = View.VISIBLE
                divider1?.visibility = View.VISIBLE
                
                // Gentle animation for streak
                streakTextView.alpha = 0f
                streakTextView.animate()
                    .alpha(1.0f)
                    .setDuration(500)
                    .start()
            } else {
                streakTextView.visibility = View.GONE
                streakContainer?.visibility = View.GONE
                divider1?.visibility = View.GONE
            }
        } ?: run {
            streakTextView.visibility = View.GONE
            streakContainer?.visibility = View.GONE
            divider1?.visibility = View.GONE
        }
        
        // Show remaining taps (creates scarcity)
        val remainingContainer = view?.findViewById<ViewGroup>(R.id.remainingContainer)
        val divider2 = view?.findViewById<View>(R.id.divider2)
        
        state.dailyRemaining?.let { remaining ->
            if (remaining >= 0) {
                tapsRemainingTextView.text = "$remaining"
                remainingContainer?.visibility = View.VISIBLE
                divider2?.visibility = View.VISIBLE
            } else {
                remainingContainer?.visibility = View.GONE
                divider2?.visibility = View.GONE
            }
        } ?: run {
            remainingContainer?.visibility = View.GONE
            divider2?.visibility = View.GONE
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
        // Keep cards visible but disable them (reduce opacity), show message
        val cards = listOf(
            view.findViewById<View>(R.id.btnHappyMiss),
            view.findViewById<View>(R.id.btnSadMiss),
            view.findViewById<View>(R.id.btnNaughtyMiss),
            view.findViewById<View>(R.id.btnLovingMiss),
            view.findViewById<View>(R.id.btnExcitedMiss),
            view.findViewById<View>(R.id.btnSleepyMiss),
            view.findViewById<View>(R.id.btnPlayfulMiss),
            view.findViewById<View>(R.id.btnThinkingMiss),
            view.findViewById<View>(R.id.btnRomanticMiss),
            view.findViewById<View>(R.id.btnCustomEmoji)
        )
        
        cards.forEach { card ->
            card?.isClickable = false
            card?.isFocusable = false
            card?.alpha = 0.5f // Make them look disabled
        }
        
        // Show "Not paired" card
        view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.notPairedCard)?.visibility = View.VISIBLE
    }
}
