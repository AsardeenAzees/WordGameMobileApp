package com.example.wordgame.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordgame.data.local.PlayerPreferences
import com.example.wordgame.domain.model.GuessHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val playerPreferences: PlayerPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            playerPreferences.observeProfile().collect { profile ->
                _state.update {
                    it.copy(
                        name = profile.name,
                        bestScore = profile.bestScore,
                        guessHistory = profile.guessHistory
                    )
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name.take(20)) }
    }

    fun saveName() {
        val name = state.value.name.trim()
        if (name.length < 3) {
            _state.update { it.copy(message = "Name must be at least 3 characters") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, message = null) }
            playerPreferences.saveName(name)
            _state.update { it.copy(isSaving = false, message = "Saved") }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            playerPreferences.reset()
            _state.update { SettingsUiState(message = "Progress reset") }
        }
    }
}

data class SettingsUiState(
    val name: String = "",
    val bestScore: Int = 0,
    val isSaving: Boolean = false,
    val message: String? = null,
    val guessHistory: List<GuessHistoryItem> = emptyList()
)
