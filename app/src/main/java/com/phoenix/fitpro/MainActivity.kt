package com.phoenix.fitpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.phoenix.fitpro.presentation.navigation.PhoenixNavHost
import com.phoenix.fitpro.presentation.theme.PhoenixTheme
import com.phoenix.fitpro.presentation.ui.auth.AuthScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhoenixTheme {
                val auth = remember { FirebaseAuth.getInstance() }
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
                    auth.addAuthStateListener(listener)
                    onDispose { auth.removeAuthStateListener(listener) }
                }

                if (currentUser?.isEmailVerified == true) PhoenixNavHost() else AuthScreen()
            }
        }
    }
}
