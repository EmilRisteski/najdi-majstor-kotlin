package com.example.najdimajstor.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.model.Handyman
import com.example.najdimajstor.data.model.Review
import com.example.najdimajstor.data.repository.FavoriteRepository
import com.example.najdimajstor.data.repository.HandymanRepository
import com.example.najdimajstor.data.repository.ReviewRepository
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiMutedText
import com.example.najdimajstor.ui.theme.NajdiNavy
import com.example.najdimajstor.ui.theme.NajdiSuccess
import com.example.najdimajstor.ui.theme.NajdiTextLight
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HandymanDetailsScreen(
    handymanId: String,
    onBackClick: () -> Unit,
    onMessageClick: (String) -> Unit
) {
    val handymanRepository = remember { HandymanRepository() }
    val favoriteRepository = remember { FavoriteRepository() }
    val reviewRepository = remember { ReviewRepository() }

    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    }

    var handyman by remember { mutableStateOf<Handyman?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var areReviewsLoaded by remember { mutableStateOf(false) }

    var isFavorite by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var favoriteErrorMessage by remember { mutableStateOf<String?>(null) }
    var reviewsErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(handymanId) {
        isLoading = true
        errorMessage = null
        favoriteErrorMessage = null
        reviewsErrorMessage = null
        areReviewsLoaded = false

        handymanRepository.getHandymanById(handymanId) { result, error ->
            handyman = result
            errorMessage = error
            isLoading = false
        }

        favoriteRepository.getFavoriteIds { ids, error ->
            if (error == null) {
                isFavorite = ids.contains(handymanId)
            } else {
                favoriteErrorMessage = error
            }
        }
    }

    DisposableEffect(handymanId) {
        val registration = reviewRepository.listenToReviews(handymanId) { result, error ->
            reviews = result
            reviewsErrorMessage = error
            areReviewsLoaded = true
        }

        onDispose {
            registration?.remove()
        }
    }

    val currentHandyman = handyman

    when {
        isLoading -> {
            DetailsMessageState(
                message = "Се вчитува мајсторот...",
                onBackClick = onBackClick
            )
        }

        errorMessage != null -> {
            DetailsMessageState(
                message = errorMessage ?: "Неуспешно вчитување.",
                onBackClick = onBackClick
            )
        }

        currentHandyman == null -> {
            DetailsMessageState(
                message = "Мајсторот не е пронајден.",
                onBackClick = onBackClick
            )
        }

        else -> {
            val isOwnProfile =
                currentUserId.isNotBlank() &&
                        (
                                currentHandyman.id == currentUserId ||
                                        currentHandyman.ownerId == currentUserId
                                )

            val reviewCount = if (areReviewsLoaded) {
                reviews.size
            } else {
                currentHandyman.reviewCount
            }

            val averageRating = if (areReviewsLoaded) {
                calculateAverageRating(reviews)
            } else {
                currentHandyman.rating
            }

            HandymanDetailsContent(
                handyman = currentHandyman
                    .copy(
                        isFavorite = isFavorite,
                        rating = averageRating,
                        reviewCount = reviewCount
                    ),
                currentUserId = currentUserId,
                reviews = reviews,
                favoriteErrorMessage = favoriteErrorMessage,
                reviewsErrorMessage = reviewsErrorMessage,
                showMessageButton = !isOwnProfile,
                showReviewButton = !isOwnProfile && currentUserId.isNotBlank(),
                onFavoriteClick = {
                    val previousFavoriteState = isFavorite

                    isFavorite = !isFavorite
                    favoriteErrorMessage = null

                    favoriteRepository.toggleFavorite(
                        handymanId = handymanId,
                        isCurrentlyFavorite = previousFavoriteState
                    ) { success, error ->
                        if (!success) {
                            isFavorite = previousFavoriteState
                            favoriteErrorMessage =
                                error ?: "Неуспешно зачувување на мајсторот."
                        }
                    }
                },
                onSaveReview = { rating, comment, onResult ->
                    reviewRepository.saveReview(
                        handymanId = currentHandyman.id,
                        rating = rating,
                        comment = comment,
                        onResult = onResult
                    )
                },
                onMessageClick = {
                    onMessageClick(currentHandyman.id)
                },
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun DetailsMessageState(
    message: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HandymanDetailsContent(
    handyman: Handyman,
    currentUserId: String,
    reviews: List<Review>,
    favoriteErrorMessage: String?,
    reviewsErrorMessage: String?,
    showMessageButton: Boolean,
    showReviewButton: Boolean,
    onFavoriteClick: () -> Unit,
    onSaveReview: (Int, String, (Boolean, String?) -> Unit) -> Unit,
    onMessageClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val profileInitials = getInitials(handyman.name.ifBlank { handyman.profession })

    var showAllReviewsSheet by remember { mutableStateOf(false) }
    var showReviewForm by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    var reviewFormError by remember { mutableStateOf<String?>(null) }
    var isSavingReview by remember { mutableStateOf(false) }

    val myReview = reviews.firstOrNull { review ->
        review.reviewerId == currentUserId
    }

    if (showAllReviewsSheet) {
        AllReviewsBottomSheet(
            reviews = reviews,
            onDismiss = {
                showAllReviewsSheet = false
            }
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(NajdiNavy)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = NajdiTextLight
                    )
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (handyman.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = "Зачувај",
                        tint = if (handyman.isFavorite) NajdiGold else NajdiTextLight
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(
                                color = NajdiTextLight.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profileInitials,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = NajdiGold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = handyman.profession,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = NajdiGold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = handyman.city,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NajdiTextLight.copy(alpha = 0.75f)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .background(
                            color = if (handyman.isAvailable) {
                                NajdiSuccess
                            } else {
                                NajdiMutedText
                            },
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (handyman.isAvailable) "Достапен" else "Недостапен",
                        color = NajdiTextLight,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                if (favoriteErrorMessage != null) {
                    Text(
                        text = favoriteErrorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = handyman.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (handyman.isVerified) {
                            Spacer(modifier = Modifier.size(8.dp))

                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Верификуван мајстор",
                                tint = NajdiGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = NajdiMutedText,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.size(4.dp))

                        Text(
                            text = handyman.city,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        title = "Рејтинг",
                        value = "★ ${formatRating(handyman.rating)}",
                        subtitle = getReviewCountText(handyman.reviewCount),
                        modifier = Modifier.weight(1f)
                    )

                    InfoCard(
                        title = "Искуство",
                        value = "${handyman.experienceYears} год.",
                        subtitle = "работа",
                        modifier = Modifier.weight(1f)
                    )
                }

                InfoCard(
                    title = "Цена",
                    value = handyman.price,
                    subtitle = if (handyman.isPriceNegotiable) "Цена по договор" else "Проценета цена",
                    modifier = Modifier.fillMaxWidth()
                )

                SectionCard(
                    title = "За мајсторот"
                ) {
                    Text(
                        text = handyman.description.ifBlank { "Нема внесен опис." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }

                SectionCard(
                    title = "Специјалности"
                ) {
                    if (handyman.specialties.isEmpty()) {
                        Text(
                            text = "Нема внесени специјалности.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            handyman.specialties.forEach { specialty ->
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(text = specialty)
                                    }
                                )
                            }
                        }
                    }
                }

                ReviewsSection(
                    reviews = reviews,
                    reviewCount = handyman.reviewCount,
                    averageRating = handyman.rating,
                    reviewsErrorMessage = reviewsErrorMessage,
                    showReviewButton = showReviewButton,
                    showReviewForm = showReviewForm,
                    selectedRating = selectedRating,
                    reviewComment = reviewComment,
                    reviewFormError = reviewFormError,
                    isSavingReview = isSavingReview,
                    hasMyReview = myReview != null,
                    onShowReviewForm = {
                        selectedRating = myReview?.rating ?: 5
                        reviewComment = myReview?.comment.orEmpty()
                        reviewFormError = null
                        showReviewForm = true
                    },
                    onHideReviewForm = {
                        reviewFormError = null
                        showReviewForm = false
                    },
                    onRatingChange = { rating ->
                        selectedRating = rating
                    },
                    onCommentChange = { comment ->
                        reviewComment = comment
                    },
                    onSaveReviewClick = {
                        isSavingReview = true
                        reviewFormError = null

                        onSaveReview(
                            selectedRating,
                            reviewComment
                        ) { success, error ->
                            isSavingReview = false

                            if (success) {
                                showReviewForm = false
                                reviewComment = ""
                                selectedRating = 5
                            } else {
                                reviewFormError = error ?: "Рецензијата не беше зачувана."
                            }
                        }
                    },
                    onAllReviewsClick = {
                        showAllReviewsSheet = true
                    }
                )

                if (showMessageButton) {
                    PrimaryButton(
                        text = "Испрати порака",
                        onClick = onMessageClick
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReviewsSection(
    reviews: List<Review>,
    reviewCount: Int,
    averageRating: Double,
    reviewsErrorMessage: String?,
    showReviewButton: Boolean,
    showReviewForm: Boolean,
    selectedRating: Int,
    reviewComment: String,
    reviewFormError: String?,
    isSavingReview: Boolean,
    hasMyReview: Boolean,
    onShowReviewForm: () -> Unit,
    onHideReviewForm: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSaveReviewClick: () -> Unit,
    onAllReviewsClick: () -> Unit
) {
    SectionCard(
        title = "Оценки и рецензии"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "★ ${formatRating(averageRating)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NajdiGold
                    )

                    Text(
                        text = getReviewCountText(reviewCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = NajdiMutedText
                    )
                }

                if (showReviewButton) {
                    TextButton(
                        onClick = onShowReviewForm
                    ) {
                        Text(
                            text = if (hasMyReview) "Измени рецензија" else "Напиши рецензија",
                            color = NajdiGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (reviewsErrorMessage != null) {
                Text(
                    text = reviewsErrorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (showReviewForm) {
                ReviewForm(
                    selectedRating = selectedRating,
                    comment = reviewComment,
                    errorMessage = reviewFormError,
                    isSaving = isSavingReview,
                    onRatingChange = onRatingChange,
                    onCommentChange = onCommentChange,
                    onSaveClick = onSaveReviewClick,
                    onCancelClick = onHideReviewForm
                )
            }

            if (reviews.isEmpty()) {
                Text(
                    text = "Сè уште нема рецензии.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            } else {
                sortReviews(reviews, ReviewSortOption.NEWEST)
                    .take(2)
                    .forEach { review ->
                        ReviewItem(review = review)
                    }

                if (reviews.size > 2) {
                    TextButton(
                        onClick = onAllReviewsClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Сите рецензии",
                            color = NajdiGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewForm(
    selectedRating: Int,
    comment: String,
    errorMessage: String?,
    isSaving: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Твоја оцена",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            RatingSelector(
                selectedRating = selectedRating,
                onRatingChange = onRatingChange
            )

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Коментар")
                },
                placeholder = {
                    Text(text = "Напиши кратко искуство со мајсторот...")
                },
                minLines = 3,
                maxLines = 5,
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
                    onClick = onCancelClick,
                    enabled = !isSaving
                ) {
                    Text(
                        text = "Откажи",
                        color = NajdiMutedText
                    )
                }

                TextButton(
                    onClick = onSaveClick,
                    enabled = !isSaving
                ) {
                    Text(
                        text = if (isSaving) "Се зачувува..." else "Објави",
                        color = NajdiGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingSelector(
    selectedRating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (1..5).forEach { rating ->
            Text(
                text = if (rating <= selectedRating) "★" else "☆",
                modifier = Modifier
                    .size(34.dp)
                    .clickable {
                        onRatingChange(rating)
                    },
                style = MaterialTheme.typography.headlineSmall,
                color = NajdiGold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReviewItem(
    review: Review
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(review.reviewerName),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = review.reviewerName.ifBlank { "Корисник" },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = formatReviewDate(review.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = NajdiMutedText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = getStars(review.rating),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NajdiGold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = review.comment.ifBlank { "Без коментар." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.74f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllReviewsBottomSheet(
    reviews: List<Review>,
    onDismiss: () -> Unit
) {
    var selectedSortOption by remember {
        mutableStateOf(ReviewSortOption.NEWEST)
    }

    val sortedReviews = sortReviews(
        reviews = reviews,
        sortOption = selectedSortOption
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Сите рецензии",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = selectedSortOption == option,
                        onClick = {
                            selectedSortOption = option
                        },
                        label = {
                            Text(text = option.title)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (sortedReviews.isEmpty()) {
                Text(
                    text = "Сè уште нема рецензии.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = sortedReviews,
                        key = { review -> review.id }
                    ) { review ->
                        ReviewItem(review = review)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = NajdiMutedText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

private enum class ReviewSortOption(
    val title: String
) {
    NEWEST("Најнови"),
    HIGHEST("Највисока оценка"),
    LOWEST("Најниска оценка")
}

private fun sortReviews(
    reviews: List<Review>,
    sortOption: ReviewSortOption
): List<Review> {
    return when (sortOption) {
        ReviewSortOption.NEWEST -> {
            reviews.sortedByDescending { review ->
                review.createdAt?.seconds ?: 0L
            }
        }

        ReviewSortOption.HIGHEST -> {
            reviews.sortedWith(
                compareByDescending<Review> { review -> review.rating }
                    .thenByDescending { review -> review.createdAt?.seconds ?: 0L }
            )
        }

        ReviewSortOption.LOWEST -> {
            reviews.sortedWith(
                compareBy<Review> { review -> review.rating }
                    .thenByDescending { review -> review.createdAt?.seconds ?: 0L }
            )
        }
    }
}

private fun calculateAverageRating(
    reviews: List<Review>
): Double {
    if (reviews.isEmpty()) {
        return 0.0
    }

    return (reviews.map { review -> review.rating }.average() * 10)
        .roundToInt() / 10.0
}

private fun formatRating(
    rating: Double
): String {
    return if (rating == 0.0) {
        "0.0"
    } else {
        String.format(Locale.getDefault(), "%.1f", rating)
    }
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

private fun getStars(
    rating: Int
): String {
    val safeRating = rating.coerceIn(0, 5)
    return "★".repeat(safeRating) + "☆".repeat(5 - safeRating)
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
            "?"
        }
    }
}

private fun formatReviewDate(
    timestamp: com.google.firebase.Timestamp?
): String {
    val date = timestamp?.toDate() ?: return ""

    return SimpleDateFormat(
        "dd.MM.yyyy",
        Locale.getDefault()
    ).format(date)
}