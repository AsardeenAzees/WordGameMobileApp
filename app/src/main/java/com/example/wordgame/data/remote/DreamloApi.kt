package com.example.wordgame.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

interface DreamloApi {
    @GET("{privateCode}/add/{player}/{score}")
    suspend fun submitScore(
        @Path("privateCode") privateCode: String,
        @Path("player") player: String,
        @Path("score") score: Int
    ): DreamloSubmitResponse?

    @GET("{publicCode}/json")
    suspend fun fetchScores(
        @Path("publicCode") publicCode: String
    ): DreamloLeaderboardResponse
}

@Serializable
data class DreamloSubmitResponse(
    val success: Boolean? = null
)

@Serializable
data class DreamloLeaderboardResponse(
    val dreamlo: DreamloResult? = null
)

@Serializable
data class DreamloResult(
    val leaderboard: DreamloLeaderboard? = null
)

@Serializable
data class DreamloLeaderboard(
    val entry: List<DreamloEntry> = emptyList()
)

@Serializable
data class DreamloEntry(
    val name: String,
    val score: Int,
    @SerialName("seconds") val seconds: Int? = null,
    @SerialName("when") val timestamp: String? = null
)
