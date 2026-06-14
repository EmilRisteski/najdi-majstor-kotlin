package com.example.najdimajstor.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.mock.MockData
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.MainBottomBar
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiTextLight

@Composable
fun ProfileScreen(
    onHomeClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val savedCount = MockData.handymen.count { it.isFavorite }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedItem = BottomNavItem.PROFILE,
                onHomeClick = onHomeClick,
                onFavoritesClick = onFavoritesClick,
                onProfileClick = { }
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
                    text = "Мој профил",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Управувај со твојот профил и зачувани мајстори.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            item {
                ProfileHeaderCard()
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Зачувани",
                        value = savedCount.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Оценки",
                        value = "0",
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Барања",
                        value = "0",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                PersonalInfoCard()
            }

            item {
                ProfileActionsCard(
                    onLogoutClick = onLogoutClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = NajdiNavy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(
                        color = NajdiGold,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "К",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NajdiNavy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Корисник",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NajdiTextLight
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Клиентски профил",
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiTextLight.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NajdiGold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiMutedText
            )
        }
    }
}

@Composable
private fun PersonalInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Лични информации",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(18.dp))

            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = "Е-пошта",
                value = "user@gmail.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoRow(
                icon = Icons.Default.Phone,
                label = "Телефон",
                value = "+389 70 000 000"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Локација",
                value = "Прилеп"
            )
        }
    }
}

@Composable
private fun ProfileActionsCard(
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Поставки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileActionRow(
                icon = Icons.Default.Edit,
                title = "Уреди профил",
                subtitle = "Промени име, телефон и локација"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileActionRow(
                icon = Icons.Default.Favorite,
                title = "Зачувани мајстори",
                subtitle = "Прегледај ги омилените мајстори"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileActionRow(
                icon = Icons.Default.Star,
                title = "Мои оценки",
                subtitle = "Оценките ќе бидат достапни подоцна"
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileActionRow(
                icon = Icons.Default.Settings,
                title = "Поставки на апликацијата",
                subtitle = "Тема, јазик и приватност"
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onLogoutClick
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Одјави се",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon = icon)

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiMutedText
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBox(icon = icon)

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiMutedText
            )
        }
    }
}

@Composable
private fun IconBox(
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = NajdiGold.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NajdiGold
        )
    }
}