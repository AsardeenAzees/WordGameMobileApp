package com.example.wordgame.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordgame.data.repository.LeaderboardRepository
import com.example.wordgame.domain.model.LeaderboardEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val repository: LeaderboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardUiState(isLoading = true))
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.entries.collectLatest { entries ->
                _state.update { current ->
                    current.copy(
                        entries = entries,
                        errorMessage = current.errorMessage?.takeIf { entries.isEmpty() },
                        isLoading = current.isLoading && entries.isEmpty()
                    )
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.refresh()
            _state.update { current ->
                result.fold(
                    onSuccess = { list ->
                        current.copy(
                            isLoading = false,
                            errorMessage = if (list.isEmpty()) "No scores yet" else null
                        )
                    },
                    onFailure = { error ->
                        current.copy(
                            isLoading = false,
                            errorMessage = error.friendlyMessage()
                        )
                    }
                )
            }
        }
    }

    private fun Throwable.friendlyMessage(): String {
        val raw = message?.takeIf { it.isNotBlank() }
        return raw ?: "Unable to update leaderboard right now"
    }
}

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

