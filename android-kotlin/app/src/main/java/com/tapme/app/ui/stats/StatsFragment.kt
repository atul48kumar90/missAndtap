package com.tapme.app.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.tapme.app.R
import com.tapme.app.ui.tap.TapViewModel

class StatsFragment : Fragment() {

    private lateinit var viewModel: TapViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application))[TapViewModel::class.java]

        // Check current state immediately (in case data is already loaded)
        val currentState = viewModel.tapState.value
        if (currentState is com.tapme.app.ui.tap.TapState.Success) {
            updateStats(view, currentState)
        }

        // Observe stats from TapViewModel
        viewModel.tapState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is com.tapme.app.ui.tap.TapState.Success -> {
                    updateStats(view, state)
                }
                is com.tapme.app.ui.tap.TapState.Error -> {
                    // Show error
                }
                else -> {
                    // Loading or other states
                }
            }
        }
        
        // View Memories button
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewMemories)?.setOnClickListener {
            findNavController().navigate(R.id.memoriesFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh stats when fragment becomes visible to ensure it's in sync
        viewModel.refreshStats()
    }

    private fun updateStats(view: View, state: com.tapme.app.ui.tap.TapState.Success) {
        // Update UI with RECEIVED stats (what you got, not what you sent)
        
        // Show received taps count (just the number, label is in layout)
        val receivedCount = state.todayTapsReceived ?: 0
        view.findViewById<android.widget.TextView>(R.id.statsTodayCount)?.text = 
            "$receivedCount"
        
        // Streak is based on what you sent, but we can still show it
        // Alternatively, we could calculate a "received streak" but that might be less meaningful
        val streak = state.streak ?: 0
        view.findViewById<android.widget.TextView>(R.id.statsStreak)?.text = 
            "$streak"
        
        // Update streak visualization
        val streakVisualization = view.findViewById<com.tapme.app.ui.stats.StreakVisualizationView>(R.id.streakVisualization)
        streakVisualization?.setStreak(streak)
        
        // Check for milestone celebrations (based on sent streak, but still meaningful)
        checkAndCelebrateMilestone(view, streak)
        
        // Show last tap RECEIVED time (just the time, label is in layout)
        val lastTapTextView = view.findViewById<android.widget.TextView>(R.id.statsLastTap)
        android.util.Log.d("StatsFragment", "Updating last tap received - lastTapReceivedTime: ${state.lastTapReceivedTime}, lastTapTime: ${state.lastTapTime}")
        
        if (state.lastTapReceivedTime != null) {
            val receivedDate = java.util.Date(state.lastTapReceivedTime)
            val now = java.util.Date()
            android.util.Log.d("StatsFragment", "Last tap received at: $receivedDate, now: $now, difference: ${now.time - receivedDate.time}ms")
            val lastTapText = com.tapme.app.utils.DateFormatter.formatTimeAgo(receivedDate)
            android.util.Log.d("StatsFragment", "Formatted text: $lastTapText")
            lastTapTextView?.text = lastTapText
        } else {
            android.util.Log.d("StatsFragment", "lastTapReceivedTime is null, showing 'Never'")
            lastTapTextView?.text = "Never"
        }
    }
    
    private var lastCelebratedStreak = 0
    
    private fun checkAndCelebrateMilestone(view: View, currentStreak: Int) {
        // Celebrate milestones: 7, 30, 100 days
        val milestones = listOf(7, 30, 100)
        
        for (milestone in milestones) {
            if (currentStreak >= milestone && lastCelebratedStreak < milestone) {
                celebrateMilestone(view, milestone)
                lastCelebratedStreak = milestone
                break
            }
        }
    }
    
    private fun celebrateMilestone(view: View, milestone: Int) {
        val streakCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardStreak)
        
        // Celebration message
        val messages = mapOf(
            7 to "🎉 7 Day Streak! You're building something special!",
            30 to "🌟 30 Day Streak! Amazing dedication!",
            100 to "💎 100 Day Streak! You're incredible!"
        )
        
        val message = messages[milestone] ?: "🎉 Milestone reached!"
        
        // Show celebration with animation
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
        
        // Animate the streak card
        streakCard?.animate()
            ?.scaleX(1.1f)
            ?.scaleY(1.1f)
            ?.setDuration(200)
            ?.withEndAction {
                streakCard.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
            ?.start()
    }
}
