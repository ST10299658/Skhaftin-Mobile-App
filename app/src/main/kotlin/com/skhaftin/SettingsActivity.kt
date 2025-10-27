package com.skhaftin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.skhaftin.ui.SettingsScreen
import com.skhaftin.utils.setLocale
import com.skhaftin.utils.AppPreferences
import com.skhaftin.utils.BiometricAuthenticator

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricAuthenticator = BiometricAuthenticator(this)

        setContent {
            var currentLanguage by remember { mutableStateOf(AppPreferences.loadLanguage(this)) }
            var isBiometricEnabled by remember { mutableStateOf(AppPreferences.loadBiometricEnabled(this)) }

            SettingsScreen(
                onBackClick = {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                },
                onLanguageChange = { language ->
                    AppPreferences.saveLanguage(this, language)
                    currentLanguage = language
                    setLocale(language.take(2).lowercase(), this)
                    // Restart activity to apply language change
                    recreate()
                },
                onBiometricToggle = { enabled ->
                    if (enabled && !biometricAuthenticator.canAuthenticateWithBiometrics()) {
                        // If trying to enable but device doesn't support it, show a message
                        Toast.makeText(this, getString(R.string.biometric_not_available), Toast.LENGTH_LONG).show()
                        // Don't update the state
                    } else {
                        // Otherwise, update the state and save the preference
                        isBiometricEnabled = enabled
                        AppPreferences.saveBiometricEnabled(this, enabled)
                    }
                },
                currentLanguage = currentLanguage,
                isBiometricEnabled = isBiometricEnabled
            )
        }
    }
}
