package com.example.wordgame.domain

import com.example.wordgame.domain.model.GameConfig
import com.example.wordgame.domain.model.GameStatus
import com.example.wordgame.domain.model.GameWord
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GameEngineTest {
    private lateinit var engine: GameEngine
    private val config = GameConfig(
        startingScore = 100,
        maxAttempts = 3,
        wrongGuessPenalty = 10,
        cluePenalty = 5,
        timeBonusThresholdSeconds = 30,
        timeBonus = 15
    )
    private val testWord = GameWord("testing", "A word used for testing", 1)

    @Before
    fun setup() {
        engine = GameEngine(config)
        engine.startNewRound(testWord, config.startingScore)
    }

    @Test
    fun `wrong guess should deduct 10 points`() {
        val initialScore = config.startingScore
        val result = engine.submitGuess("wrong", 5)
        
        assertEquals(false, result.isCorrect)
        assertEquals(initialScore - config.wrongGuessPenalty, result.updatedScore)
        assertEquals("Wrong guess. -${config.wrongGuessPenalty} points.", result.message)
    }

    @Test
    fun `multiple wrong guesses should deduct points correctly`() {
        // First wrong guess
        var result = engine.submitGuess("wrong1", 5)
        assertEquals(90, result.updatedScore) // 100 - 10
        
        // Second wrong guess
        result = engine.submitGuess("wrong2", 10)
        assertEquals(80, result.updatedScore) // 90 - 10
        
        // Third wrong guess (should end game)
        result = engine.submitGuess("wrong3", 15)
        assertEquals(70, result.updatedScore) // 80 - 10
        assertEquals(GameStatus.LOST, engine.status())
    }

    @Test
    fun `correct guess should not deduct points`() {
        val initialScore = config.startingScore
        val result = engine.submitGuess("testing", 35) // Use time > threshold to avoid bonus
        
        assertEquals(true, result.isCorrect)
        assertEquals(initialScore, result.updatedScore) // No penalty for correct guess
    }
}