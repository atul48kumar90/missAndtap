package com.tapme.app.utils

/**
 * Singleton to track initial authentication state
 * Used to suppress error messages during app startup authentication
 */
object AuthenticationManager {
    @Volatile
    private var isInitialAuthentication = true
    
    fun setInitialAuthentication(value: Boolean) {
        isInitialAuthentication = value
    }
    
    fun isInInitialAuthentication(): Boolean = isInitialAuthentication
}
