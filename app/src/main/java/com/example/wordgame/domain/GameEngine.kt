package com.example.wordgame.domain

import com.example.wordgame.domain.model.ClueResult
import com.example.wordgame.domain.model.ClueType
import com.example.wordgame.domain.model.GameConfig
import com.example.wordgame.domain.model.GameSnapshot
import com.example.wordgame.domain.model.GameStatus
import com.example.wordgame.domain.model.GameWord
import com.example.wordgame.domain.model.GuessResult
import kotlin.math.max

class GameEngine(
    private val config: GameConfig = GameConfig()
) {
    private lateinit var currentWord: GameWord
    private var attemptsLeft: Int = config.maxAttempts
    private var score: Int = config.startingScore
    private var wrongGuesses: Int = 0
    private val usedClues = mutableSetOf<ClueType>()
    private val revealedLetters = mutableSetOf<Char>()
    private var status: GameStatus = GameStatus.PLAYING

    fun startNewRound(word: GameWord, seedScore: Int? = null) {
        currentWord = word
        attemptsLeft = config.maxAttempts
        score = seedScore ?: config.startingScore
        wrongGuesses = 0
        usedClues.clear()
        revealedLetters.clear()
        status = GameStatus.PLAYING
    }

    fun submitGuess(guess: String, elapsedSeconds: Int): GuessResult {
        if (!::currentWord.isInitialized) error("Call startNewRound first")
        if (status != GameStatus.PLAYING) {
            return GuessResult(
                isCorrect = false,
                remainingAttempts = attemptsLeft,
                updatedScore = score,
                message = "Round already finished"
            )
        }

        val normalizedGuess = guess.trim().lowercase()
        if (normalizedGuess == currentWord.value.lowercase()) {
            status = GameStatus.WON
            val bonus = if (elapsedSeconds <= config.timeBonusThresholdSeconds) config.timeBonus else 0
            score += bonus
            return GuessResult(
                isCorrect = true,
                remainingAttempts = attemptsLeft,
                updatedScore = score,
                message = if (bonus > 0) "Correct! Speed bonus +$bonus." else "Correct!"
            )
        }

        wrongGuesses += 1
        attemptsLeft = max(attemptsLeft - 1, 0)
        score = max(score - config.wrongGuessPenalty, 0)
        if (attemptsLeft == 0) {
            status = GameStatus.LOST
        }

        return GuessResult(
            isCorrect = false,
            remainingAttempts = attemptsLeft,
            updatedScore = score,
            message = if (status == GameStatus.LOST) {
                "No attempts left. The word was \"${currentWord.value}\"."
            } else {
                "Wrong guess. -${config.wrongGuessPenalty} points."
            }
        )
    }

    fun applyClue(type: ClueType): ClueResult? {
        if (!::currentWord.isInitialized) error("Call startNewRound first")
        if (!canUseClue(type) || status != GameStatus.PLAYING) return null

        usedClues += type
        score = max(score - config.cluePenalty, 0)
        val message = when (type) {
            ClueType.LETTER_COUNT -> "The word has ${currentWord.value.length} letters."
            ClueType.LETTER_OCCURRENCE -> {
                val letterToReveal = currentWord.value.firstOrNull { !revealedLetters.contains(it) }
                    ?: currentWord.value.random()
                revealedLetters += letterToReveal
                val occurrences = currentWord.value.count { it.equals(letterToReveal, ignoreCase = true) }
                "The letter '${letterToReveal.uppercaseChar()}' appears $occurrences time(s)."
            }
            ClueType.WORD_TIP -> currentWord.hint ?: "Keep trying!"
        }
        return ClueResult(type = type, message = message, cost = config.cluePenalty)
    }

    private fun canUseClue(type: ClueType): Boolean {
        if (usedClues.contains(type)) return false
        return when (type) {
            ClueType.LETTER_COUNT -> true
            ClueType.LETTER_OCCURRENCE -> true
            ClueType.WORD_TIP -> wrongGuesses >= 5
        }
    }

    fun snapshot(elapsedSeconds: Int): GameSnapshot = GameSnapshot(
        word = currentWord,
        obscuredWord = obscureWord(),
        attemptsLeft = attemptsLeft,
        score = score,
        elapsedSeconds = elapsedSeconds,
        usedClues = usedClues.toSet(),
        status = status
    )

    fun status(): GameStatus = status

    private fun obscureWord(): String {
        return if (status == GameStatus.WON) {
            currentWord.value
        } else {
            currentWord.value.map { char ->
                if (revealedLetters.contains(char) || char == '-') char else '_'
            }.joinToString(" ")
        }
    }
}
