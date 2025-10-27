package com.skhaftin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skhaftin.R

/**
 * Composable function that displays the settings screen.
 * Allows users to change language and enable biometric login.
 * @param onBackClick Callback invoked when the back button is clicked.
 * @param onLanguageChange Callback invoked when language is changed.
 * @param onBiometricToggle Callback invoked when biometric is toggled.
 * @param currentLanguage The current selected language.
 * @param isBiometricEnabled Whether biometric is enabled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    currentLanguage: String = "English",
    isBiometricEnabled: Boolean = false
) {
    val languages = listOf("English", "Afrikaans", "isiZulu")

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFFFE066)) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button),
                    tint = Color(0xFF333333)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    stringResource(R.string.settings),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    stringResource(R.string.language),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageChange(language) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == language,
                            onClick = { onLanguageChange(language) }
                        )
                        Text(text = language, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    stringResource(R.string.biometric_login),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = onBiometricToggle
                    )
 Text(stringResource(R.string.biometric_login), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
