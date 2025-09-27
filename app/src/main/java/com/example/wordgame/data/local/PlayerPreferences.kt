package com.example.wordgame.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.wordgame.domain.model.PlayerProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class PlayerPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun readProfile(): PlayerProfile = PlayerProfile(
        name = prefs.getString(KEY_PLAYER_NAME, "") ?: "",
        bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
        hasOnboarded = prefs.getBoolean(KEY_ONBOARDED, false)
    )

    fun observeProfile(): Flow<PlayerProfile> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readProfile()).isSuccess
        }
        trySend(readProfile()).isSuccess
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    suspend fun saveName(name: String) {
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putString(KEY_PLAYER_NAME, name)
                .putBoolean(KEY_ONBOARDED, true)
                .apply()
        }
    }

    suspend fun updateBestScore(score: Int) {
        withContext(Dispatchers.IO) {
            val current = prefs.getInt(KEY_BEST_SCORE, 0)
            if (score > current) {
                prefs.edit().putInt(KEY_BEST_SCORE, score).apply()
            }
        }
    }

    suspend fun reset() {
        withContext(Dispatchers.IO) { prefs.edit().clear().apply() }
    }

    companion object {
        private const val PREFS_NAME = "word_game_preferences"
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_ONBOARDED = "has_onboarded"
    }
}
