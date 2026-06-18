package com.example.najdimajstor.ui.screens.handymanSetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.mock.MockData
import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.data.repository.HandymanRepository
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HandymanSetupScreen(
    onBackClick: () -> Unit
) {
    val handymanRepository = remember { HandymanRepository() }

    var fullName by remember { mutableStateOf("") }
    var selectedProfession by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var priceFrom by remember { mutableStateOf("") }
    var priceTo by remember { mutableStateOf("") }
    var isPriceNegotiable by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf(true) }
    var experienceYears by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var specialtiesText by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val professions = MockData.serviceCategories.map { it.title }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            message = "Корисникот не е најавен."
            isError = true
            return@LaunchedEffect
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("fullName").orEmpty().ifBlank {
                    currentUser.email.orEmpty()
                }
            }

        handymanRepository.getHandymanById(userId) { handyman, _ ->
            if (handyman != null) {
                fullName = handyman.name
                selectedProfession = handyman.profession
                city = handyman.city
                priceFrom = handyman.priceFrom?.toString().orEmpty()
                priceTo = handyman.priceTo?.toString().orEmpty()
                isPriceNegotiable = handyman.isPriceNegotiable
                isAvailable = handyman.isAvailable
                experienceYears = handyman.experienceYears.toString()
                description = handyman.description
                specialtiesText = handyman.specialties.joinToString(", ")
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Column {
                Text(
                    text = "Постави мајсторски профил",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Внеси ги услугите, цените и информациите што клиентите ќе ги гледаат.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            if (message != null) {
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        NajdiGold
                    }
                )
            }

            SetupSectionCard(title = "Основни информации") {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Име и презиме") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Професија",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    professions.forEach { profession ->
                        FilterChip(
                            selected = selectedProfession == profession,
                            onClick = {
                                selectedProfession = profession
                            },
                            label = {
                                Text(text = profession)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Град") },
                    singleLine = true
                )
            }

            SetupSectionCard(title = "Цена и достапност") {
                SwitchRow(
                    title = "Цена по договор",
                    subtitle = "Вклучи ако не сакаш да внесеш фиксна цена.",
                    checked = isPriceNegotiable,
                    onCheckedChange = { isPriceNegotiable = it }
                )

                if (!isPriceNegotiable) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = priceFrom,
                            onValueChange = { priceFrom = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Цена од") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )

                        OutlinedTextField(
                            value = priceTo,
                            onValueChange = { priceTo = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Цена до") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SwitchRow(
                    title = "Достапен",
                    subtitle = "Клиентите ќе видат дека моментално примаш работа.",
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it }
                )
            }

            SetupSectionCard(title = "Искуство и опис") {
                OutlinedTextField(
                    value = experienceYears,
                    onValueChange = { experienceYears = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Години искуство") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    label = { Text("Опис") },
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = specialtiesText,
                    onValueChange = { specialtiesText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Специјалности") },
                    placeholder = {
                        Text("Пр. Поправка на бојлери, монтажа, сервис")
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Оддели ги специјалностите со запирка.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NajdiMutedText
                )
            }

            PrimaryButton(
                text = if (isSaving) "Се зачувува..." else "Зачувај профил",
                onClick = {
                    message = null
                    isError = false

                    val from = priceFrom.toIntOrNull()
                    val to = priceTo.toIntOrNull()
                    val years = experienceYears.toIntOrNull() ?: 0

                    when {
                        fullName.isBlank() -> {
                            message = "Внеси име и презиме."
                            isError = true
                        }

                        selectedProfession.isBlank() -> {
                            message = "Избери професија."
                            isError = true
                        }

                        city.isBlank() -> {
                            message = "Внеси град."
                            isError = true
                        }

                        !isPriceNegotiable && from == null -> {
                            message = "Внеси почетна цена."
                            isError = true
                        }

                        !isPriceNegotiable && to == null -> {
                            message = "Внеси крајна цена."
                            isError = true
                        }

                        !isPriceNegotiable && from != null && to != null && to < from -> {
                            message = "Крајната цена не може да биде помала од почетната."
                            isError = true
                        }

                        else -> {
                            isSaving = true

                            val priceText = if (isPriceNegotiable) {
                                "По договор"
                            } else {
                                "$from-$to ден."
                            }

                            val specialties = specialtiesText
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }

                            val handyman = Handyman(
                                name = fullName.trim(),
                                profession = selectedProfession,
                                city = city.trim(),
                                price = priceText,
                                priceFrom = if (isPriceNegotiable) null else from,
                                priceTo = if (isPriceNegotiable) null else to,
                                isPriceNegotiable = isPriceNegotiable,
                                rating = 0.0,
                                reviewCount = 0,
                                experienceYears = years,
                                isAvailable = isAvailable,
                                description = description.trim(),
                                specialties = specialties,
                                isVerified = false,
                                verificationStatus = "none",
                                isPublished = true,
                                professionRequestStatus = "approved",
                                requestedProfession = ""
                            )

                            handymanRepository.saveHandymanProfile(handyman) { success, error ->
                                isSaving = false

                                if (success) {
                                    message = "Мајсторскиот профил е успешно зачуван."
                                    isError = false
                                } else {
                                    message = error ?: "Неуспешно зачувување на профилот."
                                    isError = true
                                }
                            }
                        }
                    }
                }
            )

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Назад")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SetupSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
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

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}