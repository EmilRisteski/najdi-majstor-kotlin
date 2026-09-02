package com.example.najdimajstor.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.model.Chat
import com.example.najdimajstor.data.repository.ChatRepository
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.MainBottomBar
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MessagesScreen(
    onHomeClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onChatClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val chatRepository = remember { ChatRepository() }
    val currentUserId = remember {
        chatRepository.getCurrentUserId().orEmpty()
    }

    val hasCachedDataForCurrentUser = MessagesScreenCache.userId == currentUserId

    var chats by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) {
                MessagesScreenCache.chats
            } else {
                emptyList()
            }
        )
    }

    var isLoading by remember(currentUserId) {
        mutableStateOf(!hasCachedDataForCurrentUser || !MessagesScreenCache.hasLoaded)
    }

    var errorMessage by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) {
                MessagesScreenCache.errorMessage
            } else {
                null
            }
        )
    }

    DisposableEffect(currentUserId) {
        val registration = chatRepository.listenToUserChats { result, error ->
            chats = result
            errorMessage = error
            isLoading = false

            MessagesScreenCache.userId = currentUserId
            MessagesScreenCache.chats = result
            MessagesScreenCache.errorMessage = error
            MessagesScreenCache.hasLoaded = true
        }

        onDispose {
            registration?.remove()
        }
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedItem = BottomNavItem.MESSAGES,
                onHomeClick = onHomeClick,
                onFavoritesClick = onFavoritesClick,
                onMessagesClick = { },
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Пораки",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            when {
                isLoading -> {
                    item {
                        MessagesCenteredState(
                            text = "Се вчитуваат разговори..."
                        )
                    }
                }

                errorMessage != null -> {
                    item {
                        MessagesCenteredState(
                            text = errorMessage ?: "Неуспешно вчитување на пораки.",
                            isError = true
                        )
                    }
                }

                chats.isEmpty() -> {
                    item {
                        EmptyMessagesState()
                    }
                }

                else -> {
                    items(
                        items = chats,
                        key = { chat -> chat.id }
                    ) { chat ->
                        val otherUserId = chat.participantIds
                            .firstOrNull { participantId ->
                                participantId != currentUserId
                            }
                            .orEmpty()

                        val otherUserName = chat.participantNames[otherUserId]
                            .orEmpty()
                            .ifBlank { "Корисник" }

                        ChatListItem(
                            chat = chat,
                            otherUserName = otherUserName,
                            onClick = {
                                if (otherUserId.isNotBlank()) {
                                    onChatClick(otherUserId)
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: Chat,
    otherUserName: String,
    onClick: () -> Unit
) {
    val initials = getInitials(otherUserName)
    val lastMessage = chat.lastMessage.ifBlank { "Нема пораки." }
    val timeText = formatChatTime(chat.lastMessageAt ?: chat.updatedAt)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NajdiGold
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherUserName,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (timeText.isNotBlank()) {
                        Spacer(modifier = Modifier.size(8.dp))

                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = NajdiMutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiMutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyMessagesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = NajdiGold,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Сè уште немаш пораки.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Отвори профил на мајстор и испрати порака за да започнеш разговор.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MessagesCenteredState(
    text: String,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            },
            textAlign = TextAlign.Center
        )
    }
}

private object MessagesScreenCache {
    var userId: String? = null
    var chats: List<Chat> = emptyList()
    var errorMessage: String? = null
    var hasLoaded: Boolean = false
}

private fun getInitials(name: String): String {
    val parts = name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }

    return when {
        parts.size >= 2 -> {
            "${parts[0].first()}${parts[1].first()}".uppercase()
        }

        parts.size == 1 -> {
            parts[0].take(2).uppercase()
        }

        else -> {
            "К"
        }
    }
}

private fun formatChatTime(
    timestamp: com.google.firebase.Timestamp?
): String {
    val date = timestamp?.toDate() ?: return ""

    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
}