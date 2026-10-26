package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AutoLockTimeout
import com.example.data.CurrencyMode
import com.example.data.UserPreferences
import com.example.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.data.database.AppDatabase

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserPreferencesRepository(application)

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateCurrencyMode(mode: CurrencyMode) {
        viewModelScope.launch {
            repository.updateCurrencyMode(mode)
        }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateBiometricEnabled(enabled)
        }
    }

    fun updateAutoLockTimeout(timeout: AutoLockTimeout) {
        viewModelScope.launch {
            repository.updateAutoLockTimeout(timeout)
        }
    }

    fun updatePrivacyModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePrivacyModeEnabled(enabled)
        }
    }

    fun importSmartWalletExcel(uri: android.net.Uri, context: android.content.Context, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val rows = withContext(Dispatchers.IO) {
                    com.example.data.SmartWalletExcelImporter.parse(context, uri)
                }
                
                if (rows.isEmpty()) {
                    onResult("فایلی انتخاب نشد یا فرمت پشتیبانی نمی‌شود.")
                    return@launch
                }
                
                val (txCount, accCount) = withContext(Dispatchers.IO) {
                    com.example.data.FinanceRepository.getInstance(context).importSmartWalletExcel(rows)
                }
                onResult("$txCount تراکنش و $accCount حساب بانکی با موفقیت وارد شدند.")
            } catch (e: Exception) {
                e.printStackTrace()
                onResult("خطا در پردازش فایل: ${e.localizedMessage}")
            }
        }
    }

    fun createPersistentBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prefs = userPreferences.value
            val phone = prefs.userPhoneNumber.ifBlank { "09000000000" }
            val first = prefs.userFirstName.ifBlank { "کاربر" }
            val last = prefs.userLastName.ifBlank { "اصلی" }

            val backupManager = com.example.data.backup.PersistentProfileBackupManager(getApplication())
            val result = backupManager.createLocalBackup(first, last, phone)
            result.fold(
                onSuccess = { file ->
                    onResult("نسخه پشتیبان پایدار با موفقیت در پوشه اسناد ذخیره شد:\n${file.name}")
                },
                onFailure = { err ->
                    onResult("خطا در ذخیره فایل پشتیبان: ${err.localizedMessage}")
                }
            )
        }
    }

    fun restorePersistentBackup(customPhone: String? = null, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prefs = userPreferences.value
            val phone = (customPhone ?: prefs.userPhoneNumber).ifBlank { "09000000000" }
            val backupManager = com.example.data.backup.PersistentProfileBackupManager(getApplication())
            val result = backupManager.restoreLocalBackup(phone)
            result.fold(
                onSuccess = { summary ->
                    onResult("اطلاعات با موفقیت بازیابی شد!\nتعداد ${summary.restoredAccountsCount} حساب و ${summary.restoredTransactionsCount} تراکنش بازگردانی شدند.")
                },
                onFailure = { err ->
                    onResult("خطا در بازیابی اطلاعات: ${err.localizedMessage}")
                }
            )
        }
    }

    fun syncWithGoogleSheets(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prefs = userPreferences.value
            if (prefs.userPhoneNumber.isBlank()) {
                onResult("لطفاً ابتدا نام و شماره موبایل خود را در بخش حساب کاربری ثبت کنید.")
                return@launch
            }
            val syncManager = com.example.data.sync.GoogleSheetsSyncManager(getApplication())
            val result = syncManager.syncData(
                firstName = prefs.userFirstName,
                lastName = prefs.userLastName,
                phoneNumber = prefs.userPhoneNumber
            )
            result.fold(
                onSuccess = { res ->
                    onResult("همگام‌سازی با فضای ابری موفقیت‌آمیز بود!\nتعداد ${res.syncedCardsCount} کارت و ${res.syncedTransactionsCount} تراکنش همگام شدند.")
                },
                onFailure = { err ->
                    onResult("خطا در همگام‌سازی با فضای ابری: ${err.localizedMessage}")
                }
            )
        }
    }

    fun restoreFromGoogleSheets(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prefs = userPreferences.value
            if (prefs.userPhoneNumber.isBlank()) {
                onResult("لطفاً ابتدا نام و شماره موبایل خود را در بخش حساب کاربری وارد کنید.")
                return@launch
            }
            val syncManager = com.example.data.sync.GoogleSheetsSyncManager(getApplication())
            val result = syncManager.restoreData(
                firstName = prefs.userFirstName,
                lastName = prefs.userLastName,
                phoneNumber = prefs.userPhoneNumber
            )
            result.fold(
                onSuccess = { res ->
                    onResult("بازیابی از فضای ابری با موفقیت انجام شد!\nتعداد ${res.restoredCardsCount} کارت و ${res.restoredTransactionsCount} تراکنش بازگردانی شدند.")
                },
                onFailure = { err ->
                    onResult("خطا در بازیابی از فضای ابری: ${err.localizedMessage}")
                }
            )
        }
    }

    fun wipeEntireDatabase(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(getApplication()).clearAllTables()
            }
            onComplete()
        }
    }
}
