package com.dosebloom.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object Localization {
    private const val PREFS = "dosebloom_localization"
    private const val KEY_LANGUAGE = "language"
    const val SYSTEM = "system"
    const val RUSSIAN = "ru"
    const val ENGLISH = "en"

    fun currentLanguage(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, SYSTEM) ?: SYSTEM

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, language).apply()
        val locales = when (language) {
            RUSSIAN -> LocaleListCompat.forLanguageTags("ru")
            ENGLISH -> LocaleListCompat.forLanguageTags("en")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun profileDisplayName(context: Context, profile: String): String =
        if (profile == "Я" && currentLanguage(context) == ENGLISH) "Me" else profile
}
