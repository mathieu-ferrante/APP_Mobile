package com.phoenix.fitpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.domain.repository.SyncRepository
import com.phoenix.fitpro.presentation.navigation.PhoenixNavHost
import com.phoenix.fitpro.presentation.theme.PhoenixTheme
import com.phoenix.fitpro.presentation.ui.auth.AuthScreen
import com.phoenix.fitpro.presentation.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var syncRepository: SyncRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhoenixTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val currentEmail by authViewModel.currentEmail.collectAsStateWithLifecycle()

                if (currentEmail != null) PhoenixNavHost() else AuthScreen(authViewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Sauvegarde cloud des dernières modifications quand l'app passe en arrière-plan.
        syncRepository.pushInBackground()
    }
}
