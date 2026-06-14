package com.example.najdimajstor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.ui.theme.NajdiGold
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    cities: List<String>,
    selectedCity: String?,
    onCitySelected: (String?) -> Unit,
    priceRange: ClosedFloatingPointRange<Float>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    minimumRating: Float,
    onMinimumRatingChange: (Float) -> Unit,
    availableOnly: Boolean,
    onAvailableOnlyChange: (Boolean) -> Unit,
    includeNegotiable: Boolean,
    onIncludeNegotiableChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Филтри",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Исчисти",
                        color = NajdiGold
                    )
                }
            }

            Column {
                Text(
                    text = "Град",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCity == null,
                            onClick = { onCitySelected(null) },
                            label = { Text("Сите градови") }
                        )
                    }

                    items(cities) { city ->
                        FilterChip(
                            selected = selectedCity == city,
                            onClick = { onCitySelected(city) },
                            label = { Text(city) }
                        )
                    }
                }
            }

            Column {
                Text(
                    text = "Цена: ${priceRange.start.roundToInt()} - ${
                        if (priceRange.endInclusive >= 5000f) "5000+" else priceRange.endInclusive.roundToInt()
                    } ден.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                RangeSlider(
                    value = priceRange,
                    onValueChange = onPriceRangeChange,
                    valueRange = 150f..5000f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("150 ден.")
                    Text("5000+ ден.")
                }
            }

            Column {
                Text(
                    text = "Рејтинг",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val ratings = listOf(
                        0f to "Сите",
                        4.0f to "4.0+",
                        4.5f to "4.5+",
                        4.8f to "4.8+"
                    )

                    items(ratings) { rating ->
                        FilterChip(
                            selected = minimumRating == rating.first,
                            onClick = { onMinimumRatingChange(rating.first) },
                            label = { Text(rating.second) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Само достапни мајстори",
                    style = MaterialTheme.typography.bodyLarge
                )

                Switch(
                    checked = availableOnly,
                    onCheckedChange = onAvailableOnlyChange
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Вклучи „по договор“",
                    style = MaterialTheme.typography.bodyLarge
                )

                Switch(
                    checked = includeNegotiable,
                    onCheckedChange = onIncludeNegotiableChange
                )
            }
        }
    }
}