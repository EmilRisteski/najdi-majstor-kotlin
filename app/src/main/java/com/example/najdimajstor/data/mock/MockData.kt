package com.example.najdimajstor.data.mock

import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.data.model.ServiceCategory

object MockData {

    val serviceCategories = listOf(
        ServiceCategory(
            id = "plumber",
            title = "Водоводџија",
            professionalsCount = 15
        ),
        ServiceCategory(
            id = "electrician",
            title = "Електричар",
            professionalsCount = 23
        ),
        ServiceCategory(
            id = "painter",
            title = "Молер",
            professionalsCount = 18
        ),
        ServiceCategory(
            id = "carpenter",
            title = "Столар",
            professionalsCount = 12
        ),
        ServiceCategory(
            id = "furniture",
            title = "Мебел",
            professionalsCount = 8
        )
    )

    val handymen = listOf(
        Handyman(
            id = "1",
            name = "Игор Петров",
            profession = "Водоводџија",
            city = "Прилеп",
            price = "800-1200 ден.",
            rating = 4.8,
            reviewCount = 127,
            experienceYears = 15,
            isAvailable = true,
            description = "Професионален водоводџија со повеќе од 15 години искуство во поправки, инсталации и итни интервенции.",
            specialties = listOf("Поправка на цевки", "Инсталации", "Итни интервенции"),
            isFavorite = true
        ),
        Handyman(
            id = "2",
            name = "Александар Стојанов",
            profession = "Електричар",
            city = "Битола",
            price = "По договор",
            rating = 4.6,
            reviewCount = 89,
            experienceYears = 10,
            isAvailable = true,
            description = "Електричар за домашни и деловни објекти, со искуство во инсталации, поправки и осветлување.",
            specialties = listOf("Електрични инсталации", "Осветлување", "Поправки")
        ),
        Handyman(
            id = "3",
            name = "Марко Јованоски",
            profession = "Молер",
            city = "Скопје",
            price = "Од 150 ден./м²",
            rating = 4.9,
            reviewCount = 64,
            experienceYears = 8,
            isAvailable = false,
            description = "Молерски услуги за станови, куќи и деловни простории со квалитетна изработка.",
            specialties = listOf("Внатрешно молерисување", "Глетување", "Декоративни техники")
        )
    )
}