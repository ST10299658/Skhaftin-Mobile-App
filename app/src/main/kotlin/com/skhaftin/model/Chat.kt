package com.skhaftin.model

data class Chat(
    val chatId: String = "",
    val participants: Map<String, Boolean> = emptyMap(),
    val messages: Map<String, Message> = emptyMap()
)
