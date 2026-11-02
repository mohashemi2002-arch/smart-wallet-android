package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val biometricEnabled: Boolean = true,
    val userFirstName: String = "",
    val userLastName: String = "",
    val userPhoneNumber: String = ""
)

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    val userPreferencesFlow: Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            val theme = runCatching {
                AppTheme.valueOf(prefs[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name)
            }.getOrDefault(AppTheme.SYSTEM)

            UserPreferences(
                appTheme = theme,
                biometricEnabled = prefs[PreferencesKeys.BIOMETRIC_ENABLED] ?: true,
                userFirstName = prefs[PreferencesKeys.USER_FIRST_NAME] ?: "",
                userLastName = prefs[PreferencesKeys.USER_LAST_NAME] ?: "",
                userPhoneNumber = prefs[PreferencesKeys.USER_PHONE_NUMBER] ?: ""
            )
        }

    suspend fun updateAppTheme(theme: AppTheme) {
        dataStore.edit { pref ->
            pref[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    suspend fun updateBiometricEnabled(enabled: Boolean) {
        dataStore.edit { pref ->
            pref[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun updateUserProfile(
        firstName: String,
        lastName: String,
        phoneNumber: String
    ) {
        dataStore.edit { pref ->
            pref[PreferencesKeys.USER_FIRST_NAME] = firstName.trim()
            pref[PreferencesKeys.USER_LAST_NAME] = lastName.trim()
            pref[PreferencesKeys.USER_PHONE_NUMBER] = phoneNumber.trim()
        }
    }

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val USER_FIRST_NAME = stringPreferencesKey("user_first_name")
        val USER_LAST_NAME = stringPreferencesKey("user_last_name")
        val USER_PHONE_NUMBER = stringPreferencesKey("user_phone_number")
    }
}
