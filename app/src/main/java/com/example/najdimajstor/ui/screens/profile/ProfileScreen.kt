package com.example.najdimajstor.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.repository.AccountRepository
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.MainBottomBar
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiSuccess
import com.example.najdimajstor.ui.theme.NajdiTextLight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Locale

@Composable
fun ProfileScreen(
    onHomeClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onHandymanSetupClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val accountRepository = remember { AccountRepository() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUserId = currentUser?.uid.orEmpty()

    val hasCachedDataForCurrentUser =
        ProfileScreenCache.userId == currentUserId && ProfileScreenCache.hasLoaded

    var savedCount by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.savedCount else 0
        )
    }

    var fullName by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.fullName else "Корисник"
        )
    }

    var email by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.email else ""
        )
    }

    var phone by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.phone else ""
        )
    }

    var role by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.role else null
        )
    }

    var handymanRating by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.handymanRating else 0.0
        )
    }

    var handymanReviewCount by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.handymanReviewCount else 0
        )
    }

    var hasHandymanProfile by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.hasHandymanProfile else false
        )
    }

    var isHandymanProfilePublished by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.isHandymanProfilePublished else false
        )
    }

    var handymanProfession by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.handymanProfession else ""
        )
    }

    var handymanProfessionRequestStatus by remember(currentUserId) {
        mutableStateOf(
            if (hasCachedDataForCurrentUser) ProfileScreenCache.handymanProfessionRequestStatus else "approved"
        )
    }

    var isLoading by remember(currentUserId) {
        mutableStateOf(!hasCachedDataForCurrentUser)
    }

    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showRatingsSheet by remember { mutableStateOf(false) }
    var showPublicationDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    var isSavingProfile by remember { mutableStateOf(false) }
    var isAccountActionLoading by remember { mutableStateOf(false) }

    var editProfileErrorMessage by remember { mutableStateOf<String?>(null) }
    var accountActionMessage by remember { mutableStateOf<String?>(null) }
    var accountActionErrorMessage by remember { mutableStateOf<String?>(null) }

    fun saveCurrentStateToCache() {
        if (currentUserId.isBlank()) return

        ProfileScreenCache.userId = currentUserId
        ProfileScreenCache.savedCount = savedCount
        ProfileScreenCache.fullName = fullName
        ProfileScreenCache.email = email
        ProfileScreenCache.phone = phone
        ProfileScreenCache.role = role ?: "CUSTOMER"
        ProfileScreenCache.handymanRating = handymanRating
        ProfileScreenCache.handymanReviewCount = handymanReviewCount
        ProfileScreenCache.hasHandymanProfile = hasHandymanProfile
        ProfileScreenCache.isHandymanProfilePublished = isHandymanProfilePublished
        ProfileScreenCache.handymanProfession = handymanProfession
        ProfileScreenCache.handymanProfessionRequestStatus = handymanProfessionRequestStatus
        ProfileScreenCache.hasLoaded = true
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isBlank()) {
            isLoading = false
            role = "CUSTOMER"
            return@LaunchedEffect
        }

        firestore
            .collection("users")
            .document(currentUserId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                savedCount = snapshot.size()

                if (!isLoading || hasCachedDataForCurrentUser) {
                    saveCurrentStateToCache()
                }
            }

        firestore
            .collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("fullName").orEmpty().ifBlank { "Корисник" }
                email = document.getString("email").orEmpty().ifBlank {
                    currentUser?.email.orEmpty()
                }
                phone = document.getString("phone").orEmpty()

                val loadedRole = document.getString("role").orEmpty().ifBlank { "CUSTOMER" }
                role = loadedRole

                if (loadedRole == "HANDYMAN") {
                    firestore
                        .collection("handymen")
                        .document(currentUserId)
                        .get()
                        .addOnSuccessListener { handymanDocument ->
                            hasHandymanProfile = handymanDocument.exists()
                            isHandymanProfilePublished =
                                handymanDocument.getBoolean("isPublished") ?: false
                            handymanProfession =
                                handymanDocument.getString("profession").orEmpty()
                            handymanProfessionRequestStatus =
                                handymanDocument.getString("professionRequestStatus") ?: "approved"
                            handymanRating = handymanDocument.getDouble("rating") ?: 0.0
                            handymanReviewCount =
                                handymanDocument.getLong("reviewCount")?.toInt() ?: 0

                            isLoading = false
                            saveCurrentStateToCache()
                        }
                        .addOnFailureListener {
                            hasHandymanProfile = false
                            isHandymanProfilePublished = false
                            handymanProfession = ""
                            handymanProfessionRequestStatus = "approved"
                            handymanRating = 0.0
                            handymanReviewCount = 0

                            isLoading = false
                            saveCurrentStateToCache()
                        }
                } else {
                    hasHandymanProfile = false
                    isHandymanProfilePublished = false
                    handymanProfession = ""
                    handymanProfessionRequestStatus = "approved"
                    handymanRating = 0.0
                    handymanReviewCount = 0

                    isLoading = false
                    saveCurrentStateToCache()
                }
            }
            .addOnFailureListener {
                email = currentUser?.email.orEmpty()
                role = "CUSTOMER"
                isLoading = false
                saveCurrentStateToCache()
            }
    }

    val isHandyman = role == "HANDYMAN"

    val roleLabel = when (role) {
        "HANDYMAN" -> "Мајстор"
        "CUSTOMER" -> "Клиент"
        else -> ""
    }

    val canReactivateHandymanProfile =
        handymanProfession.isNotBlank() &&
                handymanProfessionRequestStatus == "approved"

    if (showEditProfileSheet) {
        EditClientProfileSheet(
            fullName = fullName,
            phone = phone,
            isSaving = isSavingProfile,
            errorMessage = editProfileErrorMessage,
            onDismiss = {
                if (!isSavingProfile) {
                    editProfileErrorMessage = null
                    showEditProfileSheet = false
                }
            },
            onSaveClick = { newFullName, newPhone ->
                if (currentUserId.isBlank()) {
                    editProfileErrorMessage = "Корисникот не е најавен."
                    return@EditClientProfileSheet
                }

                val trimmedName = newFullName.trim()
                val trimmedPhone = newPhone.trim()

                if (trimmedName.isBlank()) {
                    editProfileErrorMessage = "Името не смее да биде празно."
                    return@EditClientProfileSheet
                }

                isSavingProfile = true
                editProfileErrorMessage = null

                val updatedData = mapOf(
                    "fullName" to trimmedName,
                    "phone" to trimmedPhone,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                firestore
                    .collection("users")
                    .document(currentUserId)
                    .set(updatedData, SetOptions.merge())
                    .addOnSuccessListener {
                        fullName = trimmedName
                        phone = trimmedPhone
                        saveCurrentStateToCache()

                        updateReviewerNameInExistingReviews(
                            firestore = firestore,
                            userId = currentUserId,
                            reviewerName = trimmedName,
                            onFinished = {
                                updateDisplayNameInExistingChats(
                                    firestore = firestore,
                                    userId = currentUserId,
                                    displayName = trimmedName,
                                    onFinished = {
                                        isSavingProfile = false
                                        showEditProfileSheet = false
                                    },
                                    onError = {
                                        isSavingProfile = false
                                        editProfileErrorMessage =
                                            "Профилот е ажуриран, но старите пораки не се освежија. Обиди се повторно."
                                    }
                                )
                            },
                            onError = {
                                isSavingProfile = false
                                editProfileErrorMessage =
                                    "Профилот е ажуриран, но старите рецензии не се освежија. Обиди се повторно."
                            }
                        )
                    }
                    .addOnFailureListener { exception ->
                        isSavingProfile = false
                        editProfileErrorMessage =
                            exception.message ?: "Профилот не беше ажуриран."
                    }
            }
        )
    }

    if (showRatingsSheet) {
        MyRatingsSheet(
            rating = handymanRating,
            reviewCount = handymanReviewCount,
            onDismiss = {
                showRatingsSheet = false
            }
        )
    }

    if (showPublicationDialog) {
        val targetPublishedState = !isHandymanProfilePublished

        ConfirmProfileActionDialog(
            title = if (targetPublishedState) {
                "Активирај мајсторски профил"
            } else {
                "Деактивирај мајсторски профил"
            },
            text = if (targetPublishedState) {
                "Твојот мајсторски профил повторно ќе се прикажува во пребарување и на почетната страна."
            } else {
                "Твојот мајсторски профил ќе биде скриен од пребарување и од почетната страна. Профилот и податоците ќе останат зачувани."
            },
            confirmText = if (targetPublishedState) {
                "Активирај"
            } else {
                "Деактивирај"
            },
            isDanger = !targetPublishedState,
            onConfirm = {
                showPublicationDialog = false
                isAccountActionLoading = true
                accountActionMessage = null
                accountActionErrorMessage = null

                accountRepository.setHandymanProfilePublished(
                    isPublished = targetPublishedState
                ) { success, error ->
                    isAccountActionLoading = false

                    if (success) {
                        isHandymanProfilePublished = targetPublishedState
                        saveCurrentStateToCache()

                        accountActionMessage = if (targetPublishedState) {
                            "Мајсторскиот профил е активиран."
                        } else {
                            "Мајсторскиот профил е деактивиран."
                        }
                    } else {
                        accountActionErrorMessage =
                            error ?: "Промената не беше зачувана."
                    }
                }
            },
            onDismiss = {
                showPublicationDialog = false
            }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmProfileActionDialog(
            title = "Избриши профил",
            text = "Ова трајно ќе го избрише твојот профил. Зачуваните мајстори ќе бидат избришани, твоите рецензии ќе бидат отстранети, а старите разговори ќе останат со име „Избришан корисник“. Ова не може да се врати.",
            confirmText = "Избриши",
            isDanger = true,
            onConfirm = {
                showDeleteAccountDialog = false
                isAccountActionLoading = true
                accountActionMessage = null
                accountActionErrorMessage = null

                accountRepository.deleteCurrentAccount { success, error ->
                    isAccountActionLoading = false

                    if (success) {
                        ProfileScreenCache.clear()
                        onLogoutClick()
                    } else {
                        accountActionErrorMessage =
                            error ?: "Профилот не беше избришан."
                    }
                }
            },
            onDismiss = {
                showDeleteAccountDialog = false
            }
        )
    }

    Scaffold(
        bottomBar = {
            MainBottomBar(
                selectedItem = BottomNavItem.PROFILE,
                onHomeClick = onHomeClick,
                onFavoritesClick = onFavoritesClick,
                onMessagesClick = onMessagesClick,
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
                    text = "Управувај со твојот профил.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }

            item {
                ProfileHeaderCard(
                    fullName = fullName,
                    roleLabel = roleLabel,
                    isLoading = isLoading
                )
            }

            if (isLoading) {
                item {
                    ProfileLoadingCard()
                }
            } else {
                item {
                    ProfileStatsRow(
                        isHandyman = isHandyman,
                        savedCount = savedCount,
                        rating = handymanRating,
                        reviewCount = handymanReviewCount
                    )
                }

                item {
                    PersonalInfoCard(
                        email = if (email.isBlank()) "Не е достапна" else email,
                        phone = if (phone.isBlank()) "Не е внесен" else phone
                    )
                }

                item {
                    ProfileActionsCard(
                        isHandyman = isHandyman,
                        rating = handymanRating,
                        reviewCount = handymanReviewCount,
                        onEditProfileClick = {
                            if (isHandyman) {
                                onHandymanSetupClick()
                            } else {
                                editProfileErrorMessage = null
                                showEditProfileSheet = true
                            }
                        },
                        onFavoritesClick = onFavoritesClick,
                        onMyRatingsClick = {
                            showRatingsSheet = true
                        },
                        onLogoutClick = onLogoutClick
                    )
                }

                item {
                    DangerZoneCard(
                        isHandyman = isHandyman,
                        hasHandymanProfile = hasHandymanProfile,
                        isHandymanProfilePublished = isHandymanProfilePublished,
                        canReactivateHandymanProfile = canReactivateHandymanProfile,
                        isLoading = isAccountActionLoading,
                        successMessage = accountActionMessage,
                        errorMessage = accountActionErrorMessage,
                        onPublicationClick = {
                            accountActionMessage = null
                            accountActionErrorMessage = null
                            showPublicationDialog = true
                        },
                        onDeleteAccountClick = {
                            accountActionMessage = null
                            accountActionErrorMessage = null
                            showDeleteAccountDialog = true
                        }
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
private fun ProfileHeaderCard(
    fullName: String,
    roleLabel: String,
    isLoading: Boolean
) {
    val initials = if (isLoading) {
        "…"
    } else {
        getInitials(fullName)
    }

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
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NajdiNavy
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLoading) "Се вчитува..." else fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NajdiTextLight
            )

            if (!isLoading && roleLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = roleLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiTextLight.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ProfileLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Text(
            text = "Се вчитуваат податоци...",
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ProfileStatsRow(
    isHandyman: Boolean,
    savedCount: Int,
    rating: Double,
    reviewCount: Int
) {
    if (isHandyman) {
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
                title = "Оценка",
                value = "★ ${formatRating(rating)}",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Рецензии",
                value = reviewCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        StatCard(
            title = "Зачувани мајстори",
            value = savedCount.toString(),
            modifier = Modifier.fillMaxWidth()
        )
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
private fun PersonalInfoCard(
    email: String,
    phone: String
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
                text = "Лични информации",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(18.dp))

            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = "Е-пошта",
                value = email
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInfoRow(
                icon = Icons.Default.Phone,
                label = "Телефон",
                value = phone
            )
        }
    }
}

@Composable
private fun ProfileActionsCard(
    isHandyman: Boolean,
    rating: Double,
    reviewCount: Int,
    onEditProfileClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onMyRatingsClick: () -> Unit,
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
                text = "Опции",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileActionRow(
                icon = Icons.Default.Edit,
                title = "Уреди профил",
                subtitle = if (isHandyman) {
                    "Уреди услуги, цени, локација и достапност"
                } else {
                    "Промени име и телефон"
                },
                onClick = onEditProfileClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProfileActionRow(
                icon = Icons.Default.Favorite,
                title = "Зачувани мајстори",
                subtitle = "Прегледај ги омилените мајстори",
                onClick = onFavoritesClick
            )

            if (isHandyman) {
                Spacer(modifier = Modifier.height(14.dp))

                ProfileActionRow(
                    icon = Icons.Default.Star,
                    title = "Мои оценки",
                    subtitle = "★ ${formatRating(rating)} • ${getReviewCountText(reviewCount)}",
                    onClick = onMyRatingsClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onLogoutClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
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
private fun DangerZoneCard(
    isHandyman: Boolean,
    hasHandymanProfile: Boolean,
    isHandymanProfilePublished: Boolean,
    canReactivateHandymanProfile: Boolean,
    isLoading: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onPublicationClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
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
            if (isHandyman && hasHandymanProfile) {
                DangerActionRow(
                    icon = if (isHandymanProfilePublished) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    title = if (isHandymanProfilePublished) {
                        "Деактивирај мајсторски профил"
                    } else {
                        "Активирај мајсторски профил"
                    },
                    subtitle = if (isHandymanProfilePublished) {
                        "Сокриј го профилот од пребарување"
                    } else if (canReactivateHandymanProfile) {
                        "Повторно прикажи го профилот во пребарување"
                    } else {
                        "Достапно откако професијата ќе биде одобрена"
                    },
                    enabled = !isLoading &&
                            (isHandymanProfilePublished || canReactivateHandymanProfile),
                    isDanger = isHandymanProfilePublished,
                    onClick = onPublicationClick
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            DangerActionRow(
                icon = Icons.Default.Delete,
                title = "Избриши профил",
                subtitle = "Трајно избриши го профилот и личните податоци",
                enabled = !isLoading,
                isDanger = true,
                onClick = onDeleteAccountClick
            )

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Се обработува...",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiMutedText,
                    textAlign = TextAlign.Center
                )
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = successMessage,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiSuccess,
                    textAlign = TextAlign.Center
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = errorMessage,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
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
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
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
private fun DangerActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    isDanger: Boolean,
    onClick: () -> Unit
) {
    val iconTint = if (isDanger) {
        MaterialTheme.colorScheme.error
    } else {
        NajdiGold
    }

    val titleColor = if (!enabled) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    } else if (isDanger) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val subtitleColor = if (!enabled) {
        NajdiMutedText.copy(alpha = 0.55f)
    } else {
        NajdiMutedText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DangerIconBox(
            icon = icon,
            tint = if (enabled) {
                iconTint
            } else {
                iconTint.copy(alpha = 0.45f)
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
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

@Composable
private fun DangerIconBox(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = tint.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
    }
}

@Composable
private fun ConfirmProfileActionDialog(
    title: String,
    text: String,
    confirmText: String,
    isDanger: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = text)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = confirmText,
                    color = if (isDanger) {
                        MaterialTheme.colorScheme.error
                    } else {
                        NajdiGold
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Откажи",
                    color = NajdiMutedText
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditClientProfileSheet(
    fullName: String,
    phone: String,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSaveClick: (String, String) -> Unit
) {
    var editedFullName by remember(fullName) {
        mutableStateOf(fullName)
    }

    var editedPhone by remember(phone) {
        mutableStateOf(phone)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Уреди профил",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = editedFullName,
                onValueChange = {
                    editedFullName = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Име и презиме")
                },
                singleLine = true,
                enabled = !isSaving
            )

            OutlinedTextField(
                value = editedPhone,
                onValueChange = {
                    editedPhone = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Телефон")
                },
                singleLine = true,
                enabled = !isSaving
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isSaving
                ) {
                    Text(
                        text = "Откажи",
                        color = NajdiMutedText
                    )
                }

                TextButton(
                    onClick = {
                        onSaveClick(editedFullName, editedPhone)
                    },
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (isSaving) "Се зачувува..." else "Зачувај",
                        color = NajdiGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyRatingsSheet(
    rating: Double,
    reviewCount: Int,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Мои оценки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "★ ${formatRating(rating)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = NajdiGold
            )

            Text(
                text = getReviewCountText(reviewCount),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )

            Text(
                text = "Овие оценки се пресметуваат од рецензиите што клиентите ги оставаат на твојот јавен профил.",
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiMutedText
            )
        }
    }
}

private object ProfileScreenCache {
    var userId: String? = null
    var savedCount: Int = 0
    var fullName: String = "Корисник"
    var email: String = ""
    var phone: String = ""
    var role: String = "CUSTOMER"
    var handymanRating: Double = 0.0
    var handymanReviewCount: Int = 0
    var hasHandymanProfile: Boolean = false
    var isHandymanProfilePublished: Boolean = false
    var handymanProfession: String = ""
    var handymanProfessionRequestStatus: String = "approved"
    var hasLoaded: Boolean = false

    fun clear() {
        userId = null
        savedCount = 0
        fullName = "Корисник"
        email = ""
        phone = ""
        role = "CUSTOMER"
        handymanRating = 0.0
        handymanReviewCount = 0
        hasHandymanProfile = false
        isHandymanProfilePublished = false
        handymanProfession = ""
        handymanProfessionRequestStatus = "approved"
        hasLoaded = false
    }
}

private fun updateReviewerNameInExistingReviews(
    firestore: FirebaseFirestore,
    userId: String,
    reviewerName: String,
    onFinished: () -> Unit,
    onError: () -> Unit
) {
    firestore
        .collection("handymen")
        .get()
        .addOnSuccessListener { handymenSnapshot ->
            if (handymenSnapshot.isEmpty) {
                onFinished()
                return@addOnSuccessListener
            }

            val reviewReferences = handymenSnapshot.documents.map { handymanDocument ->
                handymanDocument.reference
                    .collection("reviews")
                    .document(userId)
            }

            val existingReviewReferences = mutableListOf<DocumentReference>()

            var completedChecks = 0
            var hasFailed = false

            reviewReferences.forEach { reviewReference ->
                reviewReference
                    .get()
                    .addOnSuccessListener { reviewDocument ->
                        if (hasFailed) {
                            return@addOnSuccessListener
                        }

                        if (reviewDocument.exists()) {
                            existingReviewReferences.add(reviewReference)
                        }

                        completedChecks++

                        if (completedChecks == reviewReferences.size) {
                            if (existingReviewReferences.isEmpty()) {
                                onFinished()
                                return@addOnSuccessListener
                            }

                            val batch = firestore.batch()

                            existingReviewReferences.forEach { existingReviewReference ->
                                batch.update(
                                    existingReviewReference,
                                    mapOf(
                                        "reviewerName" to reviewerName,
                                        "updatedAt" to FieldValue.serverTimestamp()
                                    )
                                )
                            }

                            batch.commit()
                                .addOnSuccessListener {
                                    onFinished()
                                }
                                .addOnFailureListener {
                                    onError()
                                }
                        }
                    }
                    .addOnFailureListener {
                        if (!hasFailed) {
                            hasFailed = true
                            onError()
                        }
                    }
            }
        }
        .addOnFailureListener {
            onError()
        }
}

private fun updateDisplayNameInExistingChats(
    firestore: FirebaseFirestore,
    userId: String,
    displayName: String,
    onFinished: () -> Unit,
    onError: () -> Unit
) {
    firestore
        .collection("chats")
        .whereArrayContains("participantIds", userId)
        .get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                onFinished()
                return@addOnSuccessListener
            }

            val batch = firestore.batch()

            snapshot.documents.forEach { document ->
                batch.update(
                    document.reference,
                    "participantNames.$userId",
                    displayName
                )
            }

            batch.commit()
                .addOnSuccessListener {
                    onFinished()
                }
                .addOnFailureListener {
                    onError()
                }
        }
        .addOnFailureListener {
            onError()
        }
}

private fun getInitials(
    name: String
): String {
    val parts = name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }

    return when {
        parts.size >= 2 -> {
            "${parts[0].first()}${parts[1].first()}".uppercase()
        }

        parts.size == 1 -> {
            parts[0].take(2).uppercase()
        }

        else -> {
            "К"
        }
    }
}

private fun formatRating(
    rating: Double
): String {
    return String.format(Locale.getDefault(), "%.1f", rating)
}

private fun getReviewCountText(
    count: Int
): String {
    return when (count) {
        0 -> "Нема рецензии"
        1 -> "1 рецензија"
        else -> "$count рецензии"
    }
}