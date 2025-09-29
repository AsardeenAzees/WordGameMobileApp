package com.example.wordgame

import com.example.wordgame.BuildConfig
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wordgame.data.local.LocalWordDataSource
import com.example.wordgame.data.local.PlayerPreferences
import com.example.wordgame.data.remote.DreamloClient
import com.example.wordgame.data.remote.NetworkModule
import com.example.wordgame.data.repository.LeaderboardRepository
import com.example.wordgame.data.repository.WordRepository
import com.example.wordgame.presentation.game.GameViewModel
import com.example.wordgame.presentation.leaderboard.LeaderboardViewModel
import com.example.wordgame.presentation.onboarding.OnboardingViewModel
import com.example.wordgame.presentation.settings.SettingsViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)  // Increased from 10 to 15 seconds
            .readTimeout(15, TimeUnit.SECONDS)     // Increased from 10 to 15 seconds
            .writeTimeout(15, TimeUnit.SECONDS)    // Added write timeout
            .build()
    }

    // Custom Gson with adapter for Dreamlo response
    private val gson by lazy {
        GsonBuilder()
            .setLenient() // Allow malformed JSON
            .registerTypeAdapter(
                object : TypeToken<List<DreamloEntryDto>>() {}.type,
                DreamloEntryListAdapter()
            )
            .create()
    }
    
    private val dreamloClient by lazy { DreamloClient(okHttpClient, gson) }

    val playerPreferences: PlayerPreferences by lazy { PlayerPreferences(appContext) }

    private val wordRepository: WordRepository by lazy {
        WordRepository(
            randomWordApi = NetworkModule.provideRandomWordApi(),
            clueApi = NetworkModule.provideClueApi(),
            localWordDataSource = LocalWordDataSource()
        )
    }

    private val leaderboardRepository: LeaderboardRepository by lazy {
        LeaderboardRepository(
            dreamloClient = dreamloClient,
            publicCode = BuildConfig.DREAMLO_PUBLIC_CODE,
            privateCode = BuildConfig.DREAMLO_PRIVATE_CODE
        )
    }

    fun onboardingFactory(): ViewModelProvider.Factory = factory { OnboardingViewModel(playerPreferences) }

    fun gameFactory(): ViewModelProvider.Factory = factory {
        GameViewModel(
            wordRepository = wordRepository,
            leaderboardRepository = leaderboardRepository,
            playerPreferences = playerPreferences
        )
    }

    fun leaderboardFactory(): ViewModelProvider.Factory = factory {
        LeaderboardViewModel(leaderboardRepository)
    }

    fun settingsFactory(): ViewModelProvider.Factory = factory { SettingsViewModel(playerPreferences) }

    private inline fun <T : ViewModel> factory(crossinline creator: () -> T): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
        }
    }
}

// Data class for Dreamlo entry
data class DreamloEntryDto(
    val name: String? = null,
    val score: Int? = null,
    val seconds: Int? = null,
    val date: String? = null
)

// Custom adapter to handle both single object and array for Dreamlo entries
class DreamloEntryListAdapter : JsonDeserializer<List<DreamloEntryDto>> {
    override fun deserialize(
        element: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): List<DreamloEntryDto> {
        return when {
            element.isJsonNull -> emptyList()
            element.isJsonArray -> {
                element.asJsonArray.mapNotNull { 
                    if (it.isJsonObject) context.deserialize(it, DreamloEntryDto::class.java) else null
                }
            }
            element.isJsonObject -> {
                listOf(context.deserialize(element, DreamloEntryDto::class.java))
            }
            else -> emptyList()
        }
    }
}