package com.tapme.app.ui.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tapme.app.R
import kotlin.math.min

/**
 * Visual fire that grows with streak length
 * Creates psychological reward - see your connection "burning"
 */
class StreakFireView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var streak: Int = 0
    private val firePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val fireColors = listOf(
        Color.parseColor("#FF4500"), // Orange-red
        Color.parseColor("#FF6347"), // Tomato
        Color.parseColor("#FFD700"), // Gold
        Color.parseColor("#FFA500")  // Orange
    )
    
    fun setStreak(streakDays: Int) {
        streak = streakDays
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (streak == 0) {
            drawNoFire(canvas)
            return
        }
        
        val centerX = width / 2f
        val baseY = height.toFloat()
        
        // Fire size grows with streak (max at 30 days)
        val fireSize = min(streak / 30f, 1f) * min(width, height) * 0.8f
        val fireHeight = fireSize * 1.2f
        
        // Draw fire flames
        drawFireFlames(canvas, centerX, baseY, fireSize, fireHeight)
        
        // Draw streak number in center
        drawStreakText(canvas, centerX, baseY - fireHeight / 2)
    }
    
    private fun drawFireFlames(canvas: Canvas, centerX: Float, baseY: Float, width: Float, height: Float) {
        val flameCount = min(streak / 3, 5) + 1 // More flames for longer streaks
        
        for (i in 0 until flameCount) {
            val offset = (i - flameCount / 2) * (width / flameCount)
            val flameWidth = width / flameCount * 0.8f
            val flameHeight = height * (0.8f + (i % 2) * 0.2f)
            
            firePaint.color = fireColors[i % fireColors.size]
            firePaint.alpha = (255 * (0.7f + (i % 2) * 0.3f)).toInt()
            
            val path = Path().apply {
                moveTo(centerX + offset - flameWidth / 2, baseY)
                quadTo(centerX + offset, baseY - flameHeight * 0.3f, centerX + offset - flameWidth / 4, baseY - flameHeight * 0.6f)
                quadTo(centerX + offset, baseY - flameHeight * 0.8f, centerX + offset, baseY - flameHeight)
                quadTo(centerX + offset, baseY - flameHeight * 0.8f, centerX + offset + flameWidth / 4, baseY - flameHeight * 0.6f)
                quadTo(centerX + offset, baseY - flameHeight * 0.3f, centerX + offset + flameWidth / 2, baseY)
                close()
            }
            
            canvas.drawPath(path, firePaint)
        }
    }
    
    private fun drawStreakText(canvas: Canvas, centerX: Float, centerY: Float) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = min(width, height) * 0.15f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        
        canvas.drawText("$streak", centerX, centerY + textPaint.textSize / 3, textPaint)
    }
    
    private fun drawNoFire(canvas: Canvas) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.text_secondary)
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText("Start a streak to see the fire! 🔥", width / 2f, height / 2f, textPaint)
    }
}
