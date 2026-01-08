package com.tapme.app.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "tapme_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_PAIR_ID = "pair_id"
        private const val KEY_INVITE_CODE = "invite_code"
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun clearToken() = prefs.edit().remove(KEY_TOKEN).apply()

    fun saveUserId(userId: String) = prefs.edit().putString(KEY_USER_ID, userId).apply()
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun saveDeviceId(deviceId: String) = prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    fun getDeviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)

    fun savePairId(pairId: String?) = prefs.edit().putString(KEY_PAIR_ID, pairId).apply()
    fun getPairId(): String? = prefs.getString(KEY_PAIR_ID, null)

    fun saveInviteCode(code: String) = prefs.edit().putString(KEY_INVITE_CODE, code).apply()
    fun getInviteCode(): String? = prefs.getString(KEY_INVITE_CODE, null)

    fun clearAll() = prefs.edit().clear().apply()
}
