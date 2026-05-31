package com.example.najdimajstor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiSuccess

@Composable
fun HandymanCard(
    handyman: Handyman,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = NajdiNavy,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = handyman.profession.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = NajdiGold
                    )
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = handyman.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.size(6.dp))

                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = NajdiGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = handyman.profession,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "★",
                            color = NajdiGold,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = " ${handyman.rating} • ${handyman.reviewCount} оценки",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                }

                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (handyman.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = null,
                        tint = if (handyman.isFavorite) NajdiGold else NajdiMutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        color = NajdiMutedText
                    )
                }

                Text(
                    text = handyman.price,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = NajdiGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = if (handyman.isAvailable) {
                            NajdiSuccess.copy(alpha = 0.14f)
                        } else {
                            NajdiMutedText.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (handyman.isAvailable) "Достапен" else "Недостапен",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (handyman.isAvailable) NajdiSuccess else NajdiMutedText
                )
            }
        }
    }
}