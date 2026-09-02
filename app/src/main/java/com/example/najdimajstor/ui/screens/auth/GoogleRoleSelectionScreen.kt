package com.example.najdimajstor.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.najdimajstor.data.model.UserRole
import com.example.najdimajstor.data.repository.AuthRepository
import com.example.najdimajstor.ui.components.BrandLogo
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.components.RoleSelectionCard
import com.example.najdimajstor.ui.theme.NajdiError

@Composable
fun GoogleRoleSelectionScreen(
    onContinueClick: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BrandLogo(size = 82.dp)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Избери тип на профил",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Пред да продолжиш, избери дали бараш услуга или нудиш мајсторски услуги.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Тип на профил",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleSelectionCard(
                        role = UserRole.CUSTOMER,
                        selected = selectedRole == UserRole.CUSTOMER,
                        onClick = {
                            selectedRole = UserRole.CUSTOMER
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    )

                    RoleSelectionCard(
                        role = UserRole.HANDYMAN,
                        selected = selectedRole == UserRole.HANDYMAN,
                        onClick = {
                            selectedRole = UserRole.HANDYMAN
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = errorMessage ?: "",
                        color = NajdiError,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                PrimaryButton(
                    text = if (isLoading) "Се зачувува..." else "Продолжи",
                    onClick = {
                        if (isLoading) return@PrimaryButton

                        isLoading = true
                        errorMessage = null

                        authRepository.createGoogleUserProfile(
                            role = selectedRole
                        ) { success, error ->
                            isLoading = false

                            if (success) {
                                onContinueClick()
                            } else {
                                errorMessage = error ?: "Профилот не беше зачуван."
                            }
                        }
                    }
                )
            }
        }
    }
}