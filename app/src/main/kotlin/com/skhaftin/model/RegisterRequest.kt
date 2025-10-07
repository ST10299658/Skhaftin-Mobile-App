package com.skhaftin.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val phoneNumber: String?,
    val location: String?,
    val preferredLanguage: String?
)
