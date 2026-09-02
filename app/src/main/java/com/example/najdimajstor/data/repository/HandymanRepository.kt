package com.example.najdimajstor.data.repository

import com.example.najdimajstor.data.model.Handyman
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class HandymanRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private var cachedHandymen: List<Handyman>? = null

        fun clearCache() {
            cachedHandymen = null
        }
    }

    fun getCachedHandymen(): List<Handyman>? {
        return cachedHandymen
    }

    fun getHandymen(
        onResult: (List<Handyman>, String?) -> Unit
    ) {
        val cachedResult = cachedHandymen

        if (cachedResult != null) {
            onResult(cachedResult, null)
        }

        firestore.collection("handymen")
            .get()
            .addOnSuccessListener { snapshot ->
                val handymen = snapshot.documents
                    .map { it.toHandyman() }
                    .filter { it.isPublished }

                cachedHandymen = handymen
                onResult(handymen, null)
            }
            .addOnFailureListener { exception ->
                if (cachedResult == null) {
                    onResult(
                        emptyList(),
                        exception.message ?: "Неуспешно вчитување на мајстори."
                    )
                } else {
                    onResult(cachedResult, null)
                }
            }
    }

    fun getHandymanById(
        handymanId: String,
        onResult: (Handyman?, String?) -> Unit
    ) {
        if (handymanId.isBlank()) {
            onResult(null, "Мајсторот не е пронајден.")
            return
        }

        val cachedResult = cachedHandymen
            ?.firstOrNull { handyman -> handyman.id == handymanId }

        if (cachedResult != null) {
            onResult(cachedResult, null)
        }

        firestore.collection("handymen")
            .document(handymanId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val handyman = document.toHandyman()
                    updateCachedHandyman(handyman)
                    onResult(handyman, null)
                } else if (cachedResult == null) {
                    onResult(null, "Мајсторот не е пронајден.")
                }
            }
            .addOnFailureListener { exception ->
                if (cachedResult == null) {
                    onResult(
                        null,
                        exception.message ?: "Неуспешно вчитување на мајсторот."
                    )
                } else {
                    onResult(cachedResult, null)
                }
            }
    }

    fun saveHandymanProfile(
        handyman: Handyman,
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        val trimmedName = handyman.name.trim()

        val documentReference = firestore
            .collection("handymen")
            .document(userId)

        documentReference.get()
            .addOnSuccessListener { existingDocument ->
                val isNewProfile = !existingDocument.exists()

                val isCustomProfessionRequest =
                    handyman.professionRequestStatus == "pending" &&
                            handyman.profession.isBlank() &&
                            handyman.requestedProfession.isNotBlank()

                val existingRequestStatus =
                    existingDocument.getString("professionRequestStatus") ?: "approved"

                val existingRequestedProfession =
                    existingDocument.getString("requestedProfession").orEmpty()

                if (
                    !isNewProfile &&
                    existingRequestStatus == "pending" &&
                    isCustomProfessionRequest &&
                    existingRequestedProfession != handyman.requestedProfession.trim()
                ) {
                    onResult(
                        false,
                        "Веќе имаш барање за професија во обработка. Почекај да биде одобрено или одбиено."
                    )
                    return@addOnSuccessListener
                }

                val profileData = hashMapOf<String, Any?>(
                    "name" to trimmedName,
                    "profession" to handyman.profession,
                    "city" to handyman.city,
                    "price" to handyman.price,
                    "priceFrom" to handyman.priceFrom,
                    "priceTo" to handyman.priceTo,
                    "isPriceNegotiable" to handyman.isPriceNegotiable,
                    "experienceYears" to handyman.experienceYears,
                    "isAvailable" to handyman.isAvailable,
                    "description" to handyman.description,
                    "specialties" to handyman.specialties,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                when {
                    isNewProfile -> {
                        profileData["ownerId"] = userId
                        profileData["rating"] = 0.0
                        profileData["reviewCount"] = 0
                        profileData["isVerified"] = false
                        profileData["verificationStatus"] = "none"
                        profileData["verificationRejectionReason"] = ""
                        profileData["isPublished"] = !isCustomProfessionRequest
                        profileData["professionRequestStatus"] =
                            if (isCustomProfessionRequest) "pending" else "approved"
                        profileData["requestedProfession"] =
                            if (isCustomProfessionRequest) {
                                handyman.requestedProfession.trim()
                            } else {
                                ""
                            }
                        profileData["professionRejectionReason"] = ""
                        profileData["createdAt"] = FieldValue.serverTimestamp()
                    }

                    isCustomProfessionRequest &&
                            existingRequestStatus != "pending" -> {
                        profileData["isPublished"] = false
                        profileData["professionRequestStatus"] = "pending"
                        profileData["requestedProfession"] =
                            handyman.requestedProfession.trim()
                        profileData["professionRejectionReason"] = ""
                    }

                    !isCustomProfessionRequest &&
                            existingRequestStatus in listOf("pending", "rejected") -> {
                        profileData["isPublished"] = true
                        profileData["professionRequestStatus"] = "approved"
                        profileData["requestedProfession"] = ""
                        profileData["professionRejectionReason"] = ""
                    }
                }

                documentReference
                    .set(profileData, SetOptions.merge())
                    .addOnSuccessListener {
                        clearCache()

                        syncHandymanNameEverywhere(
                            userId = userId,
                            displayName = trimmedName,
                            onFinished = {
                                onResult(true, null)
                            },
                            onError = {
                                onResult(
                                    false,
                                    "Мајсторскиот профил е зачуван, но името не се ажурираше насекаде. Обиди се повторно."
                                )
                            }
                        )
                    }
                    .addOnFailureListener { exception ->
                        onResult(
                            false,
                            exception.message ?: "Неуспешно зачувување на профилот."
                        )
                    }
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Неуспешно вчитување на профилот."
                )
            }
    }

    fun requestVerification(
        onResult: (Boolean, String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onResult(false, "Корисникот не е најавен.")
            return
        }

        val documentReference = firestore
            .collection("handymen")
            .document(userId)

        documentReference.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onResult(false, "Прво зачувај мајсторски профил.")
                    return@addOnSuccessListener
                }

                val isPublished = document.getBoolean("isPublished") ?: false
                val isVerified = document.getBoolean("isVerified") ?: false
                val verificationStatus =
                    document.getString("verificationStatus") ?: "none"
                val professionRequestStatus =
                    document.getString("professionRequestStatus") ?: "approved"

                when {
                    !isPublished || professionRequestStatus != "approved" -> {
                        onResult(
                            false,
                            "Верификацијата е достапна откако професијата ќе биде одобрена."
                        )
                    }

                    isVerified || verificationStatus == "approved" -> {
                        onResult(false, "Твојот профил е веќе верификуван.")
                    }

                    verificationStatus == "pending" -> {
                        onResult(false, "Веќе имаш барање за верификација во обработка.")
                    }

                    else -> {
                        documentReference
                            .update(
                                mapOf(
                                    "verificationStatus" to "pending",
                                    "verificationRejectionReason" to "",
                                    "verificationRequestedAt" to FieldValue.serverTimestamp(),
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            )
                            .addOnSuccessListener {
                                clearCache()
                                onResult(true, null)
                            }
                            .addOnFailureListener { exception ->
                                onResult(
                                    false,
                                    exception.message
                                        ?: "Неуспешно испраќање на барањето."
                                )
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    exception.message ?: "Неуспешно вчитување на профилот."
                )
            }
    }

    private fun syncHandymanNameEverywhere(
        userId: String,
        displayName: String,
        onFinished: () -> Unit,
        onError: () -> Unit
    ) {
        firestore
            .collection("users")
            .document(userId)
            .set(
                mapOf(
                    "fullName" to displayName,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                updateDisplayNameInExistingChats(
                    userId = userId,
                    displayName = displayName,
                    onFinished = onFinished,
                    onError = onError
                )
            }
            .addOnFailureListener {
                onError()
            }
    }

    private fun updateDisplayNameInExistingChats(
        userId: String,
        displayName: String,
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
                        displayName
                    )
                }

                batch.commit()
                    .addOnSuccessListener {
                        onFinished()
                    }
                    .addOnFailureListener {
                        onError()
                    }
            }
            .addOnFailureListener {
                onError()
            }
    }

    private fun updateCachedHandyman(
        handyman: Handyman
    ) {
        val currentCache = cachedHandymen ?: return

        cachedHandymen = if (handyman.isPublished) {
            val existsInCache = currentCache.any { cachedHandyman ->
                cachedHandyman.id == handyman.id
            }

            if (existsInCache) {
                currentCache.map { cachedHandyman ->
                    if (cachedHandyman.id == handyman.id) handyman else cachedHandyman
                }
            } else {
                currentCache + handyman
            }
        } else {
            currentCache.filterNot { cachedHandyman ->
                cachedHandyman.id == handyman.id
            }
        }
    }

    private fun DocumentSnapshot.toHandyman(): Handyman {
        return Handyman(
            id = id,
            ownerId = getString("ownerId").orEmpty(),
            name = getString("name").orEmpty(),
            profession = getString("profession").orEmpty(),
            city = getString("city").orEmpty(),
            price = getString("price").orEmpty(),
            priceFrom = getLong("priceFrom")?.toInt(),
            priceTo = getLong("priceTo")?.toInt(),
            isPriceNegotiable = getBoolean("isPriceNegotiable") ?: false,
            rating = getDouble("rating") ?: 0.0,
            reviewCount = getLong("reviewCount")?.toInt() ?: 0,
            experienceYears = getLong("experienceYears")?.toInt() ?: 0,
            isAvailable = getBoolean("isAvailable") ?: true,
            description = getString("description").orEmpty(),
            specialties = get("specialties") as? List<String> ?: emptyList(),
            isFavorite = false,
            isVerified = getBoolean("isVerified") ?: false,
            verificationStatus = getString("verificationStatus") ?: "none",
            verificationRejectionReason =
                getString("verificationRejectionReason").orEmpty(),
            isPublished = getBoolean("isPublished") ?: true,
            professionRequestStatus =
                getString("professionRequestStatus") ?: "approved",
            requestedProfession = getString("requestedProfession").orEmpty(),
            professionRejectionReason =
                getString("professionRejectionReason").orEmpty()
        )
    }
}