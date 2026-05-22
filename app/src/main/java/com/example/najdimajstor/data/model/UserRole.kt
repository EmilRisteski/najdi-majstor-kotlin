package com.example.najdimajstor.data.model

enum class UserRole(
    val title: String,
    val subtitle: String
) {
    CUSTOMER(
        title = "Клиент",
        subtitle = "Барам услуги"
    ),
    HANDYMAN(
        title = "Мајстор",
        subtitle = "Нудам услуги"
    )
}