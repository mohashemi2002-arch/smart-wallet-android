package com.example.domain.util

import android.content.Context
import android.content.SharedPreferences
import android.view.Window
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppSecurityManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", true)
        set(value) {
            prefs.edit().putBoolean("biometric_enabled", value).apply()
        }

    var isScreenCaptureProtected: Boolean
        get() = prefs.getBoolean("screen_capture_protected", true)
        set(value) {
            prefs.edit().putBoolean("screen_capture_protected", value).apply()
        }

    private val _isUnlocked = MutableStateFlow(!isBiometricEnabled)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked

    fun applyScreenSecurity(window: Window) {
        if (isScreenCaptureProtected) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun promptBiometric(activity: FragmentActivity) {
        if (!isBiometricEnabled) {
            _isUnlocked.value = true
            return
        }

        val biometricManager = BiometricManager.from(context)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
            _isUnlocked.value = true
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    _isUnlocked.value = true
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ورود امن به کیف پول")
            .setSubtitle("لطفا برای ورود احراز هویت کنید")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
