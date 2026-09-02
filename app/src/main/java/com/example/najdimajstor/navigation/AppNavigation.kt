package com.example.najdimajstor.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.najdimajstor.data.repository.AuthRepository
import com.example.najdimajstor.ui.screens.auth.GoogleRoleSelectionScreen
import com.example.najdimajstor.ui.screens.auth.LoginScreen
import com.example.najdimajstor.ui.screens.auth.RegisterScreen
import com.example.najdimajstor.ui.screens.details.HandymanDetailsScreen
import com.example.najdimajstor.ui.screens.favorites.FavoritesScreen
import com.example.najdimajstor.ui.screens.handymanSetup.HandymanSetupScreen
import com.example.najdimajstor.ui.screens.home.HomeScreen
import com.example.najdimajstor.ui.screens.messages.ChatConversationScreen
import com.example.najdimajstor.ui.screens.messages.MessagesScreen
import com.example.najdimajstor.ui.screens.profile.ProfileScreen
import com.example.najdimajstor.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }

    fun navigateBottomBar(destinationRoute: String) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        if (currentRoute == destinationRoute) {
            return
        }

        navController.navigate(destinationRoute) {
            popUpTo(Screen.Home.route) {
                saveState = true
            }

            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToHomeFromAuth() {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) {
                inclusive = true
            }

            launchSingleTop = true
        }
    }

    fun navigateToGoogleRoleSelection() {
        navController.navigate(Screen.GoogleRoleSelection.route) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            ExitTransition.None
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (!authRepository.isUserLoggedIn()) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }

                        return@SplashScreen
                    }

                    authRepository.checkCurrentUserProfile { hasProfile ->
                        val destination = if (hasProfile) {
                            Screen.Home.route
                        } else {
                            Screen.GoogleRoleSelection.route
                        }

                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navigateToHomeFromAuth()
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
                onGoogleNewUserClick = {
                    navigateToGoogleRoleSelection()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterClick = {
                    navigateToHomeFromAuth()
                },
                onLoginClick = {
                    navController.popBackStack()
                },
                onGoogleNewUserClick = {
                    navigateToGoogleRoleSelection()
                }
            )
        }

        composable(Screen.GoogleRoleSelection.route) {
            GoogleRoleSelectionScreen(
                onContinueClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.GoogleRoleSelection.route) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onHandymanClick = { handymanId ->
                    navController.navigate(Screen.HandymanDetails.createRoute(handymanId)) {
                        launchSingleTop = true
                    }
                },
                onFavoritesClick = {
                    navigateBottomBar(Screen.Favorites.route)
                },
                onMessagesClick = {
                    navigateBottomBar(Screen.Messages.route)
                },
                onProfileClick = {
                    navigateBottomBar(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onHandymanClick = { handymanId ->
                    navController.navigate(Screen.HandymanDetails.createRoute(handymanId)) {
                        launchSingleTop = true
                    }
                },
                onHomeClick = {
                    navigateBottomBar(Screen.Home.route)
                },
                onMessagesClick = {
                    navigateBottomBar(Screen.Messages.route)
                },
                onProfileClick = {
                    navigateBottomBar(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Messages.route) {
            MessagesScreen(
                onHomeClick = {
                    navigateBottomBar(Screen.Home.route)
                },
                onFavoritesClick = {
                    navigateBottomBar(Screen.Favorites.route)
                },
                onChatClick = { otherUserId ->
                    navController.navigate(
                        Screen.ChatConversation.createRoute(otherUserId)
                    ) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navigateBottomBar(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onHomeClick = {
                    navigateBottomBar(Screen.Home.route)
                },
                onFavoritesClick = {
                    navigateBottomBar(Screen.Favorites.route)
                },
                onMessagesClick = {
                    navigateBottomBar(Screen.Messages.route)
                },
                onHandymanSetupClick = {
                    navController.navigate(Screen.HandymanSetup.route) {
                        launchSingleTop = true
                    }
                },
                onLogoutClick = {
                    authRepository.logout()

                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.HandymanSetup.route) {
            HandymanSetupScreen(
                onBackClick = {
                    navController.popBackStack()
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
                },
                onMessageClick = { otherUserId ->
                    navController.navigate(
                        Screen.ChatConversation.createRoute(otherUserId)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ChatConversation.route,
            arguments = listOf(
                navArgument("otherUserId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""

            ChatConversationScreen(
                otherUserId = otherUserId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}