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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.mock.MockData
import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.data.repository.FavoriteRepository
import com.example.najdimajstor.data.repository.HandymanRepository
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.CategoryCard
import com.example.najdimajstor.ui.components.FilterBottomSheet
import com.example.najdimajstor.ui.components.HandymanCard
import com.example.najdimajstor.ui.components.HomeHeader
import com.example.najdimajstor.ui.components.MainBottomBar
import com.example.najdimajstor.ui.theme.NajdiGold
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

private const val MAX_PRICE_FILTER = 5000

@Composable
fun HomeScreen(
    onHandymanClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val handymanRepository = remember { HandymanRepository() }
    val favoriteRepository = remember { FavoriteRepository() }

    val cachedHandymen = remember {
        handymanRepository.getCachedHandymen()
    }

    val cachedFavoriteIds = remember {
        favoriteRepository.getCachedFavoriteIds()
    }

    var handymen by remember { mutableStateOf<List<Handyman>>(cachedHandymen.orEmpty()) }
    var favoriteIds by remember { mutableStateOf(cachedFavoriteIds ?: emptySet()) }

    var isLoading by remember { mutableStateOf(cachedHandymen == null) }
    var loadErrorMessage by remember { mutableStateOf<String?>(null) }
    var favoriteErrorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var priceFromFilter by remember { mutableStateOf("") }
    var priceToFilter by remember { mutableStateOf("") }
    var ratingFilter by remember { mutableStateOf("all") }
    var availableOnly by remember { mutableStateOf(false) }
    var includeNegotiable by remember { mutableStateOf(true) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val homeListState = rememberLazyListState()

    var savedHomeFirstVisibleItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    var savedHomeFirstVisibleItemScrollOffset by rememberSaveable {
        mutableStateOf(0)
    }

    var hasRestoredHomeScroll by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        handymanRepository.getHandymen { result, error ->
            handymen = result
            loadErrorMessage = error
            isLoading = false
        }

        favoriteRepository.getFavoriteIds { result, error ->
            if (error == null) {
                favoriteIds = result
            } else {
                favoriteErrorMessage = error
            }
        }
    }

    val categories = MockData.serviceCategories
    val cities = handymen.map { it.city }.filter { it.isNotBlank() }.distinct().sorted()

    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }

    val priceFromValue = priceFromFilter.toIntOrNull()
    val priceToValue = priceToFilter.toIntOrNull()

    val hasPriceFilter =
        priceFromValue != null || priceToValue != null

    val hasInvalidPriceFilter =
        priceFromValue != null &&
                priceToValue != null &&
                priceToValue < priceFromValue

    val selectedPriceFrom = priceFromValue ?: Int.MIN_VALUE

    val selectedPriceTo = when {
        priceToValue == null -> Int.MAX_VALUE
        priceToValue >= MAX_PRICE_FILTER -> Int.MAX_VALUE
        else -> priceToValue
    }

    val activeFiltersCount = listOf(
        selectedCategoryId != null,
        selectedCity != null,
        hasPriceFilter,
        ratingFilter != "all",
        availableOnly,
        !includeNegotiable
    ).count { it }

    val hasAdvancedFilters =
        selectedCity != null ||
                hasPriceFilter ||
                ratingFilter != "all" ||
                availableOnly ||
                !includeNegotiable

    val isShowingResults =
        searchQuery.isNotBlank() ||
                selectedCategory != null ||
                hasAdvancedFilters

    val filteredHandymen = remember(
        handymen,
        searchQuery,
        selectedCategoryId,
        selectedCity,
        priceFromFilter,
        priceToFilter,
        ratingFilter,
        availableOnly,
        includeNegotiable
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

            val matchesPrice = when {
                hasInvalidPriceFilter -> {
                    false
                }

                handyman.isPriceNegotiable -> {
                    includeNegotiable
                }

                !hasPriceFilter -> {
                    true
                }

                else -> {
                    val handymanPriceFrom = handyman.priceFrom
                    val handymanPriceTo = handyman.priceTo ?: handyman.priceFrom

                    handymanPriceFrom != null &&
                            handymanPriceTo != null &&
                            handymanPriceFrom <= selectedPriceTo &&
                            handymanPriceTo >= selectedPriceFrom
                }
            }

            val matchesRating = when (ratingFilter) {
                "five" -> handyman.rating == 5.0
                "four_plus" -> handyman.rating >= 4.0
                "three_plus" -> handyman.rating >= 3.0
                "below_three" -> handyman.reviewCount > 0 && handyman.rating < 3.0
                "unrated" -> handyman.reviewCount == 0
                else -> true
            }

            val matchesAvailability = !availableOnly || handyman.isAvailable

            matchesSearch &&
                    matchesCategory &&
                    matchesCity &&
                    matchesPrice &&
                    matchesRating &&
                    matchesAvailability
        }
    }

    LaunchedEffect(isLoading, filteredHandymen.size) {
        if (!isLoading && !hasRestoredHomeScroll) {
            snapshotFlow { homeListState.layoutInfo.totalItemsCount }
                .filter { totalItems -> totalItems > 0 }
                .first()

            val lastIndex = homeListState.layoutInfo.totalItemsCount - 1
            val targetIndex = savedHomeFirstVisibleItemIndex.coerceIn(0, lastIndex)

            homeListState.scrollToItem(
                index = targetIndex,
                scrollOffset = savedHomeFirstVisibleItemScrollOffset
            )

            hasRestoredHomeScroll = true
        }
    }

    LaunchedEffect(homeListState, isLoading, hasRestoredHomeScroll) {
        if (!isLoading && hasRestoredHomeScroll) {
            snapshotFlow {
                homeListState.firstVisibleItemIndex to homeListState.firstVisibleItemScrollOffset
            }.collect { scrollPosition ->
                savedHomeFirstVisibleItemIndex = scrollPosition.first
                savedHomeFirstVisibleItemScrollOffset = scrollPosition.second
            }
        }
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedItem = BottomNavItem.HOME,
                onHomeClick = { },
                onFavoritesClick = onFavoritesClick,
                onMessagesClick = onMessagesClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = homeListState,
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
                    onSearchQueryChange = { searchQuery = it },
                    onFilterClick = { showFilterSheet = true },
                    activeFiltersCount = activeFiltersCount
                )
            }

            if (favoriteErrorMessage != null) {
                item {
                    Text(
                        text = favoriteErrorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (!isShowingResults) {
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
                                                if (selectedCategoryId == category.id) {
                                                    null
                                                } else {
                                                    category.id
                                                }
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            searchQuery.isNotBlank() || hasAdvancedFilters -> "Резултати"
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

            when {
                isLoading -> {
                    item {
                        LoadingHandymenState()
                    }
                }

                loadErrorMessage != null -> {
                    item {
                        ErrorHandymenState(
                            message = loadErrorMessage ?: "Неуспешно вчитување на мајстори."
                        )
                    }
                }

                filteredHandymen.isEmpty() -> {
                    item {
                        EmptySearchResult()
                    }
                }

                else -> {
                    items(
                        items = filteredHandymen,
                        key = { handyman -> handyman.id }
                    ) { handyman ->
                        val isFavorite = favoriteIds.contains(handyman.id)
                        val visibleHandyman = handyman.copy(isFavorite = isFavorite)

                        HandymanCard(
                            handyman = visibleHandyman,
                            onClick = { onHandymanClick(handyman.id) },
                            onFavoriteClick = {
                                val previousFavoriteIds = favoriteIds

                                favoriteIds = if (isFavorite) {
                                    favoriteIds - handyman.id
                                } else {
                                    favoriteIds + handyman.id
                                }

                                favoriteErrorMessage = null

                                favoriteRepository.toggleFavorite(
                                    handymanId = handyman.id,
                                    isCurrentlyFavorite = isFavorite
                                ) { success, error ->
                                    if (!success) {
                                        favoriteIds = previousFavoriteIds
                                        favoriteErrorMessage =
                                            error ?: "Неуспешно зачувување на мајсторот."
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

    if (showFilterSheet) {
        FilterBottomSheet(
            cities = cities,
            selectedCity = selectedCity,
            onCitySelected = { selectedCity = it },
            priceFrom = priceFromFilter,
            onPriceFromChange = { priceFromFilter = it },
            priceTo = priceToFilter,
            onPriceToChange = { priceToFilter = it },
            ratingFilter = ratingFilter,
            onRatingFilterChange = { ratingFilter = it },
            availableOnly = availableOnly,
            onAvailableOnlyChange = { availableOnly = it },
            includeNegotiable = includeNegotiable,
            onIncludeNegotiableChange = { includeNegotiable = it },
            onClearFilters = {
                selectedCategoryId = null
                selectedCity = null
                priceFromFilter = ""
                priceToFilter = ""
                ratingFilter = "all"
                availableOnly = false
                includeNegotiable = true
                searchQuery = ""
            },
            onDismiss = {
                showFilterSheet = false
            }
        )
    }
}

@Composable
private fun LoadingHandymenState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Се вчитуваат мајстори...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorHandymenState(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
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