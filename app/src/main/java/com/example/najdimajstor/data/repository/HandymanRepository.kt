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
                onResult(emptyList(), exception.message ?: "Неуспешно вчитување на мајстори.")
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
                onResult(null, exception.message ?: "Неуспешно вчитување на мајсторот.")
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

        val handymanData = hashMapOf(
            "ownerId" to userId,
            "name" to handyman.name,
            "profession" to handyman.profession,
            "city" to handyman.city,
            "price" to handyman.price,
            "priceFrom" to handyman.priceFrom,
            "priceTo" to handyman.priceTo,
            "isPriceNegotiable" to handyman.isPriceNegotiable,
            "rating" to handyman.rating,
            "reviewCount" to handyman.reviewCount,
            "experienceYears" to handyman.experienceYears,
            "isAvailable" to handyman.isAvailable,
            "description" to handyman.description,
            "specialties" to handyman.specialties,
            "isVerified" to handyman.isVerified,
            "verificationStatus" to handyman.verificationStatus,
            "isPublished" to handyman.isPublished,
            "professionRequestStatus" to handyman.professionRequestStatus,
            "requestedProfession" to handyman.requestedProfession,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("handymen")
            .document(userId)
            .set(handymanData, SetOptions.merge())
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(false, exception.message ?: "Неуспешно зачувување на профилот.")
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