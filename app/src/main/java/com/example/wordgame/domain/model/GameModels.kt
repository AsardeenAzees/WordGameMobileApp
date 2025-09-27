package com.example.wordgame.domain.model

data class GameWord(
    val value: String,
    val hint: String? = null,
    val difficulty: Int = 1
)

enum class ClueType { LETTER_COUNT, LETTER_OCCURRENCE, WORD_TIP }

data class ClueResult(
    val type: ClueType,
    val message: String,
    val cost: Int
)

data class GuessResult(
    val isCorrect: Boolean,
    val remainingAttempts: Int,
    val updatedScore: Int,
    val message: String
)

data class GameSnapshot(
    val word: GameWord,
    val obscuredWord: String,
    val attemptsLeft: Int,
    val score: Int,
    val elapsedSeconds: Int,
    val usedClues: Set<ClueType>,
    val status: GameStatus
)

data class PlayerProfile(
    val name: String,
    val bestScore: Int,
    val hasOnboarded: Boolean
)

data class LeaderboardEntry(
    val player: String,
    val score: Int,
    val rank: Int? = null,
    val timestamp: Long? = null
)

enum class GameStatus { PLAYING, WON, LOST }
