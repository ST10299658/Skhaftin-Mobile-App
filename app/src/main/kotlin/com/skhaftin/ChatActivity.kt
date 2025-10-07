package com.skhaftin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.skhaftin.ui.ChatListScreen
import com.skhaftin.ui.ChatScreen
import com.skhaftin.viewmodel.ChatViewModel

class ChatActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recipientId = intent.getStringExtra("recipientId")

        setContent {
            var currentScreen by remember { mutableStateOf("list") }
            var selectedChat by remember { mutableStateOf<com.skhaftin.model.Chat?>(null) }

            val chats by chatViewModel.chats.collectAsState()
            val currentChat by chatViewModel.currentChat.collectAsState()
            val messages by chatViewModel.messages.collectAsState()
            val messageInput by chatViewModel.messageInput.collectAsState()
            val currentUser by chatViewModel.currentUser.collectAsState()

            // Handle recipientId to start chat
            LaunchedEffect(recipientId, currentUser) {
                val currentUserValue = currentUser
                if (recipientId != null && currentUserValue != null) {
                    val participants = listOf(currentUserValue.id, recipientId).sorted()
                    chatViewModel.createChat(participants)
                    currentScreen = "chat"
                }
            }

            when (currentScreen) {
                "list" -> ChatListScreen(
                    chats = chats,
                    currentUserId = currentUser?.id ?: "",
                    onChatClick = { chat ->
                        chatViewModel.selectChat(chat)
                        selectedChat = chat
                        currentScreen = "chat"
                    },
                    onBackClick = { finish() }
                )
                "chat" -> (selectedChat ?: currentChat)?.let { chat ->
                    ChatScreen(
                        chat = chat,
                        messages = messages,
                        currentUserId = currentUser?.id ?: "",
                        onBackClick = { currentScreen = "list" },
                        onSendMessage = { text ->
                            chatViewModel.sendMessage(chat.chatId, text, currentUser?.id ?: "")
                        },
                        messageInput = messageInput,
                        onMessageInputChange = { chatViewModel.onMessageInputChange(it) }
                    )
                }
            }
        }
    }
}
