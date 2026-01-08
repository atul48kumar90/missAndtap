package com.tapme.app.ui.tap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tapme.app.data.remote.RetrofitClient
import com.tapme.app.domain.models.TapType
import com.tapme.app.utils.PreferencesManager
import kotlinx.coroutines.launch

sealed class TapState {
    object Loading : TapState()
    data class Success(
        val todayTapCount: Int,
        val lastTapTime: Long?,
        val streak: Int? = null,
        val dailyRemaining: Int? = null,
        val cooldownMinutes: Int? = null
    ) : TapState()
    data class Error(val message: String) : TapState()
    object NotPaired : TapState()
}

class TapViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    private val _tapState = MutableLiveData<TapState>()
    val tapState: LiveData<TapState> = _tapState

    init {
        loadStats()
    }

    fun sendTap(tapType: TapType, message: String? = null) {
        viewModelScope.launch {
            _tapState.value = TapState.Loading

            val pairId = preferencesManager.getPairId()
            if (pairId == null) {
                _tapState.value = TapState.NotPaired
                return@launch
            }

            val token = preferencesManager.getToken()
            if (token == null) {
                _tapState.value = TapState.Error("Not authenticated")
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.sendTap(
                    "Bearer $token",
                    com.tapme.app.data.remote.SendTapRequest(
                        pairId = pairId,
                        tapType = tapType.name,
                        customEmoji = null,
                        message = message
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    loadStats() // Refresh stats
                } else {
                    // Parse error message
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody?.contains("Not allowed to tap") == true) {
                            "This user hasn't added you to their allowed tappers list. Share your user code with them first."
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
            _tapState.value = TapState.Loading

            val pairId = preferencesManager.getPairId()
            if (pairId == null) {
                _tapState.value = TapState.NotPaired
                return@launch
            }

            val token = preferencesManager.getToken()
            if (token == null) {
                _tapState.value = TapState.Error("Not authenticated")
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.sendTap(
                    "Bearer $token",
                    com.tapme.app.data.remote.SendTapRequest(
                        pairId = pairId,
                        tapType = null, // No predefined tap type for custom emoji
                        customEmoji = customEmoji,
                        message = message
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    loadStats() // Refresh stats
                } else {
                    // Parse error message
                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody?.contains("Not allowed to tap") == true) {
                            "This user hasn't added you to their allowed tappers list. Share your user code with them first."
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
            val pairId = preferencesManager.getPairId()
            if (pairId == null) {
                _tapState.value = TapState.NotPaired
                return@launch
            }

            val token = preferencesManager.getToken()
            if (token == null) {
                _tapState.value = TapState.Error("Not authenticated")
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.getStats(
                    "Bearer $token",
                    pairId
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val stats = response.body()!!.stats
                    _tapState.value = TapState.Success(
                        todayTapCount = stats.todayTapCount,
                        lastTapTime = stats.lastTapTime?.let { 
                            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                .parse(it)?.time
                        },
                        streak = stats.streak,
                        dailyRemaining = dailyRemaining,
                        cooldownMinutes = cooldownMinutes
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
