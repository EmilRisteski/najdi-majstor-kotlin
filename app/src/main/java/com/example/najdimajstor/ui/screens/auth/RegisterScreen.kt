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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.example.najdimajstor.ui.components.AppTextField
import com.example.najdimajstor.ui.components.PrimaryButton
import com.example.najdimajstor.ui.components.RoleSelectionCard
import com.example.najdimajstor.ui.theme.NajdiGold

@Composable
fun RegisterScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CUSTOMER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Креирај профил",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Избери како сакаш да ја користиш апликацијата",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        AppTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = "Име и презиме",
            placeholder = "Внеси име и презиме",
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Е-пошта",
            placeholder = "Внеси е-пошта",
            leadingIcon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Телефон",
            placeholder = "Внеси телефонски број",
            leadingIcon = Icons.Default.Phone
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = "Лозинка",
            placeholder = "Внеси лозинка",
            leadingIcon = Icons.Default.Lock,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Креирај профил",
            onClick = onRegisterClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onLoginClick) {
            Text(
                text = "Веќе имаш профил? Најави се",
                color = NajdiGold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}