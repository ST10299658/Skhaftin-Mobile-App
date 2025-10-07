package com.skhaftin.utils

import android.content.Context
import java.util.Locale

fun setLocale(languageCode: String, context: Context) {
    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

fun saveLanguage(context: Context, languageCode: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("language", languageCode).apply()
}

fun loadLanguage(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getString("language", "en") ?: "en"
}
