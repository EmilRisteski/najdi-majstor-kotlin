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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import kotlin.math.abs
import kotlin.math.roundToInt

private val priceSteps = (100..2000 step 100).toList() + (2500..5000 step 500).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    cities: List<String>,
    selectedCity: String?,
    onCitySelected: (String?) -> Unit,
    priceFrom: String,
    onPriceFromChange: (String) -> Unit,
    priceTo: String,
    onPriceToChange: (String) -> Unit,
    ratingFilter: String,
    onRatingFilterChange: (String) -> Unit,
    availableOnly: Boolean,
    onAvailableOnlyChange: (Boolean) -> Unit,
    includeNegotiable: Boolean,
    onIncludeNegotiableChange: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val priceFromNumber = priceFrom.toIntOrNull()
    val priceToNumber = priceTo.toIntOrNull()

    val hasInvalidPriceRange =
        priceFromNumber != null &&
                priceToNumber != null &&
                priceToNumber < priceFromNumber

    val fromIndex = priceFromNumber
        ?.let { value -> closestPriceStepIndex(value) }
        ?: 0

    val toIndex = priceToNumber
        ?.let { value -> closestPriceStepIndex(value) }
        ?: priceSteps.lastIndex

    val sliderRange = fromIndex.toFloat()..toIndex.toFloat()

    val ratingOptions = listOf(
        "all" to "Сите",
        "five" to "★ 5",
        "four_plus" to "★ 4+",
        "three_plus" to "★ 3+",
        "below_three" to "Под ★3",
        "unrated" to "Без оцена"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onClearFilters) {
                    Text(
                        text = "Исчисти",
                        color = NajdiGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column {
                Text(
                    text = "Град",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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
                    text = "Цена: ${priceSteps[fromIndex]} - ${formatPriceStep(priceSteps[toIndex])} ден.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                RangeSlider(
                    value = sliderRange,
                    onValueChange = { range ->
                        val newFromIndex = range.start
                            .roundToInt()
                            .coerceIn(0, priceSteps.lastIndex)

                        val newToIndex = range.endInclusive
                            .roundToInt()
                            .coerceIn(newFromIndex, priceSteps.lastIndex)

                        val newFrom = priceSteps[newFromIndex]
                        val newTo = priceSteps[newToIndex]

                        if (newFromIndex == 0 && newToIndex == priceSteps.lastIndex) {
                            onPriceFromChange("")
                            onPriceToChange("")
                        } else {
                            onPriceFromChange(newFrom.toString())
                            onPriceToChange(newTo.toString())
                        }
                    },
                    valueRange = 0f..priceSteps.lastIndex.toFloat(),
                    steps = priceSteps.size - 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "100 ден.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NajdiMutedText
                    )

                    Text(
                        text = "5000+ ден.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NajdiMutedText
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = priceFrom,
                        onValueChange = { value ->
                            onPriceFromChange(value.filter { it.isDigit() })
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Од цена") },
                        placeholder = { Text("Пр. 500") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    OutlinedTextField(
                        value = priceTo,
                        onValueChange = { value ->
                            onPriceToChange(value.filter { it.isDigit() })
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("До цена") },
                        placeholder = { Text("Пр. 3000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }

                if (hasInvalidPriceRange) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Крајната цена не може да биде помала од почетната.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Column {
                Text(
                    text = "Оцена",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ratingOptions) { option ->
                        FilterChip(
                            selected = ratingFilter == option.first,
                            onClick = { onRatingFilterChange(option.first) },
                            label = { Text(option.second) }
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Вклучи „по договор“",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Прикажи мајстори без внесена фиксна цена.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NajdiMutedText
                    )
                }

                Switch(
                    checked = includeNegotiable,
                    onCheckedChange = onIncludeNegotiableChange
                )
            }
        }
    }
}

private fun closestPriceStepIndex(
    value: Int
): Int {
    return priceSteps.indices.minBy { index ->
        abs(priceSteps[index] - value)
    }
}

private fun formatPriceStep(
    value: Int
): String {
    return if (value >= 5000) {
        "5000+"
    } else {
        value.toString()
    }
}