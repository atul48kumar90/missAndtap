package com.tapme.app.ui.tap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tapme.app.data.remote.AllowedTapper
import com.tapme.app.data.remote.RetrofitClient
import com.tapme.app.domain.models.TapType
import com.tapme.app.utils.PreferencesManager
import kotlinx.coroutines.launch

sealed class TapState {
    object Loading : TapState()
    data class Success(
        // What you did (sent)
        val todayTapCount: Int,
        val lastTapTime: Long?,
        val streak: Int? = null,
        val dailyRemaining: Int? = null,
        val cooldownMinutes: Int? = null,
        // What you received
        val todayTapsReceived: Int? = null,
        val lastTapReceivedTime: Long? = null
    ) : TapState()
    data class Error(val message: String) : TapState()
    object NoRecipients : TapState() // No one in allowed list
}

class TapViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    private val _tapState = MutableLiveData<TapState>()
    val tapState: LiveData<TapState> = _tapState

    private val _allowedTappers = MutableLiveData<List<AllowedTapper>>()
    val allowedTappers: LiveData<List<AllowedTapper>> = _allowedTappers

    private val _selectedRecipient = MutableLiveData<AllowedTapper?>()
    val selectedRecipient: LiveData<AllowedTapper?> = _selectedRecipient

    init {
        loadAllowedTappers()
        loadStats()
    }

    fun refreshStats() {
        loadStats()
    }

    fun loadAllowedTappers() {
        viewModelScope.launch {
            val token = preferencesManager.getToken()
            if (token == null) {
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.getAllowedTappers("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val tappers = response.body()!!.allowedTappers
                    _allowedTappers.value = tappers
                    
                    // Auto-select first recipient if available and none selected
                    if (tappers.isNotEmpty() && _selectedRecipient.value == null) {
                        _selectedRecipient.value = tappers[0]
                    } else if (tappers.isEmpty()) {
                        _tapState.value = TapState.NoRecipients
                    }
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun selectRecipient(tapper: AllowedTapper) {
        _selectedRecipient.value = tapper
    }

    fun sendTap(tapType: TapType, message: String? = null) {
        viewModelScope.launch {
            val recipient = _selectedRecipient.value
            if (recipient == null) {
                _tapState.value = TapState.Error("Please select a recipient")
                return@launch
            }

            _tapState.value = TapState.Loading

            val token = preferencesManager.getToken()
            if (token == null) {
                _tapState.value = TapState.Error("Not authenticated")
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.sendTap(
                    "Bearer $token",
                    com.tapme.app.data.remote.SendTapRequest(
                        toUserCode = recipient.tapperUserCode,
                        tapType = tapType.name,
                        customEmoji = null,
                        message = message
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    // Update rate limit info
                    val rateLimit = response.body()?.rateLimit
                    loadStats(rateLimit?.dailyRemaining, rateLimit?.cooldownMinutes)
                } else {
                    // Parse error message
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null && errorBody.contains("Not allowed to tap")) {
                            "This user hasn't added you to their allowed tappers list. Share your user code with them first."
                        } else if (errorBody != null && errorBody.contains("Cooldown period")) {
                            val json = org.json.JSONObject(errorBody)
                            json.optString("message", "Wait before your next tap. Make it count! ❤️")
                        } else if (errorBody != null && errorBody.contains("Daily limit reached")) {
                            val json = org.json.JSONObject(errorBody)
                            json.optString("message", "You've sent all your taps for today. Tomorrow is a new day! ❤️")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        response.message()
                    }
                    _tapState.value = TapState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _tapState.value = TapState.Error(e.message ?: "Failed to send tap")
            }
        }
    }

    fun sendCustomEmojiTap(customEmoji: String, message: String? = null) {
        viewModelScope.launch {
            val recipient = _selectedRecipient.value
            if (recipient == null) {
                _tapState.value = TapState.Error("Please select a recipient")
                return@launch
            }

            _tapState.value = TapState.Loading

            val token = preferencesManager.getToken()
            if (token == null) {
                _tapState.value = TapState.Error("Not authenticated")
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.sendTap(
                    "Bearer $token",
                    com.tapme.app.data.remote.SendTapRequest(
                        toUserCode = recipient.tapperUserCode,
                        tapType = null, // No predefined tap type for custom emoji
                        customEmoji = customEmoji,
                        message = message
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    // Update rate limit info
                    val rateLimit = response.body()?.rateLimit
                    loadStats(rateLimit?.dailyRemaining, rateLimit?.cooldownMinutes)
                } else {
                    // Parse error message
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null && errorBody.contains("Not allowed to tap")) {
                            "This user hasn't added you to their allowed tappers list. Share your user code with them first."
                        } else if (errorBody != null && errorBody.contains("Cooldown period")) {
                            val json = org.json.JSONObject(errorBody)
                            json.optString("message", "Wait before your next tap. Make it count! ❤️")
                        } else if (errorBody != null && errorBody.contains("Daily limit reached")) {
                            val json = org.json.JSONObject(errorBody)
                            json.optString("message", "You've sent all your taps for today. Tomorrow is a new day! ❤️")
                        } else {
                            response.message()
                        }
                    } catch (e: Exception) {
                        response.message()
                    }
                    _tapState.value = TapState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _tapState.value = TapState.Error(e.message ?: "Failed to send tap")
            }
        }
    }

    private fun loadStats(dailyRemaining: Int? = null, cooldownMinutes: Int? = null) {
        viewModelScope.launch {
            val token = preferencesManager.getToken()
            if (token == null) {
                _tapState.value = TapState.Error("Not authenticated")
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.getStats("Bearer $token")

                if (response.isSuccessful && response.body()?.success == true) {
                    val stats = response.body()!!.stats
                    
                    // Debug logging
                    android.util.Log.d("TapViewModel", "Stats received - todayTapCount: ${stats.todayTapCount}, todayTapsReceived: ${stats.todayTapsReceived}, lastTapTime: ${stats.lastTapTime}, lastTapReceivedTime: ${stats.lastTapReceivedTime}")
                    
                    // Parse timestamps with error handling
                    val lastTapTimeParsed = stats.lastTapTime?.let {
                        try {
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }.parse(it)?.time
                        } catch (e: Exception) {
                            android.util.Log.e("TapViewModel", "Failed to parse lastTapTime: $it", e)
                            null
                        }
                    }
                    
                    val lastTapReceivedTimeParsed = stats.lastTapReceivedTime?.let {
                        try {
                            android.util.Log.d("TapViewModel", "Parsing lastTapReceivedTime: $it")
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                                timeZone = java.util.TimeZone.getTimeZone("UTC")
                            }.parse(it)?.time
                        } catch (e: Exception) {
                            android.util.Log.e("TapViewModel", "Failed to parse lastTapReceivedTime: $it", e)
                            null
                        }
                    }
                    
                    android.util.Log.d("TapViewModel", "Parsed - lastTapTime: $lastTapTimeParsed, lastTapReceivedTime: $lastTapReceivedTimeParsed")
                    
                    _tapState.value = TapState.Success(
                        // What you did (sent)
                        todayTapCount = stats.todayTapCount,
                        lastTapTime = lastTapTimeParsed,
                        streak = stats.streak,
                        dailyRemaining = dailyRemaining,
                        cooldownMinutes = cooldownMinutes,
                        // What you received
                        todayTapsReceived = stats.todayTapsReceived,
                        lastTapReceivedTime = lastTapReceivedTimeParsed
                    )
                } else {
                    _tapState.value = TapState.Error("Failed to load stats")
                }
            } catch (e: Exception) {
                _tapState.value = TapState.Error(e.message ?: "Failed to load stats")
            }
        }
    }
}
