package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AutoLockTimeout { IMMEDIATE, MIN_1, MIN_5 }
enum class CurrencyMode { TOMAN, RIAL }

data class UserPreferences(
    val currencyMode: CurrencyMode = CurrencyMode.TOMAN,
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.IMMEDIATE,
    val privacyModeEnabled: Boolean = false,
    val userFirstName: String = "",
    val userLastName: String = "",
    val userPhoneNumber: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private val dataStore = context.dataStore

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val currencyMode = CurrencyMode.valueOf(
                preferences[PreferencesKeys.CURRENCY_MODE] ?: CurrencyMode.TOMAN.name
            )
            val biometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
            val autoLockTimeout = AutoLockTimeout.valueOf(
                preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] ?: AutoLockTimeout.IMMEDIATE.name
            )
            val privacyModeEnabled = preferences[PreferencesKeys.PRIVACY_MODE_ENABLED] ?: false
            val firstName = preferences[PreferencesKeys.USER_FIRST_NAME] ?: ""
            val lastName = preferences[PreferencesKeys.USER_LAST_NAME] ?: ""
            val phone = preferences[PreferencesKeys.USER_PHONE] ?: ""

            UserPreferences(
                currencyMode = currencyMode,
                biometricEnabled = biometricEnabled,
                autoLockTimeout = autoLockTimeout,
                privacyModeEnabled = privacyModeEnabled,
                userFirstName = firstName,
                userLastName = lastName,
                userPhoneNumber = phone
            )
        }

    suspend fun updateCurrencyMode(mode: CurrencyMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_MODE] = mode.name
        }
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun updateAutoLockTimeout(timeout: AutoLockTimeout) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] = timeout.name
        }
    }

    suspend fun updatePrivacyModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRIVACY_MODE_ENABLED] = enabled
        }
    }

    suspend fun updateUserProfile(firstName: String, lastName: String, phoneNumber: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_FIRST_NAME] = firstName
            preferences[PreferencesKeys.USER_LAST_NAME] = lastName
            preferences[PreferencesKeys.USER_PHONE] = phoneNumber
        }
    }

    private object PreferencesKeys {
        val CURRENCY_MODE = stringPreferencesKey("currency_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_TIMEOUT = stringPreferencesKey("auto_lock_timeout")
        val PRIVACY_MODE_ENABLED = booleanPreferencesKey("privacy_mode_enabled")
        val USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        val USER_LAST_NAME = stringPreferencesKey("user_last_name")
        val USER_PHONE = stringPreferencesKey("user_phone")
    }
}
