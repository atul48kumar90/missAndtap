package com.tapme.app.ui.whitelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tapme.app.data.remote.RetrofitClient
import com.tapme.app.utils.PreferencesManager
import kotlinx.coroutines.launch

class WhitelistViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    private val _userCode = MutableLiveData<String?>()
    val userCode: LiveData<String?> = _userCode

    private val _allowedTappers = MutableLiveData<List<com.tapme.app.data.remote.AllowedTapper>>()
    val allowedTappers: LiveData<List<com.tapme.app.data.remote.AllowedTapper>> = _allowedTappers

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    fun loadUserCode() {
        viewModelScope.launch {
            val token = preferencesManager.getToken()
            if (token == null) {
                _error.value = "Not authenticated"
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.getMyUserCode("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _userCode.value = response.body()!!.userCode
                } else {
                    _error.value = "Failed to load user code"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load user code"
            }
        }
    }

    fun regenerateUserCode() {
        viewModelScope.launch {
            val token = preferencesManager.getToken()
            if (token == null) {
                _error.value = "Not authenticated"
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.regenerateUserCode("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _userCode.value = response.body()!!.userCode
                    _success.value = response.body()!!.message ?: "Code regenerated"
                } else {
                    _error.value = "Failed to regenerate code"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to regenerate code"
            }
        }
    }

    fun loadAllowedTappers() {
        viewModelScope.launch {
            val token = preferencesManager.getToken()
            if (token == null) {
                _error.value = "Not authenticated"
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.getAllowedTappers("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _allowedTappers.value = response.body()!!.allowedTappers
                } else {
                    _error.value = "Failed to load allowed tappers"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load allowed tappers"
            }
        }
    }

    fun addTapper(userCode: String) {
        viewModelScope.launch {
            val token = preferencesManager.getToken()
            if (token == null) {
                _error.value = "Not authenticated"
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.addTapper(
                    "Bearer $token",
                    com.tapme.app.data.remote.AddTapperRequest(userCode = userCode)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _success.value = response.body()!!.message
                    loadAllowedTappers() // Refresh list
                } else {
                    _error.value = response.body()?.message ?: "Failed to add tapper"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add tapper"
            }
        }
    }

    fun removeTapper(tapperId: String) {
        viewModelScope.launch {
            val token = preferencesManager.getToken()
            if (token == null) {
                _error.value = "Not authenticated"
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.removeTapper("Bearer $token", tapperId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _success.value = response.body()!!.message
                    loadAllowedTappers() // Refresh list
                } else {
                    _error.value = "Failed to remove tapper"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to remove tapper"
            }
        }
    }
}
