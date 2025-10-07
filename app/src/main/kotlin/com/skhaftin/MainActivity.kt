package com.skhaftin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.FirebaseDatabase
import com.skhaftin.data.DataRepository
import com.skhaftin.ui.MainScreen
import com.skhaftin.ui.SplashScreen
import com.skhaftin.ui.LoadingScreen
import com.skhaftin.utils.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    companion object {
        const val REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load and set locale
        val language = loadLanguage(this)
        setLocale(language, this)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Firebase Analytics
        FirebaseAnalytics.getInstance(this)

        // Enable offline persistence (only once)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
            }
        }



        setContent {
            val activity = this@MainActivity
            var currentScreen by remember { mutableStateOf("splash") }

            LaunchedEffect(Unit) {
                delay(2000)
                currentScreen = "loading"
                delay(2000)
                currentScreen = "main"
            }

            when (currentScreen) {
                "splash" -> SplashScreen()
                "loading" -> LoadingScreen()
                "main" -> MainScreen(
                    onLoginClick = {
                        startActivity(Intent(activity, LoginActivity::class.java))
                    },
                    onRegisterClick = {
                        startActivity(Intent(activity, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}
