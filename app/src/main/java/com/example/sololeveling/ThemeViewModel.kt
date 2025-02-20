package com.example.sololeveling

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val themePreferenceManager = ThemePreferenceManager(application)

    // 🔹 Flow to observe the theme state
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> get() = _isDarkTheme

    init {
        // 🔹 Load the saved theme preference when ViewModel is created
        viewModelScope.launch {
            themePreferenceManager.isDarkThemeEnabled.collect { savedTheme ->
                _isDarkTheme.value = savedTheme
            }
        }
    }

    // 🔹 Toggle the theme and persist the choice
    fun toggleTheme() {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme

        // Save the user's choice to DataStore
        viewModelScope.launch {
            themePreferenceManager.setDarkThemeEnabled(newTheme)
        }
    }
}
