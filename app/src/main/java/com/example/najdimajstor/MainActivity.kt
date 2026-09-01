package com.example.najdimajstor

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.najdimajstor.navigation.AppNavigation
import com.example.najdimajstor.notifications.LocalChatNotificationRepository
import com.example.najdimajstor.notifications.NotificationTokenRepository
import com.example.najdimajstor.ui.theme.NajdiMajstorTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser != null) {
            NotificationTokenRepository.refreshTokenForCurrentUser()
            LocalChatNotificationRepository.startListening(applicationContext)
        } else {
            LocalChatNotificationRepository.stopListening()
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyWindowBackground()
        requestNotificationPermissionIfNeeded()
        auth.addAuthStateListener(authStateListener)

        setContent {
            NajdiMajstorTheme {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        auth.removeAuthStateListener(authStateListener)
        super.onDestroy()
    }

    private fun applyWindowBackground() {
        val isDarkMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        val backgroundColor = if (isDarkMode) {
            Color.parseColor("#0B1220")
        } else {
            Color.parseColor("#F8FAFC")
        }

        window.setBackgroundDrawable(ColorDrawable(backgroundColor))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }
}