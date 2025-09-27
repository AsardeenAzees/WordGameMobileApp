package com.example.wordgame.data.repository

import com.example.wordgame.data.local.LocalWordDataSource
import com.example.wordgame.data.remote.ClueApi
import com.example.wordgame.data.remote.RandomWordApi
import com.example.wordgame.domain.model.GameWord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WordRepository(
    private val randomWordApi: RandomWordApi,
    private val clueApi: ClueApi,
    private val localWordDataSource: LocalWordDataSource
) {

    suspend fun fetchWordForLevel(level: Int): GameWord = withContext(Dispatchers.IO) {
        val targetLength = (level + 5).coerceAtMost(12)
        runCatching {
            val remoteWord = randomWordApi.getRandomWord(length = targetLength).firstOrNull()
            if (remoteWord.isNullOrBlank()) throw IllegalStateException("Empty remote response")
            GameWord(remoteWord.lowercase(), difficulty = level)
        }.getOrElse {
            localWordDataSource.randomWordForLevel(level)
        }
    }

    suspend fun fetchTipFor(word: String): String? = withContext(Dispatchers.IO) {
        val safeWord = word.lowercase()
        val synonym = runCatching { clueApi.getSynonyms(safeWord).firstOrNull()?.word }.getOrNull()
        if (!synonym.isNullOrBlank()) return@withContext "Synonym: $synonym"

        val rhyme = runCatching { clueApi.getRhymes(safeWord).firstOrNull()?.word }.getOrNull()
        rhyme?.let { "Rhymes with: $it" }
    }
}
