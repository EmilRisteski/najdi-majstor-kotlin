package com.example.najdimajstor.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.ui.theme.NajdiGold

enum class BottomNavItem {
    HOME,
    FAVORITES,
    MESSAGES,
    PROFILE
}

@Composable
fun MainBottomBar(
    selectedItem: BottomNavItem,
    onHomeClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val barColor = if (isDarkTheme) {
        Color(0xFF172033)
    } else {
        Color(0xFFEFF3F8)
    }

    val unselectedColor = if (isDarkTheme) {
        Color(0xFF94A3B8)
    } else {
        Color(0xFF64748B)
    }

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = NajdiGold,
        selectedTextColor = NajdiGold,
        unselectedIconColor = unselectedColor,
        unselectedTextColor = unselectedColor,
        indicatorColor = Color.Transparent
    )

    NavigationBar(
        containerColor = barColor,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == BottomNavItem.HOME,
            onClick = onHomeClick,
            colors = itemColors,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = {
                Text(text = "Почетна")
            }
        )

        NavigationBarItem(
            selected = selectedItem == BottomNavItem.FAVORITES,
            onClick = onFavoritesClick,
            colors = itemColors,
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null
                )
            },
            label = {
                Text(text = "Зачувани")
            }
        )

        NavigationBarItem(
            selected = selectedItem == BottomNavItem.MESSAGES,
            onClick = onMessagesClick,
            colors = itemColors,
            icon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            label = {
                Text(text = "Пораки")
            }
        )

        NavigationBarItem(
            selected = selectedItem == BottomNavItem.PROFILE,
            onClick = onProfileClick,
            colors = itemColors,
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null
                )
            },
            label = {
                Text(text = "Профил")
            }
        )
    }
}