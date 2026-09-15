package com.phoenix.fitpro

import android.content.Intent
import android.os.Bundle
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.data.remote.sync.SupabaseBackend
import com.phoenix.fitpro.domain.repository.SyncRepository
import io.github.jan.supabase.auth.handleDeeplinks
import com.phoenix.fitpro.presentation.navigation.PhoenixNavHost
import com.phoenix.fitpro.presentation.theme.PhoenixTheme
import com.phoenix.fitpro.presentation.util.LanguageHelper
import com.phoenix.fitpro.presentation.ui.auth.AuthScreen
import com.phoenix.fitpro.presentation.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var supabaseBackend: SupabaseBackend

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Aligne la locale JVM sur celle reellement retenue pour les ressources.
        // Sans cela, Locale.getDefault() renvoie la langue du SYSTEME alors que
        // l'interface suit la langue choisie dans l'application : la logique
        // metier (conseils du coach, libelles nutrition) repondait en anglais
        // pendant que l'ecran affichait du francais.
        // LanguageHelper connait le choix explicite de l'utilisateur (LocaleManager
        // sur Android 13+, AppCompatDelegate en dessous) et retombe sur la locale
        // systeme a defaut. Lire la config des ressources directement ecraserait
        // ce choix sur les versions ou il n'est pas reporte dans la configuration.
        Locale.setDefault(Locale(LanguageHelper.getCurrentLanguage(this)))

        // Retour de connexion Google : c'est supabase-kt qui extrait la session
        // du lien profond, encore faut-il le lui transmettre.
        supabaseBackend.client?.handleDeeplinks(intent)

        setContent {
            PhoenixTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val currentEmail by authViewModel.currentEmail.collectAsStateWithLifecycle()

                if (currentEmail != null) PhoenixNavHost() else AuthScreen(authViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabaseBackend.client?.handleDeeplinks(intent)
    }

    override fun onStop() {
        super.onStop()
        // Sauvegarde cloud des dernières modifications quand l'app passe en arrière-plan.
        syncRepository.pushInBackground()
    }
}
