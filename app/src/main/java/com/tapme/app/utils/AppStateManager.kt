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
    
    override fun onActivityStarted(activity: Activity) {
        AppStateManager.getInstance().onActivityStarted()
    }
    
    override fun onActivityStopped(activity: Activity) {
        AppStateManager.getInstance().onActivityStopped()
    }
    
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
