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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.mock.MockData
import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.data.repository.HandymanRepository
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HandymanSetupScreen(
    onBackClick: () -> Unit
) {
    val handymanRepository = remember { HandymanRepository() }

    var fullName by remember { mutableStateOf("") }
    var selectedProfession by remember { mutableStateOf("") }
    var isCustomProfession by remember { mutableStateOf(false) }
    var customProfession by remember { mutableStateOf("") }
    var professionRequestStatus by remember { mutableStateOf("approved") }
    var professionRejectionReason by remember { mutableStateOf("") }

    var city by remember { mutableStateOf("") }
    var priceFrom by remember { mutableStateOf("") }
    var priceTo by remember { mutableStateOf("") }
    var isPriceNegotiable by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf(true) }
    var experienceYears by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var specialtiesText by remember { mutableStateOf("") }

    var hasSavedProfile by remember { mutableStateOf(false) }
    var isPublished by remember { mutableStateOf(false) }

    var isVerified by remember { mutableStateOf(false) }
    var verificationStatus by remember { mutableStateOf("none") }
    var verificationRejectionReason by remember { mutableStateOf("") }
    var isRequestingVerification by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val professions = MockData.serviceCategories.map { it.title }

    val hasPendingProfessionRequest =
        isCustomProfession &&
                professionRequestStatus == "pending" &&
                customProfession.isNotBlank()

    val hasRejectedProfessionRequest =
        isCustomProfession &&
                professionRequestStatus == "rejected" &&
                customProfession.isNotBlank()

    val canRequestVerification =
        hasSavedProfile &&
                isPublished &&
                !isCustomProfession &&
                selectedProfession.isNotBlank() &&
                professionRequestStatus == "approved" &&
                verificationStatus in listOf("none", "rejected")

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
                val savedName = document.getString("fullName").orEmpty()

                if (fullName.isBlank()) {
                    fullName = savedName.ifBlank {
                        currentUser.email.orEmpty()
                    }
                }
            }

        handymanRepository.getHandymanById(userId) { handyman, _ ->
            if (handyman != null) {
                hasSavedProfile = true
                isPublished = handyman.isPublished

                fullName = handyman.name.ifBlank { fullName }
                city = handyman.city
                priceFrom = handyman.priceFrom?.toString().orEmpty()
                priceTo = handyman.priceTo?.toString().orEmpty()
                isPriceNegotiable = handyman.isPriceNegotiable
                isAvailable = handyman.isAvailable
                experienceYears = handyman.experienceYears
                    .takeIf { it > 0 }
                    ?.toString()
                    .orEmpty()
                description = handyman.description
                specialtiesText = handyman.specialties.joinToString(", ")

                isVerified = handyman.isVerified
                verificationStatus = handyman.verificationStatus
                verificationRejectionReason = handyman.verificationRejectionReason

                professionRequestStatus = handyman.professionRequestStatus
                customProfession = handyman.requestedProfession
                professionRejectionReason = handyman.professionRejectionReason

                isCustomProfession =
                    handyman.professionRequestStatus in listOf("pending", "rejected") &&
                            handyman.requestedProfession.isNotBlank()

                selectedProfession = if (isCustomProfession) {
                    ""
                } else {
                    handyman.profession
                }
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

            IconButton(onClick = onBackClick) {
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

            if (hasPendingProfessionRequest) {
                PendingProfessionRequestCard(
                    profession = customProfession
                )
            }

            if (hasRejectedProfessionRequest) {
                RejectedProfessionRequestCard(
                    profession = customProfession,
                    reason = professionRejectionReason
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
                            selected = !isCustomProfession &&
                                    selectedProfession == profession,
                            onClick = {
                                selectedProfession = profession
                                isCustomProfession = false
                                customProfession = ""
                                professionRequestStatus = "approved"
                            },
                            label = {
                                Text(text = profession)
                            }
                        )
                    }

                    FilterChip(
                        selected = isCustomProfession,
                        onClick = {
                            selectedProfession = ""
                            isCustomProfession = true

                            if (
                                professionRequestStatus !in listOf(
                                    "pending",
                                    "rejected"
                                )
                            ) {
                                professionRequestStatus = "approved"
                            }
                        },
                        label = {
                            Text(text = "Друга професија")
                        }
                    )
                }

                if (isCustomProfession) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customProfession,
                        onValueChange = { customProfession = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Внеси професија") },
                        placeholder = {
                            Text("Пр. Клима сервисер")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (hasRejectedProfessionRequest) {
                            "Можеш да ја измениш професијата и повторно да испратиш барање."
                        } else {
                            "Оваа професија ќе биде испратена на одобрување. Профилот нема да биде јавно прикажан додека не биде одобрена."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = NajdiMutedText
                    )
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

            if (hasPendingProfessionRequest || hasRejectedProfessionRequest) {
                SetupSectionCard(title = "Верификација") {
                    Text(
                        text = if (hasPendingProfessionRequest) {
                            "Верификацијата ќе биде достапна откако барањето за професијата ќе биде одобрено."
                        } else {
                            "Верификацијата ќе биде достапна откако ќе избереш одобрена професија и ќе зачуваш јавен профил."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = NajdiMutedText
                    )
                }
            } else {
                VerificationSection(
                    hasSavedProfile = hasSavedProfile,
                    canRequestVerification = canRequestVerification,
                    isVerified = isVerified,
                    verificationStatus = verificationStatus,
                    verificationRejectionReason = verificationRejectionReason,
                    isRequestingVerification = isRequestingVerification,
                    onRequestVerification = {
                        isRequestingVerification = true
                        message = null
                        isError = false

                        handymanRepository.requestVerification { success, error ->
                            isRequestingVerification = false

                            if (success) {
                                verificationStatus = "pending"
                                verificationRejectionReason = ""
                                isVerified = false
                                message = "Барањето за верификација е успешно испратено."
                            } else {
                                message = error ?: "Неуспешно испраќање на барањето."
                                isError = true
                            }
                        }
                    }
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
                    val trimmedCustomProfession = customProfession.trim()

                    when {
                        fullName.isBlank() -> {
                            message = "Внеси име и презиме."
                            isError = true
                        }

                        !isCustomProfession && selectedProfession.isBlank() -> {
                            message = "Избери професија."
                            isError = true
                        }

                        isCustomProfession && trimmedCustomProfession.length < 2 -> {
                            message = "Внеси ја професијата што сакаш да ја додадеш."
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

                        !isPriceNegotiable && from != null &&
                                to != null &&
                                to < from -> {
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

                            val isPendingCustomProfession = isCustomProfession

                            val handyman = Handyman(
                                name = fullName.trim(),
                                profession = if (isPendingCustomProfession) {
                                    ""
                                } else {
                                    selectedProfession
                                },
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
                                isVerified = if (isPendingCustomProfession) {
                                    false
                                } else {
                                    isVerified
                                },
                                verificationStatus = if (isPendingCustomProfession) {
                                    "none"
                                } else {
                                    verificationStatus
                                },
                                isPublished = !isPendingCustomProfession,
                                professionRequestStatus = if (isPendingCustomProfession) {
                                    "pending"
                                } else {
                                    "approved"
                                },
                                requestedProfession = if (isPendingCustomProfession) {
                                    trimmedCustomProfession
                                } else {
                                    ""
                                }
                            )

                            handymanRepository.saveHandymanProfile(handyman) { success, error ->
                                isSaving = false

                                if (success) {
                                    hasSavedProfile = true
                                    isPublished = handyman.isPublished
                                    professionRequestStatus = handyman.professionRequestStatus
                                    verificationStatus = handyman.verificationStatus
                                    isVerified = handyman.isVerified
                                    professionRejectionReason = ""

                                    message = if (isPendingCustomProfession) {
                                        "Твоето барање за додавање на професијата е во обработка."
                                    } else {
                                        "Мајсторскиот профил е успешно зачуван."
                                    }
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
private fun VerificationSection(
    hasSavedProfile: Boolean,
    canRequestVerification: Boolean,
    isVerified: Boolean,
    verificationStatus: String,
    verificationRejectionReason: String,
    isRequestingVerification: Boolean,
    onRequestVerification: () -> Unit
) {
    SetupSectionCard(title = "Верификација") {
        when {
            isVerified || verificationStatus == "approved" -> {
                Text(
                    text = "Твојот профил е верификуван.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = NajdiGold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Клиентите ќе го гледаат беџот за верификуван мајстор на твојот профил.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiMutedText
                )
            }

            verificationStatus == "pending" -> {
                Text(
                    text = "Барањето за верификација е во обработка.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = NajdiGold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ќе добиеш верификуван беџ кога барањето ќе биде одобрено.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiMutedText
                )
            }

            verificationStatus == "rejected" -> {
                Text(
                    text = "Барањето за верификација е одбиено.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                if (verificationRejectionReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Причина: $verificationRejectionReason",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NajdiMutedText
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (canRequestVerification) {
                    PrimaryButton(
                        text = if (isRequestingVerification) {
                            "Се испраќа..."
                        } else {
                            "Испрати ново барање"
                        },
                        onClick = onRequestVerification
                    )
                }
            }

            !hasSavedProfile || !canRequestVerification -> {
                Text(
                    text = "Прво зачувај јавен мајсторски профил за да можеш да побараш верификација.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiMutedText
                )
            }

            else -> {
                Text(
                    text = "Побарај верификација",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Верификацијата е посебна од објавувањето на профилот. Профилот останува јавно видлив и додека барањето е во обработка.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiMutedText
                )

                Spacer(modifier = Modifier.height(14.dp))

                PrimaryButton(
                    text = if (isRequestingVerification) {
                        "Се испраќа..."
                    } else {
                        "Побарај верификација"
                    },
                    onClick = onRequestVerification
                )
            }
        }
    }
}

@Composable
private fun PendingProfessionRequestCard(
    profession: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = NajdiGold.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Барањето е во обработка",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NajdiGold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Твоето барање за додавање на професијата „$profession“ е во обработка. Профилот нема да биде прикажан јавно додека професијата не биде одобрена.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun RejectedProfessionRequestCard(
    profession: String,
    reason: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Барањето за професија е одбиено",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Барањето за професијата „$profession“ не е одобрено.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            if (reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Причина: $reason",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Можеш да внесеш друга професија или да избереш една од постоечките професии и повторно да зачуваш.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
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