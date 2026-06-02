package com.example.najdimajstor.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.mock.MockData
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.CategoryCard
import com.example.najdimajstor.ui.components.HandymanCard
import com.example.najdimajstor.ui.components.HomeHeader
import com.example.najdimajstor.ui.components.MainBottomBar
import com.example.najdimajstor.ui.theme.NajdiGold

@Composable
fun HomeScreen(
    onHandymanClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var availableOnly by remember { mutableStateOf(false) }

    val categories = MockData.serviceCategories
    val handymen = MockData.handymen
    val cities = handymen.map { it.city }.distinct().sorted()

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    val filteredHandymen = remember(
        searchQuery,
        selectedCategoryId,
        selectedCity,
        availableOnly
    ) {
        val query = searchQuery.trim()

        handymen.filter { handyman ->
            val matchesSearch = query.isBlank() ||
                    handyman.name.contains(query, ignoreCase = true) ||
                    handyman.profession.contains(query, ignoreCase = true) ||
                    handyman.city.contains(query, ignoreCase = true) ||
                    handyman.specialties.any { specialty ->
                        specialty.contains(query, ignoreCase = true)
                    }

            val matchesCategory = selectedCategory == null ||
                    handyman.profession == selectedCategory.title

            val matchesCity = selectedCity == null ||
                    handyman.city == selectedCity

            val matchesAvailability = !availableOnly ||
                    handyman.isAvailable

            matchesSearch && matchesCategory && matchesCity && matchesAvailability
        }
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedItem = BottomNavItem.HOME,
                onHomeClick = { },
                onFavoritesClick = onFavoritesClick,
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

                HomeHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }

            if (searchQuery.isBlank()) {
                item {
                    Text(
                        text = "Каква услуга ти треба?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        categories.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { category ->
                                    CategoryCard(
                                        category = category,
                                        selected = selectedCategoryId == category.id,
                                        onClick = {
                                            selectedCategoryId =
                                                if (selectedCategoryId == category.id) null else category.id
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            if (selectedCategory != null || searchQuery.isNotBlank()) {
                item {
                    FilterSection(
                        cities = cities,
                        selectedCity = selectedCity,
                        onCitySelected = { selectedCity = it },
                        availableOnly = availableOnly,
                        onAvailableOnlyChange = { availableOnly = it },
                        onClearFilters = {
                            selectedCategoryId = null
                            selectedCity = null
                            availableOnly = false
                            searchQuery = ""
                        }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "Резултати"
                            selectedCategory != null -> selectedCategory.title
                            else -> "Истакнати мајстори"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "${filteredHandymen.size} пронајдени",
                        style = MaterialTheme.typography.labelLarge,
                        color = NajdiGold
                    )
                }
            }

            if (filteredHandymen.isEmpty()) {
                item {
                    EmptySearchResult()
                }
            } else {
                items(
                    items = filteredHandymen,
                    key = { handyman -> handyman.id }
                ) { handyman ->
                    HandymanCard(
                        handyman = handyman,
                        onClick = { onHandymanClick(handyman.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun FilterSection(
    cities: List<String>,
    selectedCity: String?,
    onCitySelected: (String?) -> Unit,
    availableOnly: Boolean,
    onAvailableOnlyChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Филтри",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(onClick = onClearFilters) {
                Text(
                    text = "Исчисти",
                    color = NajdiGold
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCity == null,
                    onClick = { onCitySelected(null) },
                    label = {
                        Text(text = "Сите градови")
                    }
                )
            }

            items(cities) { city ->
                FilterChip(
                    selected = selectedCity == city,
                    onClick = { onCitySelected(city) },
                    label = {
                        Text(text = city)
                    }
                )
            }
        }

        FilterChip(
            selected = availableOnly,
            onClick = { onAvailableOnlyChange(!availableOnly) },
            label = {
                Text(text = "Само достапни")
            }
        )
    }
}

@Composable
private fun EmptySearchResult() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Нема пронајдени мајстори за ова пребарување.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}