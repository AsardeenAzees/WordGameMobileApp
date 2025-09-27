package com.example.wordgame.data.repository

import com.example.wordgame.data.remote.DreamloApi
import com.example.wordgame.data.remote.DreamloEntry
import com.example.wordgame.domain.model.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LeaderboardRepository(
    private val dreamloApi: DreamloApi,
    private val publicCode: String,
    private val privateCode: String
) {

    suspend fun submitScore(name: String, score: Int): Boolean = withContext(Dispatchers.IO) {
        if (publicCode.isBlank() || privateCode.isBlank()) return@withContext false
        runCatching {
            dreamloApi.submitScore(privateCode, name.trim(), score)
            true
        }.getOrElse { false }
    }

    suspend fun fetchLeaderboard(): List<LeaderboardEntry> = withContext(Dispatchers.IO) {
        if (publicCode.isBlank()) return@withContext emptyList()
        runCatching {
            val response = dreamloApi.fetchScores(publicCode)
            val entries: List<DreamloEntry> = response.dreamlo?.leaderboard?.entry.orEmpty()
            entries.sortedByDescending { it.score }
                .mapIndexed { index, item ->
                    LeaderboardEntry(
                        player = item.name,
                        score = item.score,
                        rank = index + 1,
                        timestamp = item.timestamp?.toLongOrNull()
                    )
                }
        }.getOrElse { emptyList() }
    }
}
