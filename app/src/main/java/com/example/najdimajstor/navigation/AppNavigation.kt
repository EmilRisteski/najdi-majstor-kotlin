package com.example.najdimajstor.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.najdimajstor.ui.screens.auth.LoginScreen
import com.example.najdimajstor.ui.screens.auth.RegisterScreen
import com.example.najdimajstor.ui.screens.home.HomeScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.najdimajstor.ui.screens.details.HandymanDetailsScreen
import com.example.najdimajstor.ui.screens.favorites.FavoritesScreen
import com.example.najdimajstor.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onHandymanClick = { handymanId ->
                    navController.navigate(Screen.HandymanDetails.createRoute(handymanId))
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onHandymanClick = { handymanId ->
                    navController.navigate(Screen.HandymanDetails.createRoute(handymanId))
                },
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onHomeClick = {
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                    }
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route) {
                        launchSingleTop = true
                    }
                },
                onLogoutClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = Screen.HandymanDetails.route,
            arguments = listOf(
                navArgument("handymanId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val handymanId = backStackEntry.arguments?.getString("handymanId") ?: ""

            HandymanDetailsScreen(
                handymanId = handymanId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun TemporaryScreen(
    title: String,
    subtitle: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String,
    onSecondaryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium
        )

        Button(onClick = onPrimaryClick) {
            Text(text = primaryButtonText)
        }

        Button(onClick = onSecondaryClick) {
            Text(text = secondaryButtonText)
        }
    }
}