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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.model.UserRole
import com.example.najdimajstor.data.repository.AuthRepository
import com.example.najdimajstor.ui.components.AppTextField
import com.example.najdimajstor.ui.components.BrandLogo
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.components.RoleSelectionCard
import com.example.najdimajstor.ui.theme.NajdiError
import com.example.najdimajstor.ui.theme.NajdiGold
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun RegisterScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val authRepository = remember { AuthRepository() }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandLogo(size = 72.dp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Креирај профил",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Избери дали бараш услуга или нудиш мајсторски услуги.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                        onClick = { selectedRole = UserRole.CUSTOMER },
                        modifier = Modifier.weight(1f)
                    )

                    RoleSelectionCard(
                        role = UserRole.HANDYMAN,
                        selected = selectedRole == UserRole.HANDYMAN,
                        onClick = { selectedRole = UserRole.HANDYMAN },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                AppTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = null
                    },
                    label = "Име и презиме",
                    placeholder = "Внеси име и презиме",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = "Е-пошта",
                    placeholder = "Внеси е-пошта",
                    leadingIcon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        errorMessage = null
                    },
                    label = "Телефон (опционално)",
                    placeholder = "Внеси телефонски број",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = "Лозинка",
                    placeholder = "Внеси лозинка",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )

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
                    text = if (isLoading) "Креирање..." else "Креирај профил",
                    onClick = {
                        if (isLoading) return@PrimaryButton

                        when {
                            fullName.isBlank() -> {
                                errorMessage = "Внеси име и презиме."
                            }

                            email.isBlank() -> {
                                errorMessage = "Внеси е-пошта."
                            }

                            password.length < 6 -> {
                                errorMessage = "Лозинката мора да има најмалку 6 карактери."
                            }

                            else -> {
                                isLoading = true
                                errorMessage = null

                                authRepository.registerUser(
                                    fullName = fullName.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    password = password,
                                    role = selectedRole
                                ) { success, error ->
                                    isLoading = false

                                    if (success) {
                                        onRegisterClick()
                                    } else {
                                        errorMessage = error ?: "Регистрацијата не успеа."
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLoginClick,
            enabled = !isLoading
        ) {
            Text(
                text = "Веќе имаш профил? Најави се",
                color = NajdiGold,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}