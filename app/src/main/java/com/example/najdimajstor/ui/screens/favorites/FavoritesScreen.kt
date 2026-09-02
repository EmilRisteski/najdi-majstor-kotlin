package com.example.najdimajstor.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.data.repository.FavoriteRepository
import com.example.najdimajstor.data.repository.HandymanRepository
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.HandymanCard
import com.example.najdimajstor.ui.components.MainBottomBar

@Composable
fun FavoritesScreen(
    onHandymanClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val favoriteRepository = remember { FavoriteRepository() }
    val handymanRepository = remember { HandymanRepository() }

    val cachedHandymen = remember {
        handymanRepository.getCachedHandymen()
    }

    val cachedFavoriteIds = remember {
        favoriteRepository.getCachedFavoriteIds()
    }

    var handymen by remember { mutableStateOf<List<Handyman>>(cachedHandymen.orEmpty()) }
    var favoriteIds by remember { mutableStateOf(cachedFavoriteIds ?: emptySet()) }

    var isLoading by remember {
        mutableStateOf(cachedHandymen == null || cachedFavoriteIds == null)
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        favoriteRepository.getFavoriteIds { ids, favoriteError ->
            if (favoriteError != null && cachedFavoriteIds == null) {
                errorMessage = favoriteError
                isLoading = false
                return@getFavoriteIds
            }

            if (favoriteError == null) {
                favoriteIds = ids
                errorMessage = null
            }

            handymanRepository.getHandymen { result, handymanError ->
                if (handymanError != null && cachedHandymen == null) {
                    errorMessage = handymanError
                } else {
                    handymen = result
                    if (handymanError == null) {
                        errorMessage = null
                    }
                }

                isLoading = false
            }
        }
    }

    val savedHandymen = handymen
        .filter { handyman -> favoriteIds.contains(handyman.id) }
        .map { handyman -> handyman.copy(isFavorite = true) }

    val savedCountText = if (savedHandymen.size == 1) {
        "1 зачуван мајстор"
    } else {
        "${savedHandymen.size} зачувани мајстори"
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedItem = BottomNavItem.FAVORITES,
                onHomeClick = onHomeClick,
                onFavoritesClick = { },
                onMessagesClick = onMessagesClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Зачувани",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = savedCountText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            when {
                isLoading -> {
                    item {
                        FavoritesMessageContent(
                            text = "Се вчитуваат зачувани мајстори..."
                        )
                    }
                }

                errorMessage != null -> {
                    item {
                        FavoritesMessageContent(
                            text = errorMessage ?: "Неуспешно вчитување.",
                            isError = true
                        )
                    }
                }

                savedHandymen.isEmpty() -> {
                    item {
                        EmptyFavoritesContent()
                    }
                }

                else -> {
                    items(
                        items = savedHandymen,
                        key = { handyman -> handyman.id }
                    ) { handyman ->
                        HandymanCard(
                            handyman = handyman,
                            onClick = {
                                onHandymanClick(handyman.id)
                            },
                            onFavoriteClick = {
                                val previousFavoriteIds = favoriteIds

                                favoriteIds = favoriteIds - handyman.id
                                errorMessage = null

                                favoriteRepository.removeFavorite(handyman.id) { success, error ->
                                    if (!success) {
                                        favoriteIds = previousFavoriteIds
                                        errorMessage =
                                            error ?: "Мајсторот не беше отстранет од зачувани."
                                    }
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent() {
    FavoritesMessageContent(
        text = "Сè уште немаш зачувани мајстори."
    )
}

@Composable
private fun FavoritesMessageContent(
    text: String,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            }
        )
    }
}