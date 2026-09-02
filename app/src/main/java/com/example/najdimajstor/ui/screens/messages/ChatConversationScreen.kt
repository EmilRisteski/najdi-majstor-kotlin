package com.example.najdimajstor.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.model.ChatMessage
import com.example.najdimajstor.data.repository.ChatRepository
import com.example.najdimajstor.notifications.LocalChatNotificationRepository
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.example.najdimajstor.ui.theme.NajdiNavy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DELETED_USER_NAME = "Избришан корисник"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatConversationScreen(
    otherUserId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val chatRepository = remember { ChatRepository() }
    val currentUserId = remember {
        chatRepository.getCurrentUserId().orEmpty()
    }

    var chatId by remember { mutableStateOf<String?>(null) }
    var otherUserName by remember { mutableStateOf("Корисник") }
    var isOtherUserDeleted by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }

    var newMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val messageFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun scrollToBottom() {
        if (messages.isEmpty()) return

        coroutineScope.launch {
            delay(160)
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    fun keepKeyboardOpen() {
        messageFocusRequester.requestFocus()
        keyboardController?.show()
        scrollToBottom()
    }

    fun sendCurrentMessage() {
        val currentChatId = chatId
        val messageText = newMessage.trim()

        when {
            isOtherUserDeleted -> {
                errorMessage = "Овој профил е избришан. Не можеш да испраќаш нови пораки."
                return
            }

            currentChatId.isNullOrBlank() -> {
                errorMessage = "Разговорот не е подготвен."
                return
            }

            messageText.isBlank() -> {
                keepKeyboardOpen()
                return
            }

            isSending -> {
                keepKeyboardOpen()
                return
            }

            else -> {
                isSending = true
                errorMessage = null

                chatRepository.sendMessage(
                    chatId = currentChatId,
                    text = messageText
                ) { success, error ->
                    isSending = false

                    if (success) {
                        if (newMessage.trim() == messageText) {
                            newMessage = ""
                        }

                        errorMessage = null
                        keepKeyboardOpen()
                    } else {
                        errorMessage = error ?: "Пораката не беше испратена."
                        keepKeyboardOpen()
                    }
                }
            }
        }
    }

    LaunchedEffect(otherUserId) {
        isLoading = true
        errorMessage = null
        isOtherUserDeleted = false

        chatRepository.getOtherUserDisplayName(otherUserId) { name ->
            otherUserName = name
        }

        chatRepository.getOrCreateChat(otherUserId) { resultChatId, error ->
            chatId = resultChatId
            errorMessage = error
            isLoading = false
        }
    }

    DisposableEffect(chatId) {
        val currentChatId = chatId

        if (!currentChatId.isNullOrBlank()) {
            LocalChatNotificationRepository.markChatAsOpen(
                context = context,
                chatId = currentChatId
            )
        }

        onDispose {
            if (!currentChatId.isNullOrBlank()) {
                LocalChatNotificationRepository.markChatAsClosed(currentChatId)
            }
        }
    }

    DisposableEffect(chatId, otherUserId) {
        val currentChatId = chatId

        val registration = if (!currentChatId.isNullOrBlank()) {
            chatRepository.listenToChat(currentChatId) { chat, error ->
                val nameFromChat = chat
                    ?.participantNames
                    ?.get(otherUserId)
                    .orEmpty()

                if (nameFromChat == DELETED_USER_NAME) {
                    otherUserName = DELETED_USER_NAME
                    isOtherUserDeleted = true
                    newMessage = ""
                } else if (nameFromChat.isNotBlank() && nameFromChat != "Корисник") {
                    otherUserName = nameFromChat
                    isOtherUserDeleted = false
                }

                if (error != null) {
                    errorMessage = error
                }
            }
        } else {
            null
        }

        onDispose {
            registration?.remove()
        }
    }

    DisposableEffect(chatId) {
        val currentChatId = chatId

        val registration = if (!currentChatId.isNullOrBlank()) {
            chatRepository.listenToMessages(currentChatId) { result, error ->
                messages = result

                if (error != null) {
                    errorMessage = error
                }
            }
        } else {
            null
        }

        onDispose {
            registration?.remove()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            ChatHeader(
                title = otherUserName,
                onBackClick = onBackClick
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        ChatCenteredMessage(
                            text = "Се отвора разговорот..."
                        )
                    }

                    messages.isEmpty() && errorMessage == null -> {
                        ChatCenteredMessage(
                            text = "Нема пораки. Испрати ја првата порака."
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                space = 10.dp,
                                alignment = Alignment.Bottom
                            )
                        ) {
                            items(
                                items = messages,
                                key = { message -> message.id }
                            ) { message ->
                                MessageBubble(
                                    message = message,
                                    isMine = message.senderId == currentUserId
                                )
                            }
                        }
                    }
                }
            }

            if (isOtherUserDeleted) {
                DeletedUserNotice()
            } else {
                ChatInputBar(
                    value = newMessage,
                    onValueChange = { newMessage = it },
                    messageFocusRequester = messageFocusRequester,
                    keyboardController = keyboardController,
                    isSending = isSending,
                    enabled = chatId != null && !isLoading,
                    onInputFocused = {
                        scrollToBottom()
                    },
                    onSendClick = {
                        sendCurrentMessage()
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Пораки",
                    style = MaterialTheme.typography.bodySmall,
                    color = NajdiMutedText
                )
            }
        }
    }
}

@Composable
private fun ChatCenteredMessage(
    text: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 28.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean
) {
    val bubbleColor = if (isMine) {
        NajdiGold
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isMine) {
        NajdiNavy
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMine) 18.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor,
                contentColor = textColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
private fun DeletedUserNotice(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = "Овој профил е избришан. Не можеш да испраќаш нови пораки.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = NajdiMutedText,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    messageFocusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    isSending: Boolean,
    enabled: Boolean,
    onInputFocused: () -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(messageFocusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onInputFocused()
                        }
                    },
                enabled = enabled,
                placeholder = {
                    Text(text = "Напиши порака...")
                },
                singleLine = false,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onSendClick()
                        messageFocusRequester.requestFocus()
                        keyboardController?.show()
                    }
                )
            )

            IconButton(
                onClick = {
                    onSendClick()
                    messageFocusRequester.requestFocus()
                    keyboardController?.show()
                },
                enabled = enabled && !isSending && value.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Испрати",
                    tint = if (enabled && !isSending && value.isNotBlank()) {
                        NajdiGold
                    } else {
                        NajdiMutedText
                    }
                )
            }
        }
    }
}