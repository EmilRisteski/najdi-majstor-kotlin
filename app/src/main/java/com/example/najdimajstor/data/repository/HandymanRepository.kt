package com.example.najdimajstor.data.repository

import com.example.najdimajstor.data.model.Handyman
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class HandymanRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getHandymen(
        onResult: (List<Handyman>, String?) -> Unit
    ) {
        firestore.collection("handymen")
            .get()
            .addOnSuccessListener { snapshot ->
                val handymen = snapshot.documents.map { document ->
                    document.toHandyman()
                }

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
            verificationStatus = getString("verificationStatus") ?: "none"
        )
    }
}