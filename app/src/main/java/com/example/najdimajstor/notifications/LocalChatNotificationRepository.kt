package com.example.najdimajstor.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.najdimajstor.MainActivity
import com.example.najdimajstor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlin.math.max

object LocalChatNotificationRepository {
    private const val CHANNEL_ID = "local_chat_messages_channel"
    private const val MAX_MESSAGES_PER_NOTIFICATION = 5

    private var listenerRegistration: ListenerRegistration? = null
    private var activeUserId: String? = null
    private var activeChatId: String? = null
    private var hasLoadedInitialChats = false

    private val seenChatUpdateTimes = mutableMapOf<String, Long>()
    private val notificationMessages = mutableMapOf<String, MutableList<String>>()

    fun startListening(context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val appContext = context.applicationContext

        if (listenerRegistration != null && activeUserId == currentUserId) {
            return
        }

        stopListening()

        activeUserId = currentUserId
        hasLoadedInitialChats = false
        seenChatUpdateTimes.clear()
        notificationMessages.clear()

        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("chats")
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                if (!hasLoadedInitialChats) {
                    snapshot.documents.forEach { document ->
                        seenChatUpdateTimes[document.id] = getChatUpdateTime(document)
                    }

                    hasLoadedInitialChats = true
                    return@addSnapshotListener
                }

                snapshot.documentChanges.forEach { change ->
                    if (change.type == DocumentChange.Type.REMOVED) {
                        return@forEach
                    }

                    handleChatChange(
                        context = appContext,
                        document = change.document,
                        currentUserId = currentUserId
                    )
                }
            }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
        activeUserId = null
        activeChatId = null
        hasLoadedInitialChats = false
        seenChatUpdateTimes.clear()
        notificationMessages.clear()
    }

    fun markChatAsOpen(
        context: Context,
        chatId: String
    ) {
        if (chatId.isBlank()) {
            return
        }

        activeChatId = chatId
        clearChatNotification(
            context = context,
            chatId = chatId
        )
    }

    fun markChatAsClosed(chatId: String) {
        if (activeChatId == chatId) {
            activeChatId = null
        }
    }

    private fun handleChatChange(
        context: Context,
        document: QueryDocumentSnapshot,
        currentUserId: String
    ) {
        val chatId = document.id
        val lastMessage = document.getString("lastMessage").orEmpty()
        val lastMessageSenderId = document.getString("lastMessageSenderId").orEmpty()
        val currentUpdateTime = getChatUpdateTime(document)
        val previousUpdateTime = seenChatUpdateTimes[chatId] ?: 0L

        seenChatUpdateTimes[chatId] = max(previousUpdateTime, currentUpdateTime)

        if (lastMessage.isBlank()) {
            return
        }

        if (lastMessageSenderId.isBlank()) {
            return
        }

        if (lastMessageSenderId == currentUserId) {
            return
        }

        if (currentUpdateTime <= previousUpdateTime) {
            return
        }

        if (activeChatId == chatId) {
            clearChatNotification(
                context = context,
                chatId = chatId
            )
            return
        }

        val senderName = getParticipantName(
            document = document,
            participantId = lastMessageSenderId
        )

        addMessageToNotificationStack(
            chatId = chatId,
            message = lastMessage
        )

        showNotification(
            context = context,
            chatId = chatId,
            title = senderName
        )
    }

    private fun addMessageToNotificationStack(
        chatId: String,
        message: String
    ) {
        val messages = notificationMessages.getOrPut(chatId) {
            mutableListOf()
        }

        messages.add(message)

        while (messages.size > MAX_MESSAGES_PER_NOTIFICATION) {
            messages.removeAt(0)
        }
    }

    private fun clearChatNotification(
        context: Context,
        chatId: String
    ) {
        notificationMessages.remove(chatId)

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.cancel(getNotificationId(chatId))
    }

    private fun getChatUpdateTime(
        document: com.google.firebase.firestore.DocumentSnapshot
    ): Long {
        return document.getTimestamp("lastMessageAt")?.toDate()?.time
            ?: document.getTimestamp("updatedAt")?.toDate()?.time
            ?: 0L
    }

    private fun getParticipantName(
        document: QueryDocumentSnapshot,
        participantId: String
    ): String {
        val participantNames = document.get("participantNames") as? Map<*, *>

        return participantNames
            ?.get(participantId)
            ?.toString()
            .orEmpty()
            .ifBlank { "Нова порака" }
    }

    private fun showNotification(
        context: Context,
        chatId: String,
        title: String
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)

        val messages = notificationMessages[chatId].orEmpty()
        val latestMessage = messages.lastOrNull().orEmpty()

        if (latestMessage.isBlank()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            getNotificationId(chatId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)

        messages.forEach { message ->
            inboxStyle.addLine(message)
        }

        if (messages.size > 1) {
            inboxStyle.setSummaryText("${messages.size} нови пораки")
        }

        val contentText = if (messages.size > 1) {
            "${messages.size} нови пораки"
        } else {
            latestMessage
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setNumber(messages.size)
            .setOnlyAlertOnce(false)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.notify(
            getNotificationId(chatId),
            notification
        )
    }

    private fun getNotificationId(chatId: String): Int {
        return chatId.hashCode()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Пораки",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Известувања за нови пораки"
            }

            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }
}