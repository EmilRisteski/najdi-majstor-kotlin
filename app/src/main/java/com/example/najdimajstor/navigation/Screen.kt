package com.example.najdimajstor.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object Profile : Screen("profile")
    data object HandymanDetails : Screen("handyman_details/{handymanId}") {
        fun createRoute(handymanId: String): String = "handyman_details/$handymanId"
    }
}