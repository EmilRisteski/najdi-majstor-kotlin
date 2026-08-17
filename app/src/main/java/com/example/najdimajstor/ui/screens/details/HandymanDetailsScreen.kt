package com.example.najdimajstor.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.data.repository.FavoriteRepository
import com.example.najdimajstor.data.repository.HandymanRepository
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiSuccess
import com.example.najdimajstor.ui.theme.NajdiTextLight
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HandymanDetailsScreen(
    handymanId: String,
    onBackClick: () -> Unit,
    onMessageClick: (String) -> Unit
) {
    val handymanRepository = remember { HandymanRepository() }
    val favoriteRepository = remember { FavoriteRepository() }
    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    }

    var handyman by remember { mutableStateOf<Handyman?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var favoriteErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(handymanId) {
        isLoading = true
        errorMessage = null
        favoriteErrorMessage = null

        handymanRepository.getHandymanById(handymanId) { result, error ->
            handyman = result
            errorMessage = error
            isLoading = false
        }

        favoriteRepository.getFavoriteIds { ids, error ->
            if (error == null) {
                isFavorite = ids.contains(handymanId)
            } else {
                favoriteErrorMessage = error
            }
        }
    }

    val currentHandyman = handyman

    when {
        isLoading -> {
            DetailsMessageState(
                message = "Се вчитува мајсторот...",
                onBackClick = onBackClick
            )
        }

        errorMessage != null -> {
            DetailsMessageState(
                message = errorMessage ?: "Неуспешно вчитување.",
                onBackClick = onBackClick
            )
        }

        currentHandyman == null -> {
            DetailsMessageState(
                message = "Мајсторот не е пронајден.",
                onBackClick = onBackClick
            )
        }

        else -> {
            val isOwnProfile =
                currentUserId.isNotBlank() &&
                        (
                                currentHandyman.id == currentUserId ||
                                        currentHandyman.ownerId == currentUserId
                                )

            HandymanDetailsContent(
                handyman = currentHandyman.copy(isFavorite = isFavorite),
                favoriteErrorMessage = favoriteErrorMessage,
                showMessageButton = !isOwnProfile,
                onFavoriteClick = {
                    val previousFavoriteState = isFavorite

                    isFavorite = !isFavorite
                    favoriteErrorMessage = null

                    favoriteRepository.toggleFavorite(
                        handymanId = handymanId,
                        isCurrentlyFavorite = previousFavoriteState
                    ) { success, error ->
                        if (!success) {
                            isFavorite = previousFavoriteState
                            favoriteErrorMessage =
                                error ?: "Неуспешно зачувување на мајсторот."
                        }
                    }
                },
                onMessageClick = {
                    onMessageClick(currentHandyman.id)
                },
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun DetailsMessageState(
    message: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HandymanDetailsContent(
    handyman: Handyman,
    favoriteErrorMessage: String?,
    showMessageButton: Boolean,
    onFavoriteClick: () -> Unit,
    onMessageClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val professionInitial = handyman.profession
        .firstOrNull()
        ?.toString()
        ?: "?"

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(NajdiNavy)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = NajdiTextLight
                    )
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (handyman.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "Зачувај",
                        tint = if (handyman.isFavorite) NajdiGold else NajdiTextLight
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(
                                color = NajdiTextLight.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = professionInitial,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = NajdiGold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = handyman.profession,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = NajdiGold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = handyman.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NajdiTextLight.copy(alpha = 0.75f)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .background(
                            color = if (handyman.isAvailable) {
                                NajdiSuccess
                            } else {
                                NajdiMutedText
                            },
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (handyman.isAvailable) "Достапен" else "Недостапен",
                        color = NajdiTextLight,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                if (favoriteErrorMessage != null) {
                    Text(
                        text = favoriteErrorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = handyman.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (handyman.isVerified) {
                            Spacer(modifier = Modifier.size(8.dp))

                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Верификуван мајстор",
                                tint = NajdiGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = NajdiMutedText,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.size(4.dp))

                        Text(
                            text = handyman.city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        title = "Рејтинг",
                        value = "★ ${handyman.rating}",
                        subtitle = "${handyman.reviewCount} оценки",
                        modifier = Modifier.weight(1f)
                    )

                    InfoCard(
                        title = "Искуство",
                        value = "${handyman.experienceYears} год.",
                        subtitle = "работа",
                        modifier = Modifier.weight(1f)
                    )
                }

                InfoCard(
                    title = "Цена",
                    value = handyman.price,
                    subtitle = if (handyman.isPriceNegotiable) "Цена по договор" else "Проценета цена",
                    modifier = Modifier.fillMaxWidth()
                )

                SectionCard(
                    title = "За мајсторот"
                ) {
                    Text(
                        text = handyman.description.ifBlank { "Нема внесен опис." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }

                SectionCard(
                    title = "Специјалности"
                ) {
                    if (handyman.specialties.isEmpty()) {
                        Text(
                            text = "Нема внесени специјалности.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            handyman.specialties.forEach { specialty ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(text = specialty)
                                    }
                                )
                            }
                        }
                    }
                }

                SectionCard(
                    title = "Претходни работи"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PortfolioPlaceholder(
                            text = "Работа 1",
                            modifier = Modifier.weight(1f)
                        )

                        PortfolioPlaceholder(
                            text = "Работа 2",
                            modifier = Modifier.weight(1f)
                        )

                        PortfolioPlaceholder(
                            text = "Работа 3",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (showMessageButton) {
                    PrimaryButton(
                        text = "Испрати порака",
                        onClick = onMessageClick
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiMutedText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun PortfolioPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(92.dp)
            .background(
                color = NajdiGold.copy(alpha = 0.12f),
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = NajdiGold,
            textAlign = TextAlign.Center
        )
    }
}