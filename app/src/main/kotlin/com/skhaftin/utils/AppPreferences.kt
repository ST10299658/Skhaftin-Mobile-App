package com.skhaftin.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing application preferences using SharedPreferences.
 */
object AppPreferences {

    private const val PREFS_NAME = "skhaftin_prefs"
    private const val KEY_CURRENT_LANGUAGE = "current_language"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLanguage(context: Context, language: String) {
        getSharedPreferences(context).edit().putString(KEY_CURRENT_LANGUAGE, language).apply()
    }

    fun loadLanguage(context: Context): String {
        return getSharedPreferences(context).getString(KEY_CURRENT_LANGUAGE, "English") ?: "English"
    }

    fun saveBiometricEnabled(context: Context, enabled: Boolean) {
        getSharedPreferences(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun loadBiometricEnabled(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
}