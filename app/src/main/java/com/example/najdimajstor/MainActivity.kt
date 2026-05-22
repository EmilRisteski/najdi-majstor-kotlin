package com.example.najdimajstor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.najdimajstor.navigation.AppNavigation
import com.example.najdimajstor.ui.theme.NajdiMajstorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NajdiMajstorTheme {
                AppNavigation()
            }
        }
    }
}