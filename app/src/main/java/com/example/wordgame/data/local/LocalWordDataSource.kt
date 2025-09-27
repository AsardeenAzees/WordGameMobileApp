package com.example.wordgame.data.local

import com.example.wordgame.domain.model.GameWord
import kotlin.random.Random

class LocalWordDataSource {

    private val fallbackWords: Map<Int, List<GameWord>> = mapOf(
        1 to listOf(
            GameWord("planet", "We live on one", difficulty = 1),
            GameWord("guitar", "Six strings of music", difficulty = 1),
            GameWord("forest", "Trees everywhere", difficulty = 1)
        ),
        2 to listOf(
            GameWord("cascade", "Waterfalls do this", difficulty = 2),
            GameWord("quantum", "Physics buzzword", difficulty = 2),
            GameWord("nebula", "Stellar nursery", difficulty = 2)
        ),
        3 to listOf(
            GameWord("xylophone", "Percussive instrument", difficulty = 3),
            GameWord("paradox", "Contradiction in logic", difficulty = 3),
            GameWord("labyrinth", "Complex maze", difficulty = 3)
        )
    )

    fun randomWordForLevel(level: Int): GameWord {
        val pool = fallbackWords[level] ?: fallbackWords[3].orEmpty()
        return pool.randomOrFirst()
    }

    private fun <T> List<T>.randomOrFirst(): T = if (isEmpty()) {
        throw IllegalStateException("Fallback word list is empty")
    } else {
        if (size == 1) first() else this[Random.nextInt(size)]
    }
}
