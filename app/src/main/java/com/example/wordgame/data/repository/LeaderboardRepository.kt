package com.example.wordgame.data.repository

import com.example.wordgame.data.remote.DreamloClient
import com.example.wordgame.data.remote.DreamloEntryDto
import com.example.wordgame.domain.model.LeaderboardEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LeaderboardRepository(
    private val dreamloClient: DreamloClient,
    private val publicCode: String,
    private val privateCode: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _entries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val entries: StateFlow<List<LeaderboardEntry>> = _entries.asStateFlow()

    suspend fun submitScore(name: String, score: Int, timeSeconds: Int): Result<Unit> = withContext(dispatcher) {
        if (privateCode.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Dreamlo private code not configured"))
        }
        val sanitizedName = sanitizeName(name)
        if (sanitizedName.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Player name is invalid"))
        }
        val submission = dreamloClient.submitScore(privateCode, sanitizedName, score, timeSeconds.coerceAtLeast(0))
        if (submission.isSuccess) {
            refresh().onFailure { println("Dreamlo refresh failed: ${it.message}") }
        }
        submission
    }

    suspend fun refresh(): Result<List<LeaderboardEntry>> = withContext(dispatcher) {
        if (publicCode.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Dreamlo public code not configured"))
        }
        dreamloClient.fetchLeaderboard(publicCode).map { dtoEntries ->
            val mapped = dtoEntries.mapNotNull { it.toLeaderboardEntry() }
            val sorted = mapped
                .sortedWith(
                    compareByDescending<LeaderboardEntry> { it.score }
                        .thenBy { it.timeSeconds }
                        .thenBy { it.timestamp ?: Long.MAX_VALUE }
                )
                .take(10)
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
            _entries.value = sorted
            sorted
        }
    }

    private fun sanitizeName(raw: String): String {
        return raw.trim()
            .take(20)
            .ifBlank { "Player" }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .ifBlank { "Player" }
    }

    private fun DreamloEntryDto.toLeaderboardEntry(): LeaderboardEntry? {
        val safeName = name?.ifBlank { null } ?: return null
        val safeScore = score ?: return null
        val safeSeconds = seconds ?: Int.MAX_VALUE
        return LeaderboardEntry(
            player = safeName,
            score = safeScore,
            timeSeconds = safeSeconds,
            timestamp = parseTimestamp(date)
        )
    }

    private fun parseTimestamp(dateString: String?): Long? {
        if (dateString.isNullOrBlank()) return null
        val formats = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_DATE_TIME
        )
        for (formatter in formats) {
            try {
                val local = LocalDateTime.parse(dateString, formatter)
                return local.toInstant(ZoneOffset.UTC).toEpochMilli()
            } catch (_: DateTimeParseException) {
                // try next
            }
        }
        return runCatching { Instant.parse(dateString).toEpochMilli() }.getOrNull()
    }
}