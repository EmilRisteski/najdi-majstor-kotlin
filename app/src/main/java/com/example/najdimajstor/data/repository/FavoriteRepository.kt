package com.example.najdimajstor.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private val cachedFavoriteIdsByUserId = mutableMapOf<String, Set<String>>()
    }

    fun getCachedFavoriteIds(): Set<String>? {
        val userId = auth.currentUser?.uid ?: return null
        return cachedFavoriteIdsByUserId[userId]
    }

    fun getFavoriteIds(
        onResult: (Set<String>, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(emptySet(), "Корисникот не е најавен.")
            return
        }

        val cachedResult = cachedFavoriteIdsByUserId[userId]

        if (cachedResult != null) {
            onResult(cachedResult, null)
        }

        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                val favoriteIds = snapshot.documents.map { document ->
                    document.id
                }.toSet()

                cachedFavoriteIdsByUserId[userId] = favoriteIds
                onResult(favoriteIds, null)
            }
            .addOnFailureListener { exception ->
                if (cachedResult == null) {
                    onResult(
                        emptySet(),
                        exception.message ?: "Неуспешно вчитување на зачувани мајстори."
                    )
                } else {
                    onResult(cachedResult, null)
                }
            }
    }

    fun addFavorite(
        handymanId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        val favoriteData = hashMapOf(
            "handymanId" to handymanId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(handymanId)
            .set(favoriteData)
            .addOnSuccessListener {
                cachedFavoriteIdsByUserId[userId] =
                    cachedFavoriteIdsByUserId[userId].orEmpty() + handymanId

                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message ?: "Мајсторот не беше зачуван.")
            }
    }

    fun removeFavorite(
        handymanId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        firestore.collection("users")
            .document(userId)
            .collection("favorites")
            .document(handymanId)
            .delete()
            .addOnSuccessListener {
                cachedFavoriteIdsByUserId[userId] =
                    cachedFavoriteIdsByUserId[userId].orEmpty() - handymanId

                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message ?: "Мајсторот не беше отстранет од зачувани.")
            }
    }

    fun toggleFavorite(
        handymanId: String,
        isCurrentlyFavorite: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (isCurrentlyFavorite) {
            removeFavorite(handymanId, onResult)
        } else {
            addFavorite(handymanId, onResult)
        }
    }
}