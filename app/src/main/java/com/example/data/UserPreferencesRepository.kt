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
    val appTheme: AppTheme = AppTheme.SYSTEM
)

class UserPreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    val userPreferencesFlow: Flow<UserPreferences> =
        dataStore.data.map { prefs ->
            val theme = runCatching {
                AppTheme.valueOf(prefs[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name)
            }.getOrDefault(AppTheme.SYSTEM)

            UserPreferences(
                appTheme = theme
            )
        }

    suspend fun updateAppTheme(theme: AppTheme) {
        dataStore.edit { pref ->
            pref[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
    }
}
