package com.example.sololeveling
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance
private val Context.dataStore by preferencesDataStore("user_preferences")

class ThemePreferenceManager(private val context: Context) {
    companion object {
        private val THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
    }

    // Get the saved theme (default: Light Mode)
    val isDarkThemeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: false
        }

    // Save theme preference
    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = enabled
        }
    }
}
