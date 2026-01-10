package com.tapme.app.ui.tap

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tapme.app.R
import com.tapme.app.utils.AnticipationTimer

/**
 * Shows anticipation timer with gentle pulsing animation
 * Creates "waiting for tap" feeling without pressure
 */
class TapAnticipationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    
    private val timerText: TextView
    private val anticipationTimer = AnticipationTimer()
    
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_tap_anticipation, this, true)
        timerText = view.findViewById(R.id.anticipationTimerText)
    }
    
    fun startAnticipation(lastTapTime: Long?) {
        anticipationTimer.start(lastTapTime) { timeText ->
            timerText.text = "Last tap: $timeText"
            
            // Gentle pulsing animation if it's been a while (> 2 hours)
            if (lastTapTime != null) {
                val hoursSince = (System.currentTimeMillis() - lastTapTime) / (1000 * 60 * 60)
                if (hoursSince >= 2) {
                    startPulsingAnimation()
                } else {
                    stopPulsingAnimation()
                }
            }
        }
    }
    
    fun stopAnticipation() {
        anticipationTimer.stop()
    }
    
    private fun startPulsingAnimation() {
        animate()
            .alpha(0.6f)
            .setDuration(1000)
            .withEndAction {
                animate()
                    .alpha(1.0f)
                    .setDuration(1000)
                    .withEndAction {
                        if (visibility == View.VISIBLE) {
                            startPulsingAnimation()
                        }
                    }
                    .start()
            }
            .start()
    }
    
    private fun stopPulsingAnimation() {
        animate().alpha(1.0f).setDuration(200).start()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnticipation()
    }
}
