package com.example.najdimajstor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.najdimajstor.data.repository.AuthRepository
import com.example.najdimajstor.ui.screens.auth.LoginScreen
import com.example.najdimajstor.ui.screens.auth.RegisterScreen
import com.example.najdimajstor.ui.screens.details.HandymanDetailsScreen
import com.example.najdimajstor.ui.screens.favorites.FavoritesScreen
import com.example.najdimajstor.ui.screens.home.HomeScreen
import com.example.najdimajstor.ui.screens.profile.ProfileScreen
import com.example.najdimajstor.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val destination = if (authRepository.isUserLoggedIn()) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }

                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

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
                    navController.navigate(Screen.Favorites.route) {
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
                    authRepository.logout()

                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = true
                        }
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