package com.example.najdimajstor.data.model

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val handymanId: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)