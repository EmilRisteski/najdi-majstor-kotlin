package com.example.najdimajstor.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class AccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun setHandymanProfilePublished(
        isPublished: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        val handymanReference = firestore
            .collection("handymen")
            .document(userId)

        handymanReference
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(false, "Мајсторскиот профил не е пронајден.")
                    return@addOnSuccessListener
                }

                val profession = document.getString("profession").orEmpty()
                val professionRequestStatus =
                    document.getString("professionRequestStatus") ?: "approved"

                if (isPublished && (profession.isBlank() || professionRequestStatus != "approved")) {
                    onResult(
                        false,
                        "Профилот може да се активира откако професијата ќе биде одобрена."
                    )
                    return@addOnSuccessListener
                }

                handymanReference
                    .update(
                        mapOf(
                            "isPublished" to isPublished,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener {
                        HandymanRepository.clearCache()
                        onResult(true, null)
                    }
                    .addOnFailureListener { exception ->
                        onResult(
                            false,
                            exception.message ?: "Промената не беше зачувана."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Неуспешно вчитување на мајсторскиот профил."
                )
            }
    }

    fun deleteCurrentAccount(
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (currentUser == null || userId == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        deleteFavorites(
            userId = userId,
            onFinished = {
                deleteReviewsWrittenByUser(
                    userId = userId,
                    onFinished = {
                        anonymizeUserInChats(
                            userId = userId,
                            onFinished = {
                                deleteOwnHandymanProfile(
                                    userId = userId,
                                    onFinished = {
                                        deleteUserDocumentAndAuthAccount(
                                            userId = userId,
                                            onResult = onResult
                                        )
                                    },
                                    onError = {
                                        onResult(false, "Мајсторскиот профил не беше избришан.")
                                    }
                                )
                            },
                            onError = {
                                onResult(false, "Разговорите не беа ажурирани.")
                            }
                        )
                    },
                    onError = {
                        onResult(false, "Рецензиите не беа избришани.")
                    }
                )
            },
            onError = {
                onResult(false, "Зачуваните мајстори не беа избришани.")
            }
        )
    }

    private fun deleteFavorites(
        userId: String,
        onFinished: () -> Unit,
        onError: () -> Unit
    ) {
        firestore
            .collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onFinished()
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()

                snapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener { onFinished() }
                    .addOnFailureListener { onError() }
            }
            .addOnFailureListener { onError() }
    }

    private fun deleteReviewsWrittenByUser(
        userId: String,
        onFinished: () -> Unit,
        onError: () -> Unit
    ) {
        firestore
            .collection("handymen")
            .get()
            .addOnSuccessListener { handymenSnapshot ->
                if (handymenSnapshot.isEmpty) {
                    onFinished()
                    return@addOnSuccessListener
                }

                val reviewReferences = handymenSnapshot.documents.map { handymanDocument ->
                    handymanDocument.reference
                        .collection("reviews")
                        .document(userId)
                }

                val existingReviewReferences = mutableListOf<DocumentReference>()
                val affectedHandymanIds = mutableSetOf<String>()

                var completedChecks = 0
                var hasFailed = false

                reviewReferences.forEach { reviewReference ->
                    reviewReference
                        .get()
                        .addOnSuccessListener { reviewDocument ->
                            if (hasFailed) {
                                return@addOnSuccessListener
                            }

                            if (reviewDocument.exists()) {
                                existingReviewReferences.add(reviewReference)

                                val affectedHandymanId =
                                    reviewReference.parent.parent?.id.orEmpty()

                                if (affectedHandymanId.isNotBlank()) {
                                    affectedHandymanIds.add(affectedHandymanId)
                                }
                            }

                            completedChecks++

                            if (completedChecks == reviewReferences.size) {
                                if (existingReviewReferences.isEmpty()) {
                                    onFinished()
                                    return@addOnSuccessListener
                                }

                                val batch = firestore.batch()

                                existingReviewReferences.forEach { existingReviewReference ->
                                    batch.delete(existingReviewReference)
                                }

                                batch.commit()
                                    .addOnSuccessListener {
                                        recalculateRatingsForHandymen(
                                            handymanIds = affectedHandymanIds.toList(),
                                            onFinished = onFinished,
                                            onError = onError
                                        )
                                    }
                                    .addOnFailureListener { onError() }
                            }
                        }
                        .addOnFailureListener {
                            if (!hasFailed) {
                                hasFailed = true
                                onError()
                            }
                        }
                }
            }
            .addOnFailureListener { onError() }
    }

    private fun recalculateRatingsForHandymen(
        handymanIds: List<String>,
        onFinished: () -> Unit,
        onError: () -> Unit
    ) {
        if (handymanIds.isEmpty()) {
            onFinished()
            return
        }

        var completedUpdates = 0
        var hasFailed = false

        handymanIds.forEach { handymanId ->
            firestore
                .collection("handymen")
                .document(handymanId)
                .collection("reviews")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (hasFailed) {
                        return@addOnSuccessListener
                    }

                    val ratings = snapshot.documents.mapNotNull { document ->
                        document.getLong("rating")?.toInt()
                    }

                    val reviewCount = ratings.size

                    val averageRating = if (reviewCount == 0) {
                        0.0
                    } else {
                        (ratings.average() * 10).roundToInt() / 10.0
                    }

                    firestore
                        .collection("handymen")
                        .document(handymanId)
                        .update(
                            mapOf(
                                "rating" to averageRating,
                                "reviewCount" to reviewCount,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                        .addOnSuccessListener {
                            completedUpdates++

                            if (completedUpdates == handymanIds.size) {
                                HandymanRepository.clearCache()
                                onFinished()
                            }
                        }
                        .addOnFailureListener {
                            if (!hasFailed) {
                                hasFailed = true
                                onError()
                            }
                        }
                }
                .addOnFailureListener {
                    if (!hasFailed) {
                        hasFailed = true
                        onError()
                    }
                }
        }
    }

    private fun anonymizeUserInChats(
        userId: String,
        onFinished: () -> Unit,
        onError: () -> Unit
    ) {
        firestore
            .collection("chats")
            .whereArrayContains("participantIds", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onFinished()
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()

                snapshot.documents.forEach { document ->
                    batch.update(
                        document.reference,
                        "participantNames.$userId",
                        "Избришан корисник"
                    )
                }

                batch.commit()
                    .addOnSuccessListener { onFinished() }
                    .addOnFailureListener { onError() }
            }
            .addOnFailureListener { onError() }
    }

    private fun deleteOwnHandymanProfile(
        userId: String,
        onFinished: () -> Unit,
        onError: () -> Unit
    ) {
        val handymanReference = firestore
            .collection("handymen")
            .document(userId)

        handymanReference
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onFinished()
                    return@addOnSuccessListener
                }

                handymanReference
                    .delete()
                    .addOnSuccessListener {
                        HandymanRepository.clearCache()
                        onFinished()
                    }
                    .addOnFailureListener { onError() }
            }
            .addOnFailureListener { onError() }
    }

    private fun deleteUserDocumentAndAuthAccount(
        userId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        firestore
            .collection("users")
            .document(userId)
            .delete()
            .addOnSuccessListener {
                currentUser
                    .delete()
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { exception ->
                        val message = when (exception) {
                            is FirebaseAuthRecentLoginRequiredException ->
                                "За бришење на профилот треба повторно да се најавиш и да пробаш пак."

                            is FirebaseNetworkException ->
                                "Нема интернет-конекција. Провери ја мрежата и обиди се повторно."

                            is FirebaseTooManyRequestsException ->
                                "Премногу обиди. Почекај малку и обиди се повторно."

                            else ->
                                exception.message ?: "Профилот не беше целосно избришан."
                        }

                        onResult(false, message)
                    }
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Корисничкиот профил не беше избришан."
                )
            }
    }
}