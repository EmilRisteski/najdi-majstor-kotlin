package com.example.najdimajstor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiNavyLight
import com.example.najdimajstor.ui.theme.NajdiTextLight

@Composable
fun HomeHeader(
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    val searchBackground = if (isDarkTheme) {
        NajdiNavyLight
    } else {
        MaterialTheme.colorScheme.surface
    }

    val searchTextColor = if (isDarkTheme) {
        NajdiTextLight.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = NajdiNavy
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "НајдиМајстор",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = NajdiTextLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Пронајди доверлив мајстор во твоја близина.",
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiTextLight.copy(alpha = 0.72f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = searchBackground,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NajdiGold
                    )

                    Text(
                        text = "Пребарај услуга или мајстор...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = searchTextColor
                    )
                }
            }
        }
    }
}