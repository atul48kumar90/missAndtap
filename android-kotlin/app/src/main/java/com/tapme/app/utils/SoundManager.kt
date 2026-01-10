package com.tapme.app.utils

import android.content.Context
import android.media.SoundPool
import android.os.Build
import com.tapme.app.domain.models.TapType

/**
 * Manages custom sounds for different tap types
 * Each tap type has a unique sound that tells a story
 */
class SoundManager private constructor(context: Context) {
    
    private val soundPool: SoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        SoundPool.Builder()
            .setMaxStreams(5)
            .build()
    } else {
        @Suppress("DEPRECATION")
        SoundPool(5, android.media.AudioManager.STREAM_NOTIFICATION, 0)
    }
    
    private val sounds = mutableMapOf<TapType, Int>()
    private var loaded = false
    
    init {
        // Load default notification sound for now
        // In production, you can add custom sound files in res/raw/
        // For each tap type: happy_sound.mp3, sad_sound.mp3, etc.
        loadSounds(context)
    }
    
    private fun loadSounds(context: Context) {
        // Note: To add custom sounds, place them in res/raw/ and load them here
        // Example:
        // sounds[TapType.HAPPY_MISS] = soundPool.load(context, R.raw.happy_chime, 1)
        // sounds[TapType.SAD_MISS] = soundPool.load(context, R.raw.sad_melody, 1)
        // etc.
        
        // For now, we'll use system notification sound as fallback
        loaded = true
    }
    
    /**
     * Play sound for a specific tap type
     * Each tap type has a unique sound story:
     * - Happy: Soft, cheerful chime
     * - Sad: Gentle, melancholic melody
     * - Naughty: Playful, quick sound
     * - Loving: Warm, heartbeat-like rhythm
     * - Excited: Energetic, upbeat sound
     * - Sleepy: Gentle lullaby tone
     * - Playful: Bouncy, fun sound
     * - Thinking: Thoughtful, calm sound
     */
    fun playSound(tapType: TapType, volume: Float = 0.7f) {
        if (!loaded) return
        
        val soundId = sounds[tapType]
        if (soundId != null && soundId > 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
        // If custom sound not loaded, system notification sound will play via notification
    }
    
    fun release() {
        soundPool.release()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null
        
        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
