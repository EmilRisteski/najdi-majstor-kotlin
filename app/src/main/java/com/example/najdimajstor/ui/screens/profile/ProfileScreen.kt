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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.ui.components.BottomNavItem
import com.example.najdimajstor.ui.components.MainBottomBar
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiTextLight
import com.google.firebase.auth.FirebaseAuth
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

    var savedCount by remember { mutableStateOf(0) }

    var fullName by remember { mutableStateOf("Корисник") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CUSTOMER") }

    var handymanRating by remember { mutableStateOf(0.0) }
    var handymanReviewCount by remember { mutableStateOf(0) }

    var isLoading by remember { mutableStateOf(true) }
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showRatingsSheet by remember { mutableStateOf(false) }

    var isSavingProfile by remember { mutableStateOf(false) }
    var editProfileErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        firestore
            .collection("users")
            .document(userId)
            .collection("favorites")
            .get()
            .addOnSuccessListener { snapshot ->
                savedCount = snapshot.size()
            }

        firestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                fullName = document.getString("fullName").orEmpty().ifBlank { "Корисник" }
                email = document.getString("email").orEmpty().ifBlank {
                    currentUser.email.orEmpty()
                }
                phone = document.getString("phone").orEmpty()

                val loadedRole = document.getString("role").orEmpty().ifBlank { "CUSTOMER" }
                role = loadedRole

                if (loadedRole == "HANDYMAN") {
                    firestore
                        .collection("handymen")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { handymanDocument ->
                            handymanRating = handymanDocument.getDouble("rating") ?: 0.0
                            handymanReviewCount =
                                handymanDocument.getLong("reviewCount")?.toInt() ?: 0
                            isLoading = false
                        }
                        .addOnFailureListener {
                            isLoading = false
                        }
                } else {
                    isLoading = false
                }
            }
            .addOnFailureListener {
                email = currentUser.email.orEmpty()
                isLoading = false
            }
    }

    val isHandyman = role == "HANDYMAN"

    val roleLabel = if (isHandyman) {
        "Мајстор"
    } else {
        "Клиент"
    }

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
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                if (currentUserId == null) {
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
                        isSavingProfile = false
                        showEditProfileSheet = false
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
                    fullName = if (isLoading) "Се вчитува..." else fullName,
                    roleLabel = roleLabel
                )
            }

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
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    fullName: String,
    roleLabel: String
) {
    val initials = getInitials(fullName)

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
                text = fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NajdiTextLight
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = roleLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiTextLight.copy(alpha = 0.7f)
            )
        }
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