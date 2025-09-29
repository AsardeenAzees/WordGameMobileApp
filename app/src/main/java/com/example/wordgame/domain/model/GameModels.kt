package com.example.wordgame.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameWord(
    val value: String,
    val hint: String? = null,
    val difficulty: Int = 1
)

enum class ClueType { LETTER_COUNT, LETTER_OCCURRENCE, WORD_TIP }

@Serializable
data class ClueResult(
    val type: ClueType,
    val message: String,
    val cost: Int
)

@Serializable
data class GuessResult(
    val isCorrect: Boolean,
    val remainingAttempts: Int,
    val updatedScore: Int,
    val message: String
)

@Serializable
data class GameSnapshot(
    val word: GameWord,
    val obscuredWord: String,
    val attemptsLeft: Int,
    val score: Int,
    val elapsedSeconds: Int,
    val usedClues: Set<ClueType>,
    val status: GameStatus
)

@Serializable
data class PlayerProfile(
    val name: String,
    val bestScore: Int,
    val hasOnboarded: Boolean,
    val guessHistory: List<GuessHistoryItem> = emptyList()
)

@Serializable
data class GuessHistoryItem(
    val word: String,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class LeaderboardEntry(
    val player: String,
    val score: Int,
    val timeSeconds: Int,
    val rank: Int? = null,
    val timestamp: Long? = null
)

enum class GameStatus { PLAYING, WON, LOST }
