package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.remote.toUserMessage
import com.example.app.data.repository.ReorderRepository
import com.example.app.domain.model.Reorder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ReordersState(
    val queue: List<Reorder> = emptyList(),
    val history: List<Reorder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReordersViewModel : ViewModel() {

    private val repository = ReorderRepository()
    private val _state = MutableStateFlow(ReordersState())
    val state: StateFlow<ReordersState> = _state

    private val refreshIntervalMs = 3_000L
    private var pollingJob: Job? = null
    private var hasLoaded = false

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            if (!hasLoaded) {
                _state.value = _state.value.copy(isLoading = true)
            }

            while (isActive) {
                loadReorders()
                delay(refreshIntervalMs)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            loadReorders()
        }
    }

    private suspend fun loadReorders() {
        try {
            val reorders = repository.getReorders()
            hasLoaded = true
            _state.value = _state.value.copy(
                queue = reorders.queue,
                history = reorders.history,
                isLoading = false,
                error = null
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.toUserMessage("Failed to load reorders.")
            )
        }
    }
}
