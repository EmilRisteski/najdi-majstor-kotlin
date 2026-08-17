package com.example.najdimajstor.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
)