package com.example.najdimajstor.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

object NotificationTokenRepository {
    fun refreshTokenForCurrentUser() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                saveTokenForCurrentUser(token)
            }
    }

    fun saveTokenForCurrentUser(token: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (token.isBlank()) {
            return
        }

        val tokenData = mapOf(
            "fcmToken" to token,
            "fcmTokenUpdatedAt" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUserId)
            .set(tokenData, SetOptions.merge())
    }
}