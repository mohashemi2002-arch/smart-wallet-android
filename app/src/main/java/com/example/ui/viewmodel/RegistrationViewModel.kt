package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.PersistentProfileBackupManager
import com.example.data.backup.RestoreSummary
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class RegistrationUiState {
    object Idle : RegistrationUiState()
    object Loading : RegistrationUiState()
    data class Success(
        val userId: String,
        val message: String,
        val restoreSummary: RestoreSummary? = null
    ) : RegistrationUiState()
    data class Error(val message: String) : RegistrationUiState()
}

class RegistrationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
    private val backupManager = PersistentProfileBackupManager(application)
    private val googleSheetsSyncManager = com.example.data.sync.GoogleSheetsSyncManager(application)

    private val _uiState = MutableStateFlow<RegistrationUiState>(RegistrationUiState.Idle)
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private val _backupFound = MutableStateFlow(false)
    val backupFound: StateFlow<Boolean> = _backupFound.asStateFlow()

    private fun normalizeDigits(input: String): String {
        return input.map { ch ->
            when (ch) {
                in '۰'..'۹' -> '0' + (ch - '۰')
                in '٠'..'٩' -> '0' + (ch - '٠')
                else -> ch
            }
        }.joinToString("")
    }

    private fun formatPhoneToEmail(phone: String): String {
        val cleanPhone = normalizeDigits(phone).replace("+", "").replace(" ", "").trim()
        val standardPhone = when {
            cleanPhone.startsWith("0") -> "98" + cleanPhone.substring(1)
            cleanPhone.startsWith("98") -> cleanPhone
            else -> "98$cleanPhone"
        }
        return "user_$standardPhone@smartwallet.internal"
    }

    fun onPhoneChanged(phone: String) {
        val cleanPhone = normalizeDigits(phone).replace(" ", "").trim()
        if (cleanPhone.length >= 10) {
            viewModelScope.launch {
                val exists = backupManager.checkBackupExists(cleanPhone)
                _backupFound.value = exists
            }
        } else {
            _backupFound.value = false
        }
    }

    fun register(firstName: String, lastName: String, phoneNumber: String, password: String) {
        val trimmedFirst = firstName.trim()
        val trimmedLast = lastName.trim()
        val cleanPhone = normalizeDigits(phoneNumber).replace(" ", "").trim()

        if (trimmedFirst.isBlank()) {
            _uiState.value = RegistrationUiState.Error("لطفاً نام خود را وارد کنید")
            return
        }

        if (trimmedLast.isBlank()) {
            _uiState.value = RegistrationUiState.Error("لطفاً نام خانوادگی خود را وارد کنید")
            return
        }

        if (cleanPhone.length < 10) {
            _uiState.value = RegistrationUiState.Error("شماره همراه وارد شده باید حداقل ۱۰ رقم باشد")
            return
        }

        if (password.length < 6) {
            _uiState.value = RegistrationUiState.Error("رمز عبور باید حداقل ۶ کاراکتر باشد")
            return
        }

        _uiState.value = RegistrationUiState.Loading

        viewModelScope.launch {
            try {
                // 1. Check & Restore Persistent Device Backup (across app uninstalls)
                var restoreResult: RestoreSummary? = null
                if (backupManager.checkBackupExists(cleanPhone)) {
                    val restored = backupManager.restoreLocalBackup(cleanPhone).getOrNull()
                    if (restored != null && (restored.restoredAccountsCount > 0 || restored.restoredTransactionsCount > 0)) {
                        restoreResult = restored
                    }
                }

                // 2. Perform Firebase Auth Registration if online
                var userUid = "local_${System.currentTimeMillis()}"
                val firebaseAuthInstance = auth
                if (firebaseAuthInstance != null) {
                    try {
                        val email = formatPhoneToEmail(cleanPhone)
                        withContext(Dispatchers.IO) {
                            val authResult = firebaseAuthInstance.createUserWithEmailAndPassword(email, password).awaitTask()
                            val user = authResult.user
                            if (user != null) {
                                userUid = user.uid
                                val profileUpdate = UserProfileChangeRequest.Builder()
                                    .setDisplayName("$trimmedFirst $trimmedLast")
                                    .build()
                                user.updateProfile(profileUpdate).awaitTask()
                            }
                        }
                    } catch (e: Exception) {
                        // If user already exists in Firebase, attempt sign-in
                        val email = formatPhoneToEmail(cleanPhone)
                        try {
                            val signResult = firebaseAuthInstance.signInWithEmailAndPassword(email, password).awaitTask()
                            signResult.user?.uid?.let { userUid = it }
                        } catch (signInEx: Exception) {
                            // Fallback to local user
                        }
                    }
                }

                // 3. Immediately Create/Update Persistent Local Backup on Device Storage
                backupManager.createLocalBackup(trimmedFirst, trimmedLast, cleanPhone)
                com.example.data.UserPreferencesRepository(getApplication()).updateUserProfile(
                    firstName = trimmedFirst,
                    lastName = trimmedLast,
                    phoneNumber = cleanPhone
                )

                // 4. Auto-Sync to Google Sheets Web App in background
                viewModelScope.launch(Dispatchers.IO) {
                    googleSheetsSyncManager.syncData(trimmedFirst, trimmedLast, cleanPhone)
                }

                val message = if (restoreResult != null) {
                    "ثبت‌نام انجام شد! اطلاعات قبلی شامل ${restoreResult.restoredAccountsCount} کارت و ${restoreResult.restoredTransactionsCount} تراکنش بازیابی شدند."
                } else {
                    "ثبت‌نام با موفقیت انجام شد و اطلاعات با فضای ابری همگام‌سازی گردید."
                }

                _uiState.value = RegistrationUiState.Success(
                    userId = userUid,
                    message = message,
                    restoreSummary = restoreResult
                )
            } catch (e: Exception) {
                val message = mapFirebaseError(e)
                _uiState.value = RegistrationUiState.Error(message)
            }
        }
    }

    fun restoreFromGoogleSheets(firstName: String, lastName: String, phoneNumber: String) {
        val trimmedFirst = firstName.trim()
        val trimmedLast = lastName.trim()
        val cleanPhone = normalizeDigits(phoneNumber).replace(" ", "").trim()

        if (cleanPhone.length < 10) {
            _uiState.value = RegistrationUiState.Error("شماره همراه باید حداقل ۱۰ رقم باشد")
            return
        }

        _uiState.value = RegistrationUiState.Loading

        viewModelScope.launch {
            val result = googleSheetsSyncManager.restoreData(trimmedFirst, trimmedLast, cleanPhone)
            result.fold(
                onSuccess = { res ->
                    com.example.data.UserPreferencesRepository(getApplication()).updateUserProfile(
                        firstName = trimmedFirst,
                        lastName = trimmedLast,
                        phoneNumber = cleanPhone
                    )
                    backupManager.createLocalBackup(trimmedFirst, trimmedLast, cleanPhone)

                    _uiState.value = RegistrationUiState.Success(
                        userId = "user_$cleanPhone",
                        message = "اطلاعات با موفقیت از فضای ابری بازیابی شد!\nتعداد ${res.restoredCardsCount} کارت و ${res.restoredTransactionsCount} تراکنش بازگردانی شدند."
                    )
                },
                onFailure = { err ->
                    // Fallback to persistent local storage backup if online call fails
                    val localRestored = backupManager.restoreLocalBackup(cleanPhone).getOrNull()
                    if (localRestored != null && (localRestored.restoredAccountsCount > 0 || localRestored.restoredTransactionsCount > 0)) {
                        com.example.data.UserPreferencesRepository(getApplication()).updateUserProfile(
                            firstName = trimmedFirst,
                            lastName = trimmedLast,
                            phoneNumber = cleanPhone
                        )
                        _uiState.value = RegistrationUiState.Success(
                            userId = "user_$cleanPhone",
                            message = "اطلاعات از فایل پشتیبان حافظه گوشی بازیابی شد (عدم پاسخ فضای ابری: ${err.localizedMessage})",
                            restoreSummary = localRestored
                        )
                    } else {
                        _uiState.value = RegistrationUiState.Error("خطا در بازیابی اطلاعات: ${err.localizedMessage}")
                    }
                }
            )
        }
    }

    private fun mapFirebaseError(e: Exception): String {
        val msg = e.localizedMessage ?: ""
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) ->
                "این شماره موبایل قبلاً ثبت‌نام شده است. لطفاً وارد حساب شوید یا از دکمه بازیابی استفاده کنید."
            msg.contains("Password should be at least", ignoreCase = true) ->
                "رمز عبور باید حداقل ۶ کاراکتر باشد."
            msg.contains("network error", ignoreCase = true) ->
                "خطا در اتصال به اینترنت. لطفاً شبکه خود را بررسی کنید."
            else -> msg.ifBlank { "خطایی رخ داد. لطفاً دوباره تلاش کنید." }
        }
    }

    fun resetState() {
        _uiState.value = RegistrationUiState.Idle
    }
}

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) continuation.resume(result)
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (continuation.isActive) continuation.cancel()
    }
}
