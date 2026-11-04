package com.example.domain.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.UserPreferencesRepository
import com.example.data.sync.GoogleSheetsSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val syncManager = GoogleSheetsSyncManager(context)
    private val userPrefsRepo = UserPreferencesRepository(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    companion object {
        private const val TAG = "AuthManager"
        private const val PREFS_NAME = "smart_wallet_auth_prefs"
        private const val KEY_HAS_ACCOUNT = "has_account"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

        /**
         * Backward-compatible delegation to PasswordHasher.
         */
        fun hashSha256(input: String): String {
            return PasswordHasher.hash(input)
        }
    }

    val hasAccount: Boolean
        get() = prefs.getBoolean(KEY_HAS_ACCOUNT, false)

    val savedUsername: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""

    val savedFirstName: String
        get() = prefs.getString(KEY_FIRST_NAME, "") ?: ""

    val savedLastName: String
        get() = prefs.getString(KEY_LAST_NAME, "") ?: ""

    val savedPhoneNumber: String
        get() = prefs.getString(KEY_PHONE_NUMBER, "") ?: ""

    var isRememberMeEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
            scope.launch {
                userPrefsRepo.updateBiometricEnabled(value)
            }
        }

    val hasPinSet: Boolean
        get() = !prefs.getString(KEY_PIN_HASH, "").isNullOrBlank()

    init {
        // If Remember Me is true, auto-authenticate on launch
        if (hasAccount && isRememberMeEnabled) {
            _isAuthenticated.value = true
        }
    }

    /**
     * Registers a new user account on the device with PBKDF2 hashed credentials and syncs to cloud storage.
     */
    fun register(
        username: String,
        password: String,
        pin: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        rememberMe: Boolean = false
    ): Result<Unit> {
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()
        val cleanPin = pin.trim()

        if (trimmedUser.isBlank()) {
            return Result.failure(IllegalArgumentException("لطفاً نام کاربری را وارد کنید"))
        }
        if (trimmedPass.length < 4) {
            return Result.failure(IllegalArgumentException("رمز عبور باید حداقل ۴ کاراکتر باشد"))
        }
        if (cleanPin.length != 4 || !cleanPin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("پین کد امنیتی باید دقیقاً ۴ رقم باشد"))
        }

        // Generate individual PBKDF2 hashes with unique cryptographically random salts
        val passHash = PasswordHasher.hash(trimmedPass)
        val pinHash = PasswordHasher.hash(cleanPin)

        prefs.edit()
            .putBoolean(KEY_HAS_ACCOUNT, true)
            .putString(KEY_USERNAME, trimmedUser)
            .putString(KEY_PASSWORD_HASH, passHash)
            .putString(KEY_PIN_HASH, pinHash)
            .putString(KEY_FIRST_NAME, firstName.trim())
            .putString(KEY_LAST_NAME, lastName.trim())
            .putString(KEY_PHONE_NUMBER, phoneNumber.trim())
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .apply()

        scope.launch {
            userPrefsRepo.updateUserProfile(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber.trim()
            )
            triggerBackgroundSync()
        }

        _isAuthenticated.value = true
        return Result.success(Unit)
    }

    /**
     * Changes user password, persists it, and syncs to cloud storage.
     */
    fun changePassword(newPassword: String): Result<Unit> {
        val trimmed = newPassword.trim()
        if (trimmed.length < 4) {
            return Result.failure(IllegalArgumentException("رمز عبور باید حداقل ۴ کاراکتر باشد"))
        }
        val passHash = PasswordHasher.hash(trimmed)
        prefs.edit().putString(KEY_PASSWORD_HASH, passHash).apply()
        triggerBackgroundSync()
        return Result.success(Unit)
    }

    /**
     * Changes user 4-digit PIN, persists it, and syncs to cloud storage.
     */
    fun changePin(newPin: String): Result<Unit> {
        val cleanPin = newPin.trim()
        if (cleanPin.length != 4 || !cleanPin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("پین کد امنیتی باید دقیقاً ۴ رقم باشد"))
        }
        val pinHash = PasswordHasher.hash(cleanPin)
        prefs.edit().putString(KEY_PIN_HASH, pinHash).apply()
        triggerBackgroundSync()
        return Result.success(Unit)
    }

    /**
     * Updates user personal profile (First Name, Last Name, Phone Number) locally and syncs to cloud.
     */
    fun updateProfile(firstName: String, lastName: String, phoneNumber: String): Result<Unit> {
        val fName = firstName.trim()
        val lName = lastName.trim()
        val phone = phoneNumber.trim()

        prefs.edit()
            .putString(KEY_FIRST_NAME, fName)
            .putString(KEY_LAST_NAME, lName)
            .putString(KEY_PHONE_NUMBER, phone)
            .apply()

        scope.launch {
            userPrefsRepo.updateUserProfile(
                firstName = fName,
                lastName = lName,
                phoneNumber = phone
            )
            triggerBackgroundSync()
        }
        return Result.success(Unit)
    }

    /**
     * Checks if biometric hardware and enrollment are available on this device.
     */
    fun canUseBiometric(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val res = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return res == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Triggers background cloud sync with up-to-date credentials.
     */
    fun triggerBackgroundSync() {
        scope.launch {
            try {
                syncManager.syncData(
                    firstName = savedFirstName,
                    lastName = savedLastName,
                    phoneNumber = savedPhoneNumber,
                    username = savedUsername,
                    passwordHash = prefs.getString(KEY_PASSWORD_HASH, ""),
                    pinHash = prefs.getString(KEY_PIN_HASH, "")
                )
            } catch (e: Exception) {
                Log.w(TAG, "Background cloud sync failed: ${e.message}")
            }
        }
    }

    /**
     * Authenticates with Username and Password, automatically upgrading legacy SHA-256 hashes to PBKDF2.
     */
    fun loginWithPassword(username: String, password: String, rememberMe: Boolean? = null): Boolean {
        val trimmedUser = username.trim()
        val cleanPassword = password.trim()
        val savedUser = savedUsername
        val savedPassHash = prefs.getString(KEY_PASSWORD_HASH, "")

        val isUserValid = trimmedUser.equals(savedUser, ignoreCase = true) || trimmedUser == savedPhoneNumber
        val isPassValid = PasswordHasher.verify(cleanPassword, savedPassHash)

        val isValid = isUserValid && isPassValid

        if (isValid) {
            // Transparent migration: if stored hash was created with old static-salt SHA-256, upgrade to PBKDF2
            if (PasswordHasher.isLegacyHash(savedPassHash)) {
                val upgradedHash = PasswordHasher.hash(cleanPassword)
                prefs.edit().putString(KEY_PASSWORD_HASH, upgradedHash).apply()
                Log.i(TAG, "User password hash successfully migrated to PBKDF2.")
            }

            if (rememberMe != null) {
                isRememberMeEnabled = rememberMe
            }
            _isAuthenticated.value = true
        }
        return isValid
    }

    /**
     * Authenticates with 4-Digit PIN, automatically upgrading legacy SHA-256 hashes to PBKDF2.
     */
    fun loginWithPin(pin: String): Boolean {
        val cleanPin = pin.trim()
        if (cleanPin.length != 4 || !cleanPin.all { it.isDigit() }) return false

        val savedPinHash = prefs.getString(KEY_PIN_HASH, "")
        val isValid = PasswordHasher.verify(cleanPin, savedPinHash)

        if (isValid) {
            // Transparent migration: if stored PIN was created with old static-salt SHA-256, upgrade to PBKDF2
            if (PasswordHasher.isLegacyHash(savedPinHash)) {
                val upgradedPinHash = PasswordHasher.hash(cleanPin)
                prefs.edit().putString(KEY_PIN_HASH, upgradedPinHash).apply()
                Log.i(TAG, "User PIN hash successfully migrated to PBKDF2.")
            }
            _isAuthenticated.value = true
        }
        return isValid
    }

    /**
     * Unlocks session with biometric authentication.
     */
    fun unlockWithBiometric() {
        _isAuthenticated.value = true
    }

    /**
     * Triggers the native Android BiometricPrompt prompt on the given activity.
     */
    fun promptBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (!isBiometricEnabled) {
            onError("ورود با اثر انگشت در تنظیمات غیرفعال است")
            return
        }

        val biometricManager = BiometricManager.from(context)
        val canWeakOrStrong = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        val canDevice = biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ورود به کیف پول هوشمند")
            .setSubtitle("لطفاً اثر انگشت خود را تأیید کنید")

        if (canWeakOrStrong == BiometricManager.BIOMETRIC_SUCCESS) {
            promptInfoBuilder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            promptInfoBuilder.setNegativeButtonText("ورود با پین کد / رمز عبور")
        } else if (canDevice == BiometricManager.BIOMETRIC_SUCCESS) {
            promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        } else {
            when (canWeakOrStrong) {
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    onError("هیچ اثر انگشتی در تنظیمات امنیتی گوشی شما ثبت نشده است")
                    return
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    onError("این دستگاه فاقد حسگر اثر انگشت است")
                    return
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    onError("حسگر اثر انگشت در حال حاضر در دسترس نیست")
                    return
                }
                else -> {
                    promptInfoBuilder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
                    promptInfoBuilder.setNegativeButtonText("ورود با پین کد / رمز عبور")
                }
            }
        }

        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                _isAuthenticated.value = true
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("اثر انگشت شناسایی نشد، دوباره تلاش کنید")
            }
        })

        try {
            prompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Error launching biometric prompt", e)
            onError(e.message ?: "خطا در اجرای اثر انگشت")
        }
    }

    /**
     * Recovers account and data from cloud storage (Google Sheets / Apps Script backend).
     */
    suspend fun recoverFromCloud(
        phoneNumber: String,
        firstName: String,
        lastName: String,
        newUsername: String,
        newPassword: String,
        newPin: String
    ): Result<String> {
        val result = syncManager.restoreData(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            passwordOrAuthKey = newPassword.takeIf { it.isNotBlank() } ?: savedUsername
        )
        return result.mapCatching { restoreSummary ->
            val finalUsername = when {
                newUsername.isNotBlank() -> newUsername.trim()
                !restoreSummary.username.isNullOrBlank() -> restoreSummary.username
                savedUsername.isNotBlank() -> savedUsername
                else -> firstName.trim()
            }
            val finalPasswordHash = when {
                newPassword.isNotBlank() -> PasswordHasher.hash(newPassword.trim())
                prefs.getString(KEY_PASSWORD_HASH, "")?.isNotBlank() == true -> prefs.getString(KEY_PASSWORD_HASH, "")!!
                else -> ""
            }
            val finalPinHash = when {
                newPin.isNotBlank() -> PasswordHasher.hash(newPin.trim())
                prefs.getString(KEY_PIN_HASH, "")?.isNotBlank() == true -> prefs.getString(KEY_PIN_HASH, "")!!
                else -> ""
            }

            prefs.edit()
                .putBoolean(KEY_HAS_ACCOUNT, true)
                .putString(KEY_USERNAME, finalUsername)
                .putString(KEY_PASSWORD_HASH, finalPasswordHash)
                .putString(KEY_PIN_HASH, finalPinHash)
                .putString(KEY_FIRST_NAME, firstName.trim())
                .putString(KEY_LAST_NAME, lastName.trim())
                .putString(KEY_PHONE_NUMBER, phoneNumber.trim())
                .apply()

            userPrefsRepo.updateUserProfile(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber.trim()
            )

            _isAuthenticated.value = true
            restoreSummary.message
        }
    }

    /**
     * Locks the app, requiring PIN, password, or biometric to re-enter.
     */
    fun lock() {
        _isAuthenticated.value = false
    }

    /**
     * Logs out the user.
     */
    fun logout() {
        isRememberMeEnabled = false
        _isAuthenticated.value = false
    }
}
