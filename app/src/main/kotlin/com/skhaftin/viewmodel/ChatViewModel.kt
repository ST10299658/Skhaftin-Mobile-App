package com.skhaftin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skhaftin.data.DataRepository
import com.skhaftin.model.Chat
import com.skhaftin.model.Message
import com.skhaftin.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = DataRepository()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Expose an immutable StateFlow to the UI.
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // State for the message input field in the UI
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private var messagesJob: Job? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getCurrentUser()?.uid?.let { userId ->
                repository.getUserFromDatabase(userId) { result: Result<User> ->
                    result.onSuccess {
                        _currentUser.value = it
                        loadChatsForUser(it.id)
                    }
                    _isLoading.value = false
                }
            } ?: run { _isLoading.value = false }
        }
    }

    fun loadChatsForUser(userId: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            repository.getChatsForUser(userId) { result ->
                _isLoading.value = false
                result.onSuccess { chatList ->
                    _chats.value = chatList
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load chats"
                }
            }
        }
    }

    fun loadMessagesForChat(chatId: String) {
        // Cancel any previous listeners to avoid multiple subscriptions.
        messagesJob?.cancel()

        _isLoading.value = true
        _error.value = null

        // For real-time updates, the repository should expose a Flow.
        // This assumes a function `getMessagesFlow` exists in DataRepository.
        messagesJob = viewModelScope.launch {
            repository.getMessagesFlow(chatId)
                .catch { exception: Throwable ->
                    _error.value = exception.message ?: "Failed to load messages"
                    _isLoading.value = false
                }
                .collect { messageList: List<Message> ->
                    _messages.value = messageList.distinctBy { it.messageId }.sortedBy { it.timestamp }
                    _isLoading.value = false
                }
        }
    }

    fun sendMessage(chatId: String, text: String, senderId: String) {
        if (text.isBlank()) return

        val message = Message(
            messageId = System.currentTimeMillis().toString(),
            senderId = senderId,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            seen = false
        )

        // Optimistically add the message to the UI
        val currentMessages = _messages.value
        _messages.value = (currentMessages + message).distinctBy { it.messageId }.sortedBy { it.timestamp }

        // Clear the input field
        onMessageInputChange("")

        viewModelScope.launch {
            repository.sendMessage(chatId, message) { result ->
                result.onFailure { exception ->
                    // Remove the message from UI on failure
                    _messages.value = _messages.value.filter { it.messageId != message.messageId }
                    // Restore the input and show an error
                    onMessageInputChange(text)
                    _error.value = exception.message ?: "Failed to send message"
                }
            }
        }
    }

    fun createChat(participants: List<String>) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            repository.createChat(participants) { result ->
                _isLoading.value = false
                result.onSuccess { chat ->
                    _currentChat.value = chat
                    // Reload chats for the current user to see the new chat.
                    participants.firstOrNull()?.let { loadChatsForUser(it) }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Failed to create chat"
                }
            }
        }
    }

    fun selectChat(chat: Chat) {
        _currentChat.value = chat
        loadMessagesForChat(chat.chatId)
    }

    fun onMessageInputChange(newText: String) {
        _messageInput.value = newText
    }

    fun clearError() {
        _error.value = null
    }

}
