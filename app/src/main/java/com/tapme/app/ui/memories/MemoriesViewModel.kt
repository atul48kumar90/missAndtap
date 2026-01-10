package com.tapme.app.ui.memories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tapme.app.data.remote.RetrofitClient
import com.tapme.app.utils.PreferencesManager
import kotlinx.coroutines.launch

sealed class MemoriesState {
    object Loading : MemoriesState()
    object LoadingMore : MemoriesState() // Loading additional items (pagination)
    data class Success(
        val taps: List<com.tapme.app.data.remote.TapHistoryItem>,
        val hasMore: Boolean = true, // True if there might be more items to load
        val isLoadingMore: Boolean = false
    ) : MemoriesState()
    data class Error(val message: String) : MemoriesState()
}

class MemoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)

    private val _memoriesState = MutableLiveData<MemoriesState>()
    val memoriesState: LiveData<MemoriesState> = _memoriesState

    private var currentTaps = mutableListOf<com.tapme.app.data.remote.TapHistoryItem>()
    private var isLoading = false
    private var hasMore = true
    private var currentOffset = 0
    private val pageSize = 20 // Load 20 items per page

    fun loadMemories() {
        if (isLoading) return
        
        viewModelScope.launch {
            isLoading = true
            currentOffset = 0
            currentTaps.clear()
            _memoriesState.value = MemoriesState.Loading

            val token = preferencesManager.getToken()
            if (token == null) {
                _memoriesState.value = MemoriesState.Error("Not authenticated")
                isLoading = false
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.getTapHistory("Bearer $token", pageSize, currentOffset)
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    val taps = body.taps
                    currentTaps.addAll(taps)
                    // Use hasMore from response if available, otherwise infer from page size
                    hasMore = body.hasMore ?: (taps.size == pageSize)
                    currentOffset += taps.size
                    _memoriesState.value = MemoriesState.Success(currentTaps.toList(), hasMore, false)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Failed to load memories"
                    _memoriesState.value = MemoriesState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _memoriesState.value = MemoriesState.Error(e.message ?: "Failed to load memories")
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMore() {
        if (isLoading || !hasMore) return

        viewModelScope.launch {
            isLoading = true
            _memoriesState.value = MemoriesState.Success(currentTaps.toList(), hasMore, isLoadingMore = true)

            val token = preferencesManager.getToken()
            if (token == null) {
                isLoading = false
                return@launch
            }

            try {
                val response = RetrofitClient.apiService.getTapHistory("Bearer $token", pageSize, currentOffset)
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    val newTaps = body.taps
                    if (newTaps.isNotEmpty()) {
                        currentTaps.addAll(newTaps)
                        // Use hasMore from response if available, otherwise infer from page size
                        hasMore = body.hasMore ?: (newTaps.size == pageSize)
                        currentOffset += newTaps.size
                        _memoriesState.value = MemoriesState.Success(currentTaps.toList(), hasMore, isLoadingMore = false)
                    } else {
                        hasMore = false
                        _memoriesState.value = MemoriesState.Success(currentTaps.toList(), hasMore = false, isLoadingMore = false)
                    }
                } else {
                    hasMore = false
                    _memoriesState.value = MemoriesState.Success(currentTaps.toList(), hasMore = false, isLoadingMore = false)
                }
            } catch (e: Exception) {
                // On error, stop trying to load more
                hasMore = false
                _memoriesState.value = MemoriesState.Success(currentTaps.toList(), hasMore = false, isLoadingMore = false)
            } finally {
                isLoading = false
            }
        }
    }

    fun isLoading(): Boolean = isLoading
    
    fun hasMore(): Boolean = hasMore
}
