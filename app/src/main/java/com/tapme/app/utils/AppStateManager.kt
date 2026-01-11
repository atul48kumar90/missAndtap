package com.tapme.app.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle

class AppStateManager private constructor() {
    
    private var isAppInForeground = false
    private var activityCount = 0
    
    fun isAppForeground(): Boolean {
        return isAppInForeground
    }
    
    fun onActivityStarted() {
        activityCount++
        isAppInForeground = true
    }
    
    fun onActivityStopped() {
        activityCount--
        if (activityCount <= 0) {
            activityCount = 0
            isAppInForeground = false
        }
    }
    
    companion object {
        @Volatile
        private var instance: AppStateManager? = null
        
        fun getInstance(): AppStateManager {
            return instance ?: synchronized(this) {
                instance ?: AppStateManager().also { instance = it }
            }
        }
    }
}

class AppStateLifecycleCallback : Application.ActivityLifecycleCallbacks {
    
    override fun onActivityResumed(activity: Activity) {
        // Activity is resumed and visible to user - app is in foreground
        AppStateManager.getInstance().onActivityStarted()
    }
    
    override fun onActivityPaused(activity: Activity) {
        // Activity is paused - app might be going to background
        // Don't set to background yet, wait for onActivityStopped
    }
    
    override fun onActivityStopped(activity: Activity) {
        // Activity is stopped and no longer visible - app is in background
        AppStateManager.getInstance().onActivityStopped()
    }
    
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
