package com.skhaftin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skhaftin.model.Chat
import com.skhaftin.model.Message
import java.text.SimpleDateFormat
import java.util.*

// It's more efficient to create one instance of SimpleDateFormat and reuse it.
private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

// Centralize UI constants for consistency and easy maintenance.
private object ChatScreenDefaults {
    val SkhaftinYellow = Color(0xFFFFE066)
    val SkhaftinTextPrimary = Color(0xFF333333)
    val SentMessageBubble = Color(0xFFFFF3B0)
    val ReceivedMessageBubble = Color.White

    val PaddingSmall = 8.dp
    val PaddingMedium = 16.dp

    val AvatarSize = 48.dp
    val AvatarCornerRadius = 24.dp

    val BubbleCornerRadius = 16.dp
    val ListItemCornerRadius = 8.dp
}

/**
 * Composable function that displays a single chat item in the chat list.
 * Shows the other participant's name, last message, and timestamp.
 * @param chat The chat to display.
 * @param currentUserId The ID of the current user.
 * @param onClick Callback invoked when the chat item is clicked.
 */
@Composable
fun ChatListItem(
    chat: Chat,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherParticipantName = chat.participants.keys.firstOrNull { it != currentUserId } ?: "Unknown User"
    val lastMessage = chat.messages.values.maxByOrNull { it.timestamp }
    val timeString = lastMessage?.let {
        timeFormatter.format(Date(it.timestamp))
    } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ChatScreenDefaults.PaddingSmall / 2)
            .clickable { onClick() },
        shape = RoundedCornerShape(ChatScreenDefaults.ListItemCornerRadius),
        colors = CardDefaults.cardColors(containerColor = ChatScreenDefaults.ReceivedMessageBubble)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ChatScreenDefaults.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(ChatScreenDefaults.AvatarSize)
                    .background(ChatScreenDefaults.SkhaftinYellow, RoundedCornerShape(ChatScreenDefaults.AvatarCornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChatScreenDefaults.SkhaftinTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(ChatScreenDefaults.PaddingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = otherParticipantName,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 16.sp
                )
                lastMessage?.let {
                    Text(
                        text = it.text,
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = timeString,
                color = Color.DarkGray,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Composable function that displays a message bubble.
 * Different styling for sent and received messages.
 * @param message The message to display.
 * @param isFromCurrentUser Whether the message is from the current user.
 */
@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean
) {
    val alignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (isFromCurrentUser) ChatScreenDefaults.SentMessageBubble else ChatScreenDefaults.ReceivedMessageBubble
    val textColor = Color.Black

    val timeString = timeFormatter.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Card(
            shape = RoundedCornerShape(ChatScreenDefaults.BubbleCornerRadius),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        Text(
            text = timeString,
            fontSize = 10.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(horizontal = ChatScreenDefaults.PaddingSmall, vertical = 2.dp)
        )
    }
}

/**
 * A reusable top bar for chat screens.
 * @param title The title to display.
 * @param onBackClick Callback for the back button.
 */
@Composable
private fun ChatTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatScreenDefaults.SkhaftinYellow)
            .padding(ChatScreenDefaults.PaddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ChatScreenDefaults.SkhaftinTextPrimary
            )
        }
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ChatScreenDefaults.SkhaftinTextPrimary,
            modifier = Modifier.padding(start = ChatScreenDefaults.PaddingMedium)
        )
    }
}

/**
 * Composable function that displays the chat list screen.
 * Shows all chats for the current user with the last message and timestamp.
 * @param chats List of chats to display.
 * @param currentUserId The ID of the current user.
 * @param onChatClick Callback invoked when a chat is clicked.
 * @param onBackClick Callback invoked when the back button is clicked.
 */
@Composable
fun ChatListScreen(
    chats: List<Chat>,
    currentUserId: String,
    onChatClick: (Chat) -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ChatScreenDefaults.SkhaftinYellow
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(
                title = "Messages",
                onBackClick = onBackClick
            )

            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No messages yet",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(ChatScreenDefaults.PaddingMedium)) {
                    items(chats) { chat ->
                        ChatListItem(
                            chat = chat,
                            currentUserId = currentUserId,
                            onClick = { onChatClick(chat) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Composable function that displays the individual chat screen.
 * Shows the conversation with message bubbles and input field.
 * @param chat The current chat.
 * @param messages List of messages in the chat.
 * @param currentUserId The ID of the current user.
 * @param onBackClick Callback invoked when the back button is clicked.
 * @param onSendMessage Callback invoked when sending a message.
 * @param messageInput The current text in the input field.
 * @param onMessageInputChange Callback for when the input text changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chat: Chat,
    messages: List<Message>,
    currentUserId: String,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    messageInput: String,
    onMessageInputChange: (String) -> Unit
) {
    val otherParticipantName = chat.participants.keys.firstOrNull { it != currentUserId } ?: "Unknown User"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ChatScreenDefaults.SkhaftinYellow
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatTopBar(
                title = otherParticipantName,
                onBackClick = onBackClick
            )

            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = ChatScreenDefaults.PaddingMedium),
                reverseLayout = true
            ) {
                // Reversing the list before submitting to LazyColumn is more performant
                // than reversing inside the items block.
                items(messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        isFromCurrentUser = message.senderId == currentUserId
                    )
                }
            }

            // Message input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ChatScreenDefaults.PaddingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = onMessageInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ChatScreenDefaults.SkhaftinYellow,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.width(ChatScreenDefaults.PaddingSmall))

                IconButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            onSendMessage(messageInput.trim())
                        }
                    },
                    enabled = messageInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (messageInput.isNotBlank()) ChatScreenDefaults.SkhaftinTextPrimary else Color.Gray
                    )
                }
            }
        }
    }
}
