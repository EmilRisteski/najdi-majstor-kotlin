package com.example.najdimajstor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiNavyLight
import com.example.najdimajstor.ui.theme.NajdiTextLight

@Composable
fun HomeHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    activeFiltersCount: Int,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    val searchBackground = if (isDarkTheme) {
        NajdiNavyLight
    } else {
        MaterialTheme.colorScheme.surface
    }

    val searchTextColor = if (isDarkTheme) {
        NajdiTextLight
    } else {
        MaterialTheme.colorScheme.onSurface
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Пребарај...",
                            color = searchTextColor.copy(alpha = 0.55f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = NajdiGold
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = searchTextColor,
                        unfocusedTextColor = searchTextColor,
                        focusedContainerColor = searchBackground,
                        unfocusedContainerColor = searchBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = NajdiGold
                    )
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = NajdiGold,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onFilterClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Филтри",
                        tint = NajdiNavy
                    )

                    if (activeFiltersCount > 0) {
                        Badge(
                            modifier = Modifier.align(Alignment.TopEnd),
                            containerColor = NajdiTextLight,
                            contentColor = NajdiNavy
                        ) {
                            Text(text = activeFiltersCount.toString())
                        }
                    }
                }
            }
        }
    }
}