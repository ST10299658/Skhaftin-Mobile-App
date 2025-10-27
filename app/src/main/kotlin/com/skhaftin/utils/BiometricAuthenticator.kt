package com.skhaftin.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Utility class for handling biometric authentication.
 * Requires a FragmentActivity context to display the BiometricPrompt.
 */
class BiometricAuthenticator(private val context: Context) {

    private val biometricManager = BiometricManager.from(context)

    /**
     * Checks if biometric authentication is available and enrolled on the device.
     * @return true if biometrics can be used, false otherwise.
     */
    fun canAuthenticateWithBiometrics(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false // No biometric hardware
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false // Hardware unavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false // No biometrics enrolled
            else -> false
        }
    }

    /**
     * Displays the biometric authentication prompt.
     * @param title The title for the biometric prompt.
     * @param subtitle The subtitle for the biometric prompt.
     * @param description The description for the biometric prompt.
     * @param negativeButtonText The text for the negative button (e.g., "Use password").
     * @param onAuthenticated Callback for successful authentication.
     * @param onFailed Callback for failed authentication (e.g., fingerprint not recognized).
     * @param onError Callback for authentication errors (e.g., too many attempts, user cancelled).
     */
    fun authenticate(
        title: String,
        subtitle: String,
        description: String,
        negativeButtonText: String,
        onAuthenticated: () -> Unit,
        onFailed: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        if (context !is FragmentActivity) {
            onError(BiometricPrompt.ERROR_VENDOR, "Biometric authentication requires a FragmentActivity context.")
            return
        }

        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(context, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticated()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}