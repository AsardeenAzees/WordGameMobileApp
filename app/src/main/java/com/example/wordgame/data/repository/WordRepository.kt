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
            val cleanWord = remoteWord.lowercase().filter { it.isLetter() }
            if (cleanWord.length < 3) throw IllegalStateException("Word too short")
            GameWord(cleanWord, difficulty = level)
        }.getOrElse { exception ->
            // Log the exception for debugging
            println("Failed to fetch remote word: ${exception.message}")
            localWordDataSource.randomWordForLevel(level)
        }
    }

    suspend fun fetchTipFor(word: String): String? = withContext(Dispatchers.IO) {
        val safeWord = word.lowercase().filter { it.isLetter() }
        if (safeWord.length < 3) return@withContext null
        
        val synonym = runCatching { 
            clueApi.getSynonyms(safeWord).firstOrNull()?.word?.filter { it.isLetter() }
        }.getOrElse { exception ->
            println("Failed to fetch synonym: ${exception.message}")
            null
        }
        if (!synonym.isNullOrBlank() && synonym != safeWord) return@withContext "Synonym: $synonym"

        val rhyme = runCatching { 
            clueApi.getRhymes(safeWord).firstOrNull()?.word?.filter { it.isLetter() }
        }.getOrElse { exception ->
            println("Failed to fetch rhyme: ${exception.message}")
            null
        }
        if (!rhyme.isNullOrBlank() && rhyme != safeWord) "Rhymes with: $rhyme" else null
    }
}
