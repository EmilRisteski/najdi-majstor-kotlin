package com.example.najdimajstor.data.repository

import com.example.najdimajstor.data.model.Handyman
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
                    Handyman(
                        id = document.id,
                        ownerId = document.getString("ownerId").orEmpty(),
                        name = document.getString("name").orEmpty(),
                        profession = document.getString("profession").orEmpty(),
                        city = document.getString("city").orEmpty(),
                        price = document.getString("price").orEmpty(),
                        priceFrom = document.getLong("priceFrom")?.toInt(),
                        priceTo = document.getLong("priceTo")?.toInt(),
                        isPriceNegotiable = document.getBoolean("isPriceNegotiable") ?: false,
                        rating = document.getDouble("rating") ?: 0.0,
                        reviewCount = document.getLong("reviewCount")?.toInt() ?: 0,
                        experienceYears = document.getLong("experienceYears")?.toInt() ?: 0,
                        isAvailable = document.getBoolean("isAvailable") ?: true,
                        description = document.getString("description").orEmpty(),
                        specialties = document.get("specialties") as? List<String> ?: emptyList(),
                        isFavorite = false,
                        isVerified = document.getBoolean("isVerified") ?: false,
                        verificationStatus = document.getString("verificationStatus") ?: "none"
                    )
                }

                onResult(handymen, null)
            }
            .addOnFailureListener { exception ->
                onResult(emptyList(), exception.message ?: "Неуспешно вчитување на мајстори.")
            }
    }
}