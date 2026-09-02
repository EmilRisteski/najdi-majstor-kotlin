package com.example.najdimajstor.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.data.repository.AuthRepository
import com.example.najdimajstor.data.repository.GoogleSignInClient
import com.example.najdimajstor.ui.components.AppTextField
import com.example.najdimajstor.ui.components.BrandLogo
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.theme.NajdiError
import com.example.najdimajstor.ui.theme.NajdiGold
import com.example.najdimajstor.ui.theme.NajdiSuccess
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleNewUserClick: () -> Unit
) {
    val authRepository = remember { AuthRepository() }
    val context = LocalContext.current
    val googleSignInClient = remember { GoogleSignInClient(context) }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun startGoogleSignIn() {
        if (isLoading) return

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            val tokenResult = googleSignInClient.getGoogleIdToken()
            val idToken = tokenResult.idToken

            if (idToken == null) {
                isLoading = false
                errorMessage = tokenResult.errorMessage ?: "Google најавата не успеа."
                return@launch
            }

            authRepository.signInWithGoogleIdToken(idToken) { success, hasUserProfile, error ->
                isLoading = false

                when {
                    success && hasUserProfile -> {
                        onLoginClick()
                    }

                    success && !hasUserProfile -> {
                        onGoogleNewUserClick()
                    }

                    else -> {
                        errorMessage = error ?: "Google најавата не успеа."
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandLogo(size = 88.dp)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Најави се",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Пронајди доверлив мајстор во твоја близина.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                    AppTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                            successMessage = null
                        },
                        label = "Е-пошта",
                        placeholder = "Внеси е-пошта",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AppTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                            successMessage = null
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
                            modifier = Modifier.fillMaxWidth(),
                            color = NajdiError,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = successMessage ?: "",
                            modifier = Modifier.fillMaxWidth(),
                            color = NajdiSuccess,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    PrimaryButton(
                        text = if (isLoading) "Најавување..." else "Најави се",
                        onClick = {
                            if (isLoading) return@PrimaryButton

                            when {
                                email.isBlank() -> {
                                    errorMessage = "Внеси е-пошта."
                                    successMessage = null
                                }

                                password.isBlank() -> {
                                    errorMessage = "Внеси лозинка."
                                    successMessage = null
                                }

                                else -> {
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null

                                    authRepository.loginUser(
                                        email = email.trim(),
                                        password = password
                                    ) { success, error ->
                                        isLoading = false

                                        if (success) {
                                            onLoginClick()
                                        } else {
                                            errorMessage = error ?: "Најавата не успеа."
                                        }
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            startGoogleSignIn()
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                        )
                    ) {
                        Text(
                            text = "Продолжи со Google",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            if (isLoading) return@TextButton

                            if (email.isBlank()) {
                                errorMessage = "Внеси ја е-поштата за да ја ресетираш лозинката."
                                successMessage = null
                                return@TextButton
                            }

                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            authRepository.sendPasswordResetEmail(
                                email = email.trim()
                            ) { success, error ->
                                isLoading = false

                                if (success) {
                                    successMessage =
                                        "Ако постои профил со оваа е-пошта, ќе добиеш линк за ресетирање на лозинката."
                                } else {
                                    errorMessage =
                                        error ?: "Неуспешно испраќање на е-пошта за ресетирање."
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Ја заборави лозинката?",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            TextButton(
                onClick = onRegisterClick,
                enabled = !isLoading
            ) {
                Text(
                    text = "Немаш профил? Креирај профил",
                    color = NajdiGold,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}