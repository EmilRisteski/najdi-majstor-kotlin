package com.example.najdimajstor.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.mock.MockData
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.HandymanCard
import com.example.najdimajstor.ui.components.MainBottomBar

@Composable
fun FavoritesScreen(
    onHandymanClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val savedHandymen = MockData.handymen.filter { it.isFavorite }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedItem = BottomNavItem.FAVORITES,
                onHomeClick = onHomeClick,
                onFavoritesClick = { },
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Зачувани",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "${savedHandymen.size} зачуван мајстор",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )

            if (savedHandymen.isEmpty()) {
                EmptyFavoritesContent()
            } else {
                savedHandymen.forEach { handyman ->
                    HandymanCard(
                        handyman = handyman,
                        onClick = {
                            onHandymanClick(handyman.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Сè уште немаш зачувани мајстори.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
    }
}