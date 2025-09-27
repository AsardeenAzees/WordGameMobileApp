package com.example.wordgame.domain

import com.example.wordgame.domain.model.ClueType
import com.example.wordgame.domain.model.GameConfig
import com.example.wordgame.domain.model.GameStatus
import com.example.wordgame.domain.model.GameWord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val config = GameConfig()

    @Test
    fun wrongGuessConsumesAttemptAndPenalty() {
        val engine = GameEngine(config)
        engine.startNewRound(GameWord("planet"))

        val result = engine.submitGuess("rocket", elapsedSeconds = 5)

        assertTrue(result.isCorrect.not())
        assertEquals(config.maxAttempts - 1, result.remainingAttempts)
        assertEquals(config.startingScore - config.wrongGuessPenalty, result.updatedScore)
    }

    @Test
    fun fastCorrectGuessAwardsBonus() {
        val engine = GameEngine(config)
        engine.startNewRound(GameWord("forest"))

        val result = engine.submitGuess("forest", elapsedSeconds = 10)

        assertTrue(result.isCorrect)
        assertEquals(GameStatus.WON, engine.snapshot(10).status)
        assertEquals(config.startingScore + config.timeBonus, result.updatedScore)
    }

    @Test
    fun cluesApplyPenalty() {
        val engine = GameEngine(config)
        engine.startNewRound(GameWord("cascade"))

        engine.applyClue(ClueType.LETTER_COUNT)
        val snapshot = engine.snapshot(0)

        assertEquals(config.startingScore - config.cluePenalty, snapshot.score)
        assertTrue(snapshot.usedClues.contains(ClueType.LETTER_COUNT))
    }
}
