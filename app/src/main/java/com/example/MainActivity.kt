package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppTheme
import com.example.ui.auth.AuthGatekeeper
import com.example.ui.theme.SmartWalletTheme
import com.example.ui.viewmodel.SettingsViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val preferences by settingsViewModel.userPreferences.collectAsState()

            val darkTheme = when (preferences.appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            val layoutDirection = if (preferences.appLanguage == com.example.data.AppLanguage.EN) {
                LayoutDirection.Ltr
            } else {
                LayoutDirection.Rtl
            }

            SmartWalletTheme(darkTheme = darkTheme) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection
                ) {
                    AuthGatekeeper(
                        authManager = com.example.domain.util.AuthManager(this),
                        activity = this
                    ) {
                        SmartWalletApp()
                    }
                }
            }
        }
    }
}
