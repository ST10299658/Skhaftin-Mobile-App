package com.skhaftin.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val location: String = "",
    val language: String = "",
    val preferences: Map<String, Boolean> = emptyMap(),
    val verified: Boolean = false,
    val impact: Map<String, Any> = emptyMap(),
    val phoneNumber: String = "",
    val token: String = "",
    val loginCount: Int = 0,
    val loginTimes: List<Long> = emptyList()
)
