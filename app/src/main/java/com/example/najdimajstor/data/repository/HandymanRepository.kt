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
    fun getHandymen(
        onResult: (List<Handyman>, String?) -> Unit
    ) {
        firestore.collection("handymen")
            .get()
            .addOnSuccessListener { snapshot ->
                val handymen = snapshot.documents
                    .map { document -> document.toHandyman() }
                    .filter { handyman -> handyman.isPublished }

                onResult(handymen, null)
            }
            .addOnFailureListener { exception ->
                onResult(
                    emptyList(),
                    exception.message ?: "Неуспешно вчитување на мајстори."
                )
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

        firestore.collection("handymen")
            .document(handymanId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(document.toHandyman(), null)
                } else {
                    onResult(null, "Мајсторот не е пронајден.")
                }
            }
            .addOnFailureListener { exception ->
                onResult(
                    null,
                    exception.message ?: "Неуспешно вчитување на мајсторот."
                )
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

        val documentReference = firestore
            .collection("handymen")
            .document(userId)

        documentReference.get()
            .addOnSuccessListener { existingDocument ->
                val isNewProfile = !existingDocument.exists()

                val requestedCustomProfession =
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
                    requestedCustomProfession &&
                    existingRequestedProfession != handyman.requestedProfession.trim()
                ) {
                    onResult(
                        false,
                        "Веќе имаш барање за професија во обработка. Почекај да биде одобрено или одбиено."
                    )
                    return@addOnSuccessListener
                }

                val profileData = hashMapOf(
                    "name" to handyman.name,
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
                        profileData["isPublished"] = !requestedCustomProfession
                        profileData["professionRequestStatus"] =
                            if (requestedCustomProfession) "pending" else "approved"
                        profileData["requestedProfession"] =
                            if (requestedCustomProfession) {
                                handyman.requestedProfession.trim()
                            } else {
                                ""
                            }
                        profileData["createdAt"] = FieldValue.serverTimestamp()
                    }

                    requestedCustomProfession &&
                            existingRequestStatus != "pending" -> {
                        profileData["isPublished"] = false
                        profileData["professionRequestStatus"] = "pending"
                        profileData["requestedProfession"] =
                            handyman.requestedProfession.trim()
                    }

                    existingRequestStatus == "pending" &&
                            !requestedCustomProfession -> {
                        profileData["isPublished"] = true
                        profileData["professionRequestStatus"] = "approved"
                        profileData["requestedProfession"] = ""
                    }
                }

                documentReference
                    .set(profileData, SetOptions.merge())
                    .addOnSuccessListener {
                        onResult(true, null)
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

                val isPublished = document.getBoolean("isPublished") ?: true
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
                                    "verificationRequestedAt" to FieldValue.serverTimestamp(),
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            )
                            .addOnSuccessListener {
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
            isPublished = getBoolean("isPublished") ?: true,
            professionRequestStatus = getString("professionRequestStatus") ?: "approved",
            requestedProfession = getString("requestedProfession").orEmpty()
        )
    }
}