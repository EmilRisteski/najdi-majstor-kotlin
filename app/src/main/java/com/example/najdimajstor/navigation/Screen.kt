package com.example.najdimajstor.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object Messages : Screen("messages")
    data object Profile : Screen("profile")
    data object HandymanSetup : Screen("handyman_setup")

    data object HandymanDetails : Screen("handyman_details/{handymanId}") {
        fun createRoute(handymanId: String): String = "handyman_details/$handymanId"
    }

    data object ChatConversation : Screen("chat_conversation/{otherUserId}") {
        fun createRoute(otherUserId: String): String = "chat_conversation/$otherUserId"
    }
}