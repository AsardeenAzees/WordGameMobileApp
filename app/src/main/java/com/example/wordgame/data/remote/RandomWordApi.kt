package com.example.wordgame.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RandomWordApi {
    @GET("word")
    suspend fun getRandomWord(
        @Query("number") number: Int = 1,
        @Query("length") length: Int? = null
    ): List<String>
}
