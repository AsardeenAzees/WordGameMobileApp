package com.example.wordgame

import com.example.wordgame.BuildConfig

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wordgame.data.local.LocalWordDataSource
import com.example.wordgame.data.local.PlayerPreferences
import com.example.wordgame.data.remote.NetworkModule
import com.example.wordgame.data.repository.LeaderboardRepository
import com.example.wordgame.data.repository.WordRepository
import com.example.wordgame.presentation.game.GameViewModel
import com.example.wordgame.presentation.leaderboard.LeaderboardViewModel
import com.example.wordgame.presentation.onboarding.OnboardingViewModel
import com.example.wordgame.presentation.settings.SettingsViewModel

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

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
            dreamloApi = NetworkModule.provideDreamloApi(),
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

    fun settingsFactory(): ViewModelProvider.Factory = factory {
        SettingsViewModel(playerPreferences)
    }

    private fun <T : ViewModel> factory(creator: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T1 : ViewModel> create(modelClass: Class<T1>): T1 {
                val viewModel = creator.invoke()
                if (modelClass.isAssignableFrom(viewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return viewModel as T1
                }
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
}
