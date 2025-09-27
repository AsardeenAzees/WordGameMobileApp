package com.example.wordgame.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface ClueApi {
    @GET("words")
    suspend fun getSynonyms(
        @Query("rel_syn") word: String,
        @Query("max") max: Int = 5
    ): List<ClueWordDto>

    @GET("words")
    suspend fun getRhymes(
        @Query("rel_rhy") word: String,
        @Query("max") max: Int = 5
    ): List<ClueWordDto>
}

@Serializable
data class ClueWordDto(
    val word: String,
    val score: Int? = null
)
