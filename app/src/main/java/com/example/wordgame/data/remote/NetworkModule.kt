package com.example.wordgame.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun baseClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor())
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun loggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(baseClient())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun provideRandomWordApi(): RandomWordApi =
        retrofit("https://random-word-api.herokuapp.com/").create(RandomWordApi::class.java)

    fun provideClueApi(): ClueApi =
        retrofit("https://api.datamuse.com/").create(ClueApi::class.java)
}
