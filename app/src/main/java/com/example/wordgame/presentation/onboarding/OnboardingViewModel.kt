package com.example.wordgame.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordgame.data.local.PlayerPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val playerPreferences: PlayerPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            playerPreferences.observeProfile().collect { profile ->
                _state.update {
                    it.copy(
                        nameInput = if (profile.name.isNotBlank()) profile.name else it.nameInput,
                        hasOnboarded = profile.hasOnboarded
                    )
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        _state.update { it.copy(nameInput = value.take(20), errorMessage = null) }
    }

    fun savePlayer() {
        val name = state.value.nameInput.trim()
        if (name.length < 3) {
            _state.update { it.copy(errorMessage = "Name must be at least 3 characters") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            playerPreferences.saveName(name)
            _state.update { it.copy(isSaving = false, hasOnboarded = true) }
        }
    }
}

data class OnboardingUiState(
    val nameInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val hasOnboarded: Boolean = false
)
