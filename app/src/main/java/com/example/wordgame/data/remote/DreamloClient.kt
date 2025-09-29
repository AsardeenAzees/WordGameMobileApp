package com.example.wordgame.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DreamloClient(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private companion object {
        private const val BASE_URL = "http://dreamlo.com/lb/"  // Changed to HTTP to match the provided URL format
    }

    suspend fun submitScore(
        privateCode: String,
        name: String,
        score: Int,
        timeSeconds: Int
    ): Result<Unit> = withContext(dispatcher) {
        if (privateCode.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Dreamlo private code missing"))
        }
        val encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8)
        val safeSeconds = timeSeconds.coerceAtLeast(0)
        // Use the full URL format as provided
        val url = "${BASE_URL}${privateCode}/add/$encodedName/$score/$safeSeconds"
        val request = Request.Builder().url(url).get().build()
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Dreamlo submit failed with HTTP ${response.code}")
                }
            }
        }
    }

    suspend fun fetchLeaderboard(publicCode: String): Result<DreamloResponse> = withContext(dispatcher) {
        if (publicCode.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Dreamlo public code missing"))
        }
        // Use HTTP URL format to match the provided URL
        val url = "${BASE_URL}${publicCode}/json"
        val httpUrl = url.toHttpUrlOrNull()
            ?: return@withContext Result.failure(IllegalArgumentException("Invalid Dreamlo URL"))
        val request = Request.Builder().url(httpUrl).get().build()
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Dreamlo fetch failed with HTTP ${response.code}")
                }
                val body = response.body ?: throw IOException("Dreamlo response body missing")
                gson.fromJson(body.charStream(), DreamloResponse::class.java)
            }
        }
    }
}

data class DreamloResponse(
    val dreamlo: DreamloPayload? = null
)

data class DreamloPayload(
    val leaderboard: DreamloLeaderboard? = null
)

data class DreamloLeaderboard(
    val entry: List<DreamloEntryDto>? = null
)

data class DreamloEntryDto(
    val name: String? = null,
    val score: Int? = null,
    val seconds: Int? = null,
    val date: String? = null
)