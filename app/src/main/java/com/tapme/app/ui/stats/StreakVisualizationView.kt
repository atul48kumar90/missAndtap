package com.tapme.app.ui.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tapme.app.R

/**
 * Beautiful visual representation of tap streak
 * Shows a growing vine/tree that grows with each day of streak
 */
class StreakVisualizationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var streakDays: Int = 0
    private val maxVisibleDays = 30 // Show up to 30 days visually
    
    private val vinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.accent_pink)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    
    private val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.accent_green)
        style = Paint.Style.FILL
    }
    
    private val milestonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_red)
        style = Paint.Style.FILL
    }
    
    fun setStreak(days: Int) {
        streakDays = days
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (streakDays == 0) {
            drawEmptyState(canvas)
            return
        }
        
        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 40f
        
        val daysToShow = minOf(streakDays, maxVisibleDays)
        val segmentHeight = (height - padding * 2) / maxVisibleDays
        
        // Draw vine path
        val path = Path()
        val startX = padding
        var currentY = height - padding
        
        // Create a gentle curve for the vine
        for (i in 0 until daysToShow) {
            val x = startX + (i % 3) * 15f // Gentle zigzag
            val y = currentY - segmentHeight
            
            if (i == 0) {
                path.moveTo(x, currentY)
            }
            path.lineTo(x, y)
            
            // Draw leaf every 3 days
            if (i > 0 && i % 3 == 0) {
                canvas.drawCircle(x, y, 8f, leafPaint)
            }
            
            // Draw milestone marker (7, 30, 100 days)
            if (streakDays >= 7 && i == 6) {
                canvas.drawCircle(x, y, 12f, milestonePaint)
            } else if (streakDays >= 30 && i == 29) {
                canvas.drawCircle(x, y, 12f, milestonePaint)
            }
            
            currentY = y
        }
        
        // Draw the vine
        canvas.drawPath(path, vinePaint)
        
        // Draw milestone labels
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.primary_red)
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        
        if (streakDays >= 7 && daysToShow >= 7) {
            val milestoneY = height - padding - (7 * segmentHeight)
            canvas.drawText("7", startX + 30f, milestoneY + 5f, textPaint)
        }
        if (streakDays >= 30 && daysToShow >= 30) {
            val milestoneY = height - padding - (30 * segmentHeight)
            canvas.drawText("30", startX + 30f, milestoneY + 5f, textPaint)
        }
    }
    
    private fun drawEmptyState(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        
        // Draw empty state message
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.text_tertiary)
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        
        val message = "Start a streak to see it grow! 🌱"
        val y = height / 2f
        canvas.drawText(message, width / 2f, y, textPaint)
    }
}
