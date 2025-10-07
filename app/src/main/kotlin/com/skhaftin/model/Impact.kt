package com.skhaftin.model

data class Impact(
    val mealsShared: Int = 0,
    val mealsReceived: Int = 0,
    val co2Saved: Double = 0.0,
    val badges: Map<String, Boolean> = emptyMap()
)
