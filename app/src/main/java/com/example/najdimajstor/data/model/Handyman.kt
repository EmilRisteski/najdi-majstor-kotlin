package com.example.najdimajstor.data.model

data class Handyman(
    val id: String,
    val name: String,
    val profession: String,
    val city: String,
    val price: String,
    val priceFrom: Int? = null,
    val priceTo: Int? = null,
    val isPriceNegotiable: Boolean = false,
    val rating: Double,
    val reviewCount: Int,
    val experienceYears: Int,
    val isAvailable: Boolean,
    val description: String,
    val specialties: List<String>,
    val isFavorite: Boolean = false
)