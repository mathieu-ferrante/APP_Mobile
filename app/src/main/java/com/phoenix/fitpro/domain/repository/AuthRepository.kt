package com.phoenix.fitpro.domain.repository

import com.phoenix.fitpro.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class AuthAccount(
    val email: String,
    val displayName: String = "",
    val lastLoginEpochMs: Long = System.currentTimeMillis()
)

/** Résultat d'une opération d'authentification. */
sealed interface AuthResult {
    data class Success(val profile: UserProfile) : AuthResult
    data class Failure(val message: String) : AuthResult
}

interface AuthRepository {
    val currentEmailFlow: StateFlow<String?>
    fun getCurrentEmail(): String?
    fun observeSavedAccounts(): Flow<List<AuthAccount>>

    /** true si une empreinte de mot de passe existe sur cet appareil pour cet email. */
    fun hasAccount(email: String): Boolean

    /** Crée le compte (cloud si configuré) et ouvre la session. */
    suspend fun signUp(email: String, password: String, displayName: String? = null): AuthResult

    /** Connecte l'utilisateur et récupère ses données depuis le cloud. */
    suspend fun signIn(email: String, password: String): AuthResult

    /** Change le mot de passe conservé sur cet appareil. */
    suspend fun changePassword(email: String, oldPassword: String, newPassword: String): AuthResult

    /** Envoie un email de réinitialisation (nécessite la synchronisation cloud). */
    suspend fun sendPasswordReset(email: String): AuthResult

    /**
     * Ouvre la connexion Google. Rend la main immédiatement : le navigateur
     * s'ouvre et la session revient par lien profond, d'où [completeOAuthSession].
     */
    suspend fun signInWithGoogle(): AuthResult

    /**
     * À appeler quand une session Supabase apparaît sans passer par le
     * formulaire : crée ou recharge le profil local correspondant.
     * Renvoie null s'il n'y a rien à faire.
     */
    suspend fun completeOAuthSession(): AuthResult?

    /** Connexion sans mot de passe : réservée au changement de compte depuis le profil. */
    suspend fun signInWithEmail(email: String, displayName: String? = null): UserProfile

    suspend fun switchAccount(email: String): UserProfile
    suspend fun signOut()
    suspend fun deleteAccount(email: String)
}
