package com.example.najdimajstor.data.repository

import com.example.najdimajstor.data.model.Chat
import com.example.najdimajstor.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getOrCreateChat(
        otherUserId: String,
        onResult: (String?, String?) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onResult(null, "Корисникот не е најавен.")
            return
        }

        when {
            otherUserId.isBlank() -> {
                onResult(null, "Корисникот не е пронајден.")
                return
            }

            currentUserId == otherUserId -> {
                onResult(null, "Не можеш да испратиш порака до самиот себе.")
                return
            }
        }

        val chatId = createChatId(currentUserId, otherUserId)
        val chatReference = firestore.collection("chats").document(chatId)

        chatReference.get()
            .addOnSuccessListener { existingChat ->
                if (existingChat.exists()) {
                    onResult(chatId, null)
                    return@addOnSuccessListener
                }

                createNewChat(
                    chatId = chatId,
                    currentUserId = currentUserId,
                    otherUserId = otherUserId,
                    onResult = onResult
                )
            }
            .addOnFailureListener { exception ->
                onResult(
                    null,
                    exception.message ?: "Неуспешно отворање на разговорот."
                )
            }
    }

    fun listenToChat(
        chatId: String,
        onResult: (Chat?, String?) -> Unit
    ): ListenerRegistration? {
        if (chatId.isBlank()) {
            onResult(null, "Разговорот не е пронајден.")
            return null
        }

        return firestore.collection("chats")
            .document(chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(
                        null,
                        error.message ?: "Неуспешно вчитување на разговорот."
                    )
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    onResult(snapshot.toChat(), null)
                } else {
                    onResult(null, "Разговорот не е пронајден.")
                }
            }
    }

    fun listenToUserChats(
        onResult: (List<Chat>, String?) -> Unit
    ): ListenerRegistration? {
        val currentUserId = auth.currentUser?.uid ?: run {
            onResult(emptyList(), "Корисникот не е најавен.")
            return null
        }

        return firestore.collection("chats")
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(
                        emptyList(),
                        error.message ?: "Неуспешно вчитување на пораки."
                    )
                    return@addSnapshotListener
                }

                val chats = snapshot
                    ?.documents
                    ?.map { document -> document.toChat() }
                    ?.sortedByDescending { chat ->
                        chat.updatedAt?.seconds ?: 0L
                    }
                    ?: emptyList()

                onResult(chats, null)
            }
    }

    fun listenToMessages(
        chatId: String,
        onResult: (List<ChatMessage>, String?) -> Unit
    ): ListenerRegistration? {
        if (chatId.isBlank()) {
            onResult(emptyList(), "Разговорот не е пронајден.")
            return null
        }

        return firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(
                        emptyList(),
                        error.message ?: "Неуспешно вчитување на разговорот."
                    )
                    return@addSnapshotListener
                }

                val messages = snapshot
                    ?.documents
                    ?.map { document -> document.toChatMessage(chatId) }
                    ?: emptyList()

                onResult(messages, null)
            }
    }

    fun sendMessage(
        chatId: String,
        text: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        val trimmedText = text.trim()

        when {
            chatId.isBlank() -> {
                onResult(false, "Разговорот не е пронајден.")
                return
            }

            trimmedText.isBlank() -> {
                onResult(false, "Внеси порака.")
                return
            }

            trimmedText.length > 1000 -> {
                onResult(false, "Пораката е предолга.")
                return
            }
        }

        val chatReference = firestore.collection("chats").document(chatId)

        val messageReference = chatReference
            .collection("messages")
            .document()

        val messageData = hashMapOf(
            "senderId" to currentUserId,
            "text" to trimmedText,
            "createdAt" to FieldValue.serverTimestamp()
        )

        val chatUpdateData = mapOf(
            "lastMessage" to trimmedText,
            "lastMessageAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.runBatch { batch ->
            batch.set(messageReference, messageData)
            batch.update(chatReference, chatUpdateData)
        }.addOnSuccessListener {
            onResult(true, null)
        }.addOnFailureListener { exception ->
            onResult(
                false,
                exception.message ?: "Пораката не беше испратена."
            )
        }
    }

    fun getOtherUserDisplayName(
        otherUserId: String,
        onResult: (String) -> Unit
    ) {
        if (otherUserId.isBlank()) {
            onResult("Корисник")
            return
        }

        firestore.collection("handymen")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { handymanDocument ->
                val handymanName = handymanDocument
                    .getString("name")
                    .orEmpty()

                if (handymanName.isNotBlank()) {
                    onResult(handymanName)
                } else {
                    onResult("Корисник")
                }
            }
            .addOnFailureListener {
                onResult("Корисник")
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun createNewChat(
        chatId: String,
        currentUserId: String,
        otherUserId: String,
        onResult: (String?, String?) -> Unit
    ) {
        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { currentUserDocument ->
                val currentUserName = currentUserDocument
                    .getString("fullName")
                    .orEmpty()
                    .ifBlank {
                        auth.currentUser?.email.orEmpty().ifBlank { "Корисник" }
                    }

                firestore.collection("handymen")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener { handymanDocument ->
                        val otherUserName = handymanDocument
                            .getString("name")
                            .orEmpty()
                            .ifBlank { "Мајстор" }

                        val chatData = hashMapOf(
                            "participantIds" to listOf(currentUserId, otherUserId).sorted(),
                            "participantNames" to mapOf(
                                currentUserId to currentUserName,
                                otherUserId to otherUserName
                            ),
                            "lastMessage" to "",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("chats")
                            .document(chatId)
                            .set(chatData)
                            .addOnSuccessListener {
                                onResult(chatId, null)
                            }
                            .addOnFailureListener { exception ->
                                onResult(
                                    null,
                                    exception.message ?: "Разговорот не беше креиран."
                                )
                            }
                    }
                    .addOnFailureListener { exception ->
                        onResult(
                            null,
                            exception.message ?: "Мајсторот не беше пронајден."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onResult(
                    null,
                    exception.message ?: "Корисничкиот профил не беше пронајден."
                )
            }
    }

    private fun createChatId(
        firstUserId: String,
        secondUserId: String
    ): String {
        return listOf(firstUserId, secondUserId)
            .sorted()
            .joinToString("_")
    }

    private fun DocumentSnapshot.toChat(): Chat {
        val participantIds = get("participantIds") as? List<*>
        val participantNames = get("participantNames") as? Map<*, *>

        return Chat(
            id = id,
            participantIds = participantIds
                ?.mapNotNull { it as? String }
                ?: emptyList(),
            participantNames = participantNames
                ?.mapNotNull { entry ->
                    val key = entry.key as? String
                    val value = entry.value as? String

                    if (key != null && value != null) {
                        key to value
                    } else {
                        null
                    }
                }
                ?.toMap()
                ?: emptyMap(),
            lastMessage = getString("lastMessage").orEmpty(),
            lastMessageAt = getTimestamp("lastMessageAt"),
            updatedAt = getTimestamp("updatedAt")
        )
    }

    private fun DocumentSnapshot.toChatMessage(
        chatId: String
    ): ChatMessage {
        return ChatMessage(
            id = id,
            chatId = chatId,
            senderId = getString("senderId").orEmpty(),
            text = getString("text").orEmpty(),
            createdAt = getTimestamp("createdAt")
        )
    }
}