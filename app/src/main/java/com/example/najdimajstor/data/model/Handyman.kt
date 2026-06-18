package com.example.najdimajstor.data.model

data class Handyman(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val profession: String = "",
    val city: String = "",
    val price: String = "",
    val priceFrom: Int? = null,
    val priceTo: Int? = null,
    val isPriceNegotiable: Boolean = false,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val experienceYears: Int = 0,
    val isAvailable: Boolean = true,
    val description: String = "",
    val specialties: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isVerified: Boolean = false,
    val verificationStatus: String = "none"
)