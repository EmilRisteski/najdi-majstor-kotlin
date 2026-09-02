package com.example.najdimajstor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.najdimajstor.R
import com.example.najdimajstor.ui.theme.NajdiNavy

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    showName: Boolean = true
) {
    val isDarkTheme = isSystemInDarkTheme()

    val logoResource = if (isDarkTheme) {
        R.drawable.logo_symbol_dark
    } else {
        R.drawable.logo_symbol_light
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(26.dp)
                )
                .background(
                    color = if (isDarkTheme) {
                        NajdiNavy
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    shape = RoundedCornerShape(26.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = logoResource),
                contentDescription = "НајдиМајстор",
                modifier = Modifier.size(size * 0.72f)
            )
        }

        if (showName) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "НајдиМајстор",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}