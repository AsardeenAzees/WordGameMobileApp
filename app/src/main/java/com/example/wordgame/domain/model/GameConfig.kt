package com.example.wordgame.domain.model

data class GameConfig(
    val startingScore: Int = 100,
    val maxAttempts: Int = 10,
    val wrongGuessPenalty: Int = 10,
    val cluePenalty: Int = 5,
    val timeBonusThresholdSeconds: Int = 30,
    val timeBonus: Int = 15
)
