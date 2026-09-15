package com.phoenix.fitpro.data.remote.sync

import com.phoenix.fitpro.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Point d'entrée unique vers Supabase (authentification + stockage du snapshot).
 *
 * Si `supabase.url` / `supabase.anon.key` sont absents de local.properties,
 * [isConfigured] vaut false et l'application fonctionne en mode local seul.
 */
@Singleton
class SupabaseBackend @Inject constructor() {

    init {
        // Hilt n'instancie ce singleton qu'une fois : on expose la reference aux
        // appelants situes hors du graphe d'injection (voir AiService).
        current = this
    }

    val isConfigured: Boolean =
        BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client: SupabaseClient? by lazy {
        if (!isConfigured) null
        else runCatching {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth) {
                    // Redirection du retour OAuth vers l'application. Doit etre
                    // declaree telle quelle dans Supabase > Authentication >
                    // URL Configuration > Redirect URLs : phoenixfit://login-callback
                    scheme = OAUTH_SCHEME
                    host = OAUTH_HOST
                }
                install(Postgrest)
            }
        }.getOrNull()
    }

    // ── Authentification ──────────────────────────────────────────────────────

    suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
        val supabase = client ?: error(NOT_CONFIGURED)
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // Si la confirmation par email est désactivée, la session est déjà ouverte.
        // Sinon on tente une connexion immédiate pour obtenir la session.
        if (supabase.auth.currentSessionOrNull() == null) {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        val supabase = client ?: error(NOT_CONFIGURED)
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Lance la connexion Google. La methode rend la main immediatement : le
     * navigateur s'ouvre, et la session arrive ensuite par le lien profond que
     * MainActivity transmet a handleDeeplinks.
     */
    suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        val supabase = client ?: error(NOT_CONFIGURED)
        supabase.auth.signInWith(Google)
    }

    /** Etat de session, pour reagir au retour d'une connexion OAuth. */
    fun sessionStatusFlow(): Flow<SessionStatus>? = client?.auth?.sessionStatus

    /** Nom affichable fourni par le fournisseur OAuth, quand il en donne un. */
    fun currentUserDisplayName(): String? {
        val meta = client?.auth?.currentUserOrNull()?.userMetadata ?: return null
        return listOf("full_name", "name").firstNotNullOfOrNull { key ->
            (meta[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
        }
    }

    suspend fun signOut(): Result<Unit> = runCatching {
        client?.auth?.signOut()
        Unit
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        val supabase = client ?: error(NOT_CONFIGURED)
        supabase.auth.updateUser { password = newPassword }
        Unit
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        val supabase = client ?: error(NOT_CONFIGURED)
        supabase.auth.resetPasswordForEmail(email)
    }

    /** Jeton d'acces de la session en cours, envoye au proxy IA. */
    fun accessToken(): String? = client?.auth?.currentSessionOrNull()?.accessToken

    fun currentUserId(): String? = client?.auth?.currentUserOrNull()?.id
    fun currentUserEmail(): String? = client?.auth?.currentUserOrNull()?.email
    fun hasSession(): Boolean = client?.auth?.currentSessionOrNull() != null

    /** Attend que la session enregistrée sur l'appareil soit rechargée au démarrage. */
    suspend fun awaitSessionRestore() {
        client?.auth?.awaitInitialization()
    }

    // ── Snapshot ──────────────────────────────────────────────────────────────

    suspend fun pull(): Result<SyncSnapshot?> = runCatching {
        val supabase = client ?: error(NOT_CONFIGURED)
        val userId = supabase.auth.currentUserOrNull()?.id ?: error(NO_SESSION)
        supabase.from(TABLE)
            .select {
                filter { eq("user_id", userId) }
                limit(1)
            }
            .decodeSingleOrNull<UserDataRow>()
            ?.payload
    }

    suspend fun push(snapshot: SyncSnapshot): Result<Unit> = runCatching {
        val supabase = client ?: error(NOT_CONFIGURED)
        val userId = supabase.auth.currentUserOrNull()?.id ?: error(NO_SESSION)
        supabase.from(TABLE).upsert(UserDataUpsert(userId = userId, payload = snapshot))
        Unit
    }

    companion object {
        /** Instance unique creee par Hilt, ou null avant le premier acces. */
        @Volatile
        var current: SupabaseBackend? = null
            private set

        const val OAUTH_SCHEME = "phoenixfit"
        const val OAUTH_HOST = "login-callback"

        private const val TABLE = "user_data"
        private const val NOT_CONFIGURED =
            "Synchronisation non configurée : ajoute supabase.url et supabase.anon.key dans local.properties."
        private const val NO_SESSION = "Aucune session active."
    }
}
