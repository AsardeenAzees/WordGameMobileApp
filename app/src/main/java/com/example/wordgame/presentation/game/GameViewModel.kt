package com.example.wordgame.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordgame.data.local.PlayerPreferences
import com.example.wordgame.data.repository.LeaderboardRepository
import com.example.wordgame.data.repository.WordRepository
import com.example.wordgame.domain.GameEngine
import com.example.wordgame.domain.model.ClueResult
import com.example.wordgame.domain.model.ClueType
import com.example.wordgame.domain.model.GameConfig
import com.example.wordgame.domain.model.GameStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val wordRepository: WordRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val playerPreferences: PlayerPreferences,
    private val engine: GameEngine = GameEngine(GameConfig())
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var currentLevel: Int = 1
    private val config = GameConfig()

    init {
        viewModelScope.launch {
            playerPreferences.observeProfile().collect { profile ->
                _state.update {
                    it.copy(
                        playerName = profile.name,
                        bestScore = profile.bestScore
                    )
                }
            }
        }
        startNewGame()
    }

    fun startNewGame() {
        currentLevel = 1
        loadWord(seedScore = config.startingScore)
    }

    fun retrySameLevel() {
        loadWord(seedScore = config.startingScore)
    }

    fun proceedToNextLevel() {
        currentLevel += 1
        val carryOver = state.value.score
        loadWord(seedScore = carryOver)
    }

    fun onGuessChanged(value: String) {
        _state.update { it.copy(currentGuess = value.take(20)) }
    }

    fun submitGuess() {
        val guess = state.value.currentGuess
        if (guess.isBlank()) {
            _state.update { it.copy(message = "Enter a guess to continue") }
            return
        }
        val elapsed = state.value.elapsedSeconds
        val result = engine.submitGuess(guess, elapsed)
        val snapshot = engine.snapshot(elapsed)
        if (snapshot.status != GameStatus.PLAYING) {
            timerJob?.cancel()
        }

        if (result.isCorrect) {
            val playerName = state.value.playerName
            viewModelScope.launch {
                playerPreferences.updateBestScore(result.updatedScore)
                val sanitizedName = playerName.trim()
                if (sanitizedName.isNotBlank()) {
                    val submission = leaderboardRepository.submitScore(
                        name = sanitizedName,
                        score = result.updatedScore,
                        timeSeconds = elapsed
                    )
                    submission.onFailure { error ->
                        val fallback = error.message ?: "Could not post score online right now"
                        _state.update { current ->
                            val base = current.message ?: result.message
                            current.copy(message = listOfNotNull(base, fallback).joinToString("\n").ifBlank { fallback })
                        }
                    }
                }
            }
        }

        _state.update {
            it.copy(
                score = result.updatedScore,
                attemptsLeft = result.remainingAttempts,
                message = result.message,
                currentGuess = if (result.isCorrect) "" else it.currentGuess,
                status = snapshot.status,
                usedClues = snapshot.usedClues,
                obscuredWord = snapshot.obscuredWord,
                // Update clue history
                clueHistory = engine.getClueHistory()
            )
        }
    }

    fun useClue(type: ClueType, letter: Char? = null) {
        val clue: ClueResult = engine.applyClue(type, letter) ?: run {
            _state.update { it.copy(message = "Clue not available yet") }
            return
        }
        val snapshot = engine.snapshot(state.value.elapsedSeconds)
        _state.update {
            it.copy(
                score = snapshot.score,
                usedClues = snapshot.usedClues,
                // Instead of single clueMessage, we now use clueHistory
                clueMessage = "-${clue.cost} pts: ${clue.message}",
                obscuredWord = snapshot.obscuredWord,
                // Update clue history
                clueHistory = engine.getClueHistory()
            )
        }
    }

    fun sendScoreToLeaderboard() {
        val name = state.value.playerName.trim()
        if (name.isBlank()) {
            _state.update { it.copy(message = "Set your player name first in Settings") }
            return
        }
        val score = state.value.score
        val timeSeconds = state.value.elapsedSeconds
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingScore = true) }
            val result = leaderboardRepository.submitScore(name, score, timeSeconds)
            val message = result.fold(
                onSuccess = { "Score submitted!" },
                onFailure = { it.message ?: "Unable to submit score right now" }
            )
            _state.update {
                it.copy(
                    isSubmittingScore = false,
                    message = message
                )
            }
        }
    }

    private fun loadWord(seedScore: Int) {
        timerJob?.cancel()
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    message = null,
                    clueMessage = null,
                    currentGuess = "",
                    status = GameStatus.PLAYING,
                    clueHistory = emptyList() // Clear clue history for new round
                )
            }
            try {
                val baseWord = wordRepository.fetchWordForLevel(currentLevel)
                val tip = wordRepository.fetchTipFor(baseWord.value)
                val word = if (!tip.isNullOrBlank()) baseWord.copy(hint = tip) else baseWord
                engine.startNewRound(word, seedScore = seedScore)
                _state.update {
                    it.copy(
                        isLoading = false,
                        level = currentLevel,
                        score = seedScore,
                        attemptsLeft = config.maxAttempts,
                        obscuredWord = engine.snapshot(0).obscuredWord,
                        elapsedSeconds = 0,
                        usedClues = emptySet(),
                        clueMessage = null,
                        clueHistory = engine.getClueHistory() // Initialize clue history
                    )
                }
                startTimer()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "Failed to load word. Please try again."
                    )
                }
            }
        }
    }


    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _state.update { it.copy(elapsedSeconds = 0) }
            while (true) {
                delay(1_000)
                val status = engine.status()
                if (status != GameStatus.PLAYING) {
                    break
                }
                _state.update { state -> state.copy(elapsedSeconds = state.elapsedSeconds + 1) }
            }
            timerJob = null
        }
    }
}

data class GameUiState(
    val playerName: String = "",
    val bestScore: Int = 0,
    val level: Int = 1,
    val score: Int = 100,
    val attemptsLeft: Int = 10,
    val elapsedSeconds: Int = 0,
    val obscuredWord: String = "",
    val currentGuess: String = "",
    val message: String? = null,
    val clueMessage: String? = null,
    val usedClues: Set<ClueType> = emptySet(),
    val status: GameStatus = GameStatus.PLAYING,
    val isLoading: Boolean = false,
    val isSubmittingScore: Boolean = false,
    // Add clue history for stacking clues
    val clueHistory: List<ClueResult> = emptyList()
)