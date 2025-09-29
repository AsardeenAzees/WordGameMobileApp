package com.example.wordgame.data.remote

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

class DreamloClient(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private companion object {
        private const val BASE_URL = "http://dreamlo.com/lb/"
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
        val url = "${BASE_URL}${privateCode}/add/$encodedName/$score/$safeSeconds"
        val request = Request.Builder().url(url).get().build()
        runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Dreamlo submit failed with HTTP ${response.code}")
                }
            }
        }.recoverCatching { exception ->
            // Better error handling for network issues
            when (exception) {
                is UnknownHostException -> {
                    throw IOException("Unable to resolve dreamlo.com. Please check your internet connection.", exception)
                }
                else -> throw exception
            }
        }
    }

    suspend fun fetchLeaderboard(publicCode: String): Result<List<DreamloEntryDto>> = withContext(dispatcher) {
        if (publicCode.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Dreamlo public code missing"))
        }
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
                val jsonElement = gson.fromJson(body.charStream(), JsonElement::class.java)
                
                // Handle the Dreamlo JSON structure directly
                val entries = parseDreamloResponse(jsonElement)
                entries
            }
        }.recoverCatching { exception ->
            // Better error handling for network issues
            when (exception) {
                is UnknownHostException -> {
                    throw IOException("Unable to resolve dreamlo.com. Please check your internet connection.", exception)
                }
                is JsonSyntaxException -> {
                    // Handle malformed JSON gracefully
                    throw IOException("Dreamlo returned malformed JSON. This is temporary and will resolve on refresh.", exception)
                }
                else -> throw exception
            }
        }
    }
    
    private fun parseDreamloResponse(element: JsonElement): List<DreamloEntryDto> {
        if (!element.isJsonObject) return emptyList()
        
        val jsonObject = element.asJsonObject
        val dreamloObject = jsonObject.getAsJsonObject("dreamlo") ?: return emptyList()
        val leaderboardObject = dreamloObject.getAsJsonObject("leaderboard") ?: return emptyList()
        
        // Handle both single object and array for entry field
        return when {
            leaderboardObject.has("entry") && !leaderboardObject.get("entry").isJsonNull -> {
                val entryElement = leaderboardObject.get("entry")
                when {
                    entryElement.isJsonArray -> {
                        val type = object : TypeToken<List<DreamloEntryDto>>() {}.type
                        gson.fromJson(entryElement, type)
                    }
                    entryElement.isJsonObject -> {
                        listOf(gson.fromJson(entryElement, DreamloEntryDto::class.java))
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
}

data class DreamloEntryDto(
    val name: String? = null,
    val score: Int? = null,
    val seconds: Int? = null,
    val date: String? = null
)