package com.example.najdimajstor.data.repository

import com.example.najdimajstor.data.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlin.math.roundToInt

class ReviewRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun listenToReviews(
        handymanId: String,
        onResult: (List<Review>, String?) -> Unit
    ): ListenerRegistration? {
        if (handymanId.isBlank()) {
            onResult(emptyList(), "Мајсторот не е пронајден.")
            return null
        }

        return firestore.collection("handymen")
            .document(handymanId)
            .collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onResult(
                        emptyList(),
                        error.message ?: "Неуспешно вчитување на рецензии."
                    )
                    return@addSnapshotListener
                }

                val reviews = snapshot
                    ?.documents
                    ?.map { document -> document.toReview(handymanId) }
                    ?: emptyList()

                onResult(reviews, null)
            }
    }

    fun saveReview(
        handymanId: String,
        rating: Int,
        comment: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        if (handymanId.isBlank()) {
            onResult(false, "Мајсторот не е пронајден.")
            return
        }

        if (currentUserId == handymanId) {
            onResult(false, "Не можеш да оставиш рецензија на сопствениот профил.")
            return
        }

        if (rating !in 1..5) {
            onResult(false, "Избери оцена од 1 до 5.")
            return
        }

        val trimmedComment = comment.trim()

        if (trimmedComment.length > 500) {
            onResult(false, "Коментарот е предолг.")
            return
        }

        firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { userDocument ->
                val reviewerName = userDocument
                    .getString("fullName")
                    .orEmpty()
                    .ifBlank {
                        currentUser.email.orEmpty().ifBlank { "Корисник" }
                    }

                saveReviewWithName(
                    handymanId = handymanId,
                    reviewerId = currentUserId,
                    reviewerName = reviewerName,
                    rating = rating,
                    comment = trimmedComment,
                    onResult = onResult
                )
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Неуспешно вчитување на корисникот."
                )
            }
    }

    private fun saveReviewWithName(
        handymanId: String,
        reviewerId: String,
        reviewerName: String,
        rating: Int,
        comment: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val reviewReference = firestore.collection("handymen")
            .document(handymanId)
            .collection("reviews")
            .document(reviewerId)

        reviewReference.get()
            .addOnSuccessListener { existingReview ->
                val reviewData = hashMapOf<String, Any>(
                    "handymanId" to handymanId,
                    "reviewerId" to reviewerId,
                    "reviewerName" to reviewerName,
                    "rating" to rating,
                    "comment" to comment,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                if (!existingReview.exists()) {
                    reviewData["createdAt"] = FieldValue.serverTimestamp()
                }

                reviewReference
                    .set(reviewData, SetOptions.merge())
                    .addOnSuccessListener {
                        recalculateHandymanRating(
                            handymanId = handymanId,
                            onResult = onResult
                        )
                    }
                    .addOnFailureListener { exception ->
                        onResult(
                            false,
                            exception.message ?: "Неуспешно зачувување на рецензијата."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Неуспешно вчитување на рецензијата."
                )
            }
    }

    private fun recalculateHandymanRating(
        handymanId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firestore.collection("handymen")
            .document(handymanId)
            .collection("reviews")
            .get()
            .addOnSuccessListener { snapshot ->
                val ratings = snapshot.documents.mapNotNull { document ->
                    document.getLong("rating")?.toInt()
                }

                val reviewCount = ratings.size

                val averageRating = if (reviewCount == 0) {
                    0.0
                } else {
                    (ratings.average() * 10).roundToInt() / 10.0
                }

                firestore.collection("handymen")
                    .document(handymanId)
                    .update(
                        mapOf(
                            "rating" to averageRating,
                            "reviewCount" to reviewCount,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { exception ->
                        onResult(
                            false,
                            exception.message ?: "Рецензијата е зачувана, но оценката не се ажурираше."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Неуспешно ажурирање на оценката."
                )
            }
    }

    private fun DocumentSnapshot.toReview(handymanId: String): Review {
        return Review(
            id = id,
            handymanId = getString("handymanId").orEmpty().ifBlank { handymanId },
            reviewerId = getString("reviewerId").orEmpty(),
            reviewerName = getString("reviewerName").orEmpty(),
            rating = getLong("rating")?.toInt() ?: 0,
            comment = getString("comment").orEmpty(),
            createdAt = getTimestamp("createdAt"),
            updatedAt = getTimestamp("updatedAt")
        )
    }
}