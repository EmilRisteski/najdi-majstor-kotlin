package com.example.najdimajstor.data.model

import com.google.firebase.Timestamp

data class Chat(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)