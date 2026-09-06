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

    fun wipeEntireDatabase(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(getApplication()).clearAllTables()
            }
            onComplete()
        }
    }
}
