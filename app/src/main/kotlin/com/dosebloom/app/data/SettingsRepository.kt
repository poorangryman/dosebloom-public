package com.dosebloom.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "dosebloom_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val selectedProfile = stringPreferencesKey("selected_profile")
        val language = stringPreferencesKey("language")
        val darkMode = booleanPreferencesKey("dark_mode")
    }

    val selectedProfile: Flow<String> = context.settingsDataStore.data.map { it[Keys.selectedProfile] ?: "Я" }
    val language: Flow<String> = context.settingsDataStore.data.map { it[Keys.language] ?: "system" }
    val darkMode: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.darkMode] ?: false }

    suspend fun setSelectedProfile(value: String) = context.settingsDataStore.edit { it[Keys.selectedProfile] = value }
    suspend fun setLanguage(value: String) = context.settingsDataStore.edit { it[Keys.language] = value }
    suspend fun setDarkMode(value: Boolean) = context.settingsDataStore.edit { it[Keys.darkMode] = value }
}
