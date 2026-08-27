package com.dosebloom.app

import android.content.Context

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
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val localeManager = context.getSystemService(android.app.LocaleManager::class.java)
            localeManager.applicationLocales = when (language) {
                RUSSIAN -> android.os.LocaleList.forLanguageTags("ru")
                ENGLISH -> android.os.LocaleList.forLanguageTags("en")
                else -> android.os.LocaleList.getEmptyLocaleList()
            }
        }
    }

    fun profileDisplayName(context: Context, profile: String): String =
        if (profile == "Я" && currentLanguage(context) == ENGLISH) "Me" else profile
}
