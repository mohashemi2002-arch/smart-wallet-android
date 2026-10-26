package com.example.ui.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import com.example.domain.util.AuthManager

enum class GatekeeperMode {
    PIN_LOCK,
    AUTH_SCREEN
}

/**
 * Independent Authentication Gatekeeper Composable.
 * Sits in front of the main application.
 * Only renders [content] when the user has successfully authenticated.
 */
@Composable
fun AuthGatekeeper(
    authManager: AuthManager,
    activity: FragmentActivity,
    content: @Composable () -> Unit
) {
    val isAuthenticated by authManager.isAuthenticated.collectAsState()

    var authMode by remember {
        mutableStateOf(
            if (authManager.hasAccount && authManager.hasPinSet) {
                GatekeeperMode.PIN_LOCK
            } else {
                GatekeeperMode.AUTH_SCREEN
            }
        )
    }

    var showCloudRecoveryDialog by remember { mutableStateOf(false) }

    if (showCloudRecoveryDialog) {
        CloudRecoveryDialog(
            authManager = authManager,
            onDismiss = { showCloudRecoveryDialog = false },
            onRecoverySuccess = {
                showCloudRecoveryDialog = false
            }
        )
    }

    Crossfade(
        targetState = isAuthenticated,
        animationSpec = tween(durationMillis = 350),
        label = "AuthGatekeeperCrossfade"
    ) { authenticated ->
        if (authenticated) {
            // Render the untouched main application screen
            content()
        } else {
            // Render Authentication / Security Gate
            when (authMode) {
                GatekeeperMode.PIN_LOCK -> {
                    PinLockScreen(
                        authManager = authManager,
                        activity = activity,
                        onSwitchToPasswordLogin = { authMode = GatekeeperMode.AUTH_SCREEN },
                        onRecoverCloudClick = { showCloudRecoveryDialog = true }
                    )
                }
                GatekeeperMode.AUTH_SCREEN -> {
                    AuthScreen(
                        authManager = authManager,
                        onSwitchToPinUnlock = if (authManager.hasAccount && authManager.hasPinSet) {
                            { authMode = GatekeeperMode.PIN_LOCK }
                        } else null
                    )
                }
            }
        }
    }
}
