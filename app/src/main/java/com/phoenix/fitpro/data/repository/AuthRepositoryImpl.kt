package com.phoenix.fitpro.data.repository

import android.content.Context
import android.util.Base64
import com.phoenix.fitpro.data.remote.sync.SupabaseBackend
import com.phoenix.fitpro.domain.model.UserProfile
import com.phoenix.fitpro.domain.repository.AuthAccount
import com.phoenix.fitpro.domain.repository.AuthRepository
import com.phoenix.fitpro.domain.repository.AuthResult
import com.phoenix.fitpro.domain.repository.SyncRepository
import com.phoenix.fitpro.domain.repository.UserRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentification par email + mot de passe.
 *
 * Le compte vit sur Supabase, ce qui permet de le retrouver depuis n'importe quel
 * appareil avec les mêmes identifiants. Une empreinte du mot de passe est aussi
 * conservée localement pour autoriser l'ouverture de l'application hors ligne.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val userRepo: UserRepository,
    private val backend: SupabaseBackend,
    private val syncRepo: SyncRepository
) : AuthRepository {

    private val prefs = context.getSharedPreferences("ashes_auth_accounts_prefs", Context.MODE_PRIVATE)

    private val _currentEmail = MutableStateFlow<String?>(null)
    override val currentEmailFlow: StateFlow<String?> = _currentEmail.asStateFlow()

    private val _savedAccounts = MutableStateFlow<List<AuthAccount>>(emptyList())

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        _currentEmail.value = prefs.getString(KEY_ACTIVE_EMAIL, null)

        val accountsJson = prefs.getString(KEY_SAVED_ACCOUNTS, null)
        if (accountsJson != null) {
            _savedAccounts.value = try {
                val type = object : TypeToken<List<AuthAccount>>() {}.type
                gson.fromJson<List<AuthAccount>>(accountsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override fun getCurrentEmail(): String? = _currentEmail.value

    override fun observeSavedAccounts(): Flow<List<AuthAccount>> = _savedAccounts.asStateFlow()

    override fun hasAccount(email: String): Boolean =
        prefs.getString(passwordKey(email), null) != null

    // ── Création de compte ────────────────────────────────────────────────────

    override suspend fun signUp(email: String, password: String, displayName: String?): AuthResult {
        val cleanEmail = email.trim().lowercase()

        validateEmail(cleanEmail)?.let { return AuthResult.Failure(it) }
        validatePassword(password)?.let { return AuthResult.Failure(it) }

        if (backend.isConfigured) {
            val remote = backend.signUp(cleanEmail, password)
            if (remote.isFailure) {
                return AuthResult.Failure(translate(remote.exceptionOrNull()))
            }
            storePasswordHash(cleanEmail, password)
            val profile = signInWithEmail(cleanEmail, displayName)
            // Aucun snapshot distant pour un nouveau compte : le pull enverra le local.
            syncRepo.pullFromCloud()
            return AuthResult.Success(profile)
        }

        if (hasAccount(cleanEmail)) {
            return AuthResult.Failure("Un compte existe déjà avec cet email. Connecte-toi.")
        }
        storePasswordHash(cleanEmail, password)
        return AuthResult.Success(signInWithEmail(cleanEmail, displayName))
    }

    // ── Connexion ─────────────────────────────────────────────────────────────

    override suspend fun signIn(email: String, password: String): AuthResult {
        val cleanEmail = email.trim().lowercase()

        validateEmail(cleanEmail)?.let { return AuthResult.Failure(it) }
        if (password.isBlank()) return AuthResult.Failure("Saisis ton mot de passe.")

        if (backend.isConfigured) {
            val remote = backend.signIn(cleanEmail, password)
            if (remote.isSuccess) {
                storePasswordHash(cleanEmail, password)
                signInWithEmail(cleanEmail, null)
                syncRepo.pullFromCloud()
                return AuthResult.Success(userRepo.getProfile().copy(email = cleanEmail))
            }

            val error = remote.exceptionOrNull()
            // Hors ligne : on autorise l'accès local si le mot de passe correspond.
            if (isNetworkError(error) && verifyLocalPassword(cleanEmail, password)) {
                return AuthResult.Success(signInWithEmail(cleanEmail, null))
            }
            return AuthResult.Failure(translate(error))
        }

        if (!verifyLocalPassword(cleanEmail, password)) {
            return if (hasAccount(cleanEmail)) AuthResult.Failure("Mot de passe incorrect.")
            else AuthResult.Failure("Aucun compte trouvé pour cet email. Crée ton compte.")
        }
        return AuthResult.Success(signInWithEmail(cleanEmail, null))
    }

    override suspend fun sendMagicLink(email: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        validateEmail(cleanEmail)?.let { return AuthResult.Failure(it) }
        if (!backend.isConfigured) {
            return AuthResult.Failure("La connexion par lien nécessite la synchronisation cloud.")
        }
        val sent = backend.sendMagicLink(cleanEmail)
        return if (sent.isSuccess) {
            AuthResult.Failure("")  // succès sans session : le lien vient d'être envoyé
        } else {
            AuthResult.Failure(translate(sent.exceptionOrNull()))
        }
    }

    override suspend fun completeOAuthSession(): AuthResult? {
        if (!backend.isConfigured) return null
        val email = backend.currentUserEmail()?.trim()?.lowercase() ?: return null
        if (email == getCurrentEmail()) return null

        signInWithEmail(email, backend.currentUserDisplayName())
        syncRepo.pullFromCloud()
        return AuthResult.Success(userRepo.getProfile().copy(email = email))
    }

    override suspend fun changePassword(
        email: String,
        oldPassword: String,
        newPassword: String
    ): AuthResult {
        val cleanEmail = email.trim().lowercase()
        if (!verifyLocalPassword(cleanEmail, oldPassword)) {
            return AuthResult.Failure("Mot de passe actuel incorrect.")
        }
        validatePassword(newPassword)?.let { return AuthResult.Failure(it) }

        if (backend.isConfigured && backend.hasSession()) {
            val remote = backend.updatePassword(newPassword)
            if (remote.isFailure) return AuthResult.Failure(translate(remote.exceptionOrNull()))
        }

        storePasswordHash(cleanEmail, newPassword)
        return AuthResult.Success(userRepo.getProfile())
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        val cleanEmail = email.trim().lowercase()
        validateEmail(cleanEmail)?.let { return AuthResult.Failure(it) }

        if (!backend.isConfigured) {
            return AuthResult.Failure(
                "La réinitialisation par email demande la synchronisation cloud."
            )
        }
        val result = backend.sendPasswordReset(cleanEmail)
        return if (result.isSuccess) AuthResult.Success(userRepo.getProfile())
        else AuthResult.Failure(translate(result.exceptionOrNull()))
    }

    // ── Session ───────────────────────────────────────────────────────────────

    override suspend fun signInWithEmail(email: String, displayName: String?): UserProfile {
        val cleanEmail = email.trim().lowercase()
        val cleanName = displayName?.trim().orEmpty()

        val currentAccounts = _savedAccounts.value.toMutableList()
        val existingIndex = currentAccounts.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }
        val updatedAccount = AuthAccount(
            email = cleanEmail,
            displayName = if (cleanName.isNotBlank()) cleanName
            else (currentAccounts.getOrNull(existingIndex)?.displayName ?: cleanEmail.substringBefore("@")),
            lastLoginEpochMs = System.currentTimeMillis()
        )

        if (existingIndex >= 0) currentAccounts[existingIndex] = updatedAccount
        else currentAccounts.add(0, updatedAccount)

        _savedAccounts.value = currentAccounts
        prefs.edit()
            .putString(KEY_SAVED_ACCOUNTS, gson.toJson(currentAccounts))
            .putString(KEY_ACTIVE_EMAIL, cleanEmail)
            .apply()
        _currentEmail.value = cleanEmail

        val savedProfileJson = prefs.getString("profile_$cleanEmail", null)
        val profile = if (savedProfileJson != null) {
            try {
                gson.fromJson(savedProfileJson, UserProfile::class.java).copy(email = cleanEmail)
            } catch (e: Exception) {
                UserProfile(name = updatedAccount.displayName, email = cleanEmail)
            }
        } else {
            UserProfile(name = updatedAccount.displayName, email = cleanEmail)
        }

        userRepo.saveProfile(profile)
        saveProfileForEmail(cleanEmail, profile)

        return profile
    }

    override suspend fun switchAccount(email: String): UserProfile = signInWithEmail(email, null)

    override suspend fun signOut() {
        val activeEmail = _currentEmail.value
        if (activeEmail != null) {
            saveProfileForEmail(activeEmail, userRepo.getProfile())
        }

        // On envoie les dernières modifications avant de fermer la session.
        if (backend.isConfigured && backend.hasSession()) {
            syncRepo.pushToCloud()
        }
        backend.signOut()

        _currentEmail.value = null
        prefs.edit().remove(KEY_ACTIVE_EMAIL).apply()

        // Les données de ce compte ne doivent pas rester pour le compte suivant.
        syncRepo.clearLocalData()
        userRepo.saveProfile(UserProfile(name = "Invité", email = null))
    }

    override suspend fun deleteAccount(email: String) {
        val cleanEmail = email.trim().lowercase()
        val currentAccounts = _savedAccounts.value.filterNot { it.email.equals(cleanEmail, ignoreCase = true) }
        _savedAccounts.value = currentAccounts
        prefs.edit()
            .putString(KEY_SAVED_ACCOUNTS, gson.toJson(currentAccounts))
            .remove("profile_$cleanEmail")
            .remove(passwordKey(cleanEmail))
            .apply()

        if (_currentEmail.value.equals(cleanEmail, ignoreCase = true)) {
            signOut()
        }
    }

    private fun saveProfileForEmail(email: String, profile: UserProfile) {
        prefs.edit().putString("profile_${email.lowercase()}", gson.toJson(profile)).apply()
    }

    // ── Validation et messages ────────────────────────────────────────────────

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Saisis ton adresse email."
        !email.contains("@") || !email.substringAfter("@").contains(".") ->
            "Cette adresse email n'est pas valide."
        else -> null
    }

    private fun validatePassword(password: String): String? =
        if (password.length < MIN_PASSWORD_LENGTH)
            "Le mot de passe doit contenir au moins $MIN_PASSWORD_LENGTH caractères."
        else null

    private fun isNetworkError(error: Throwable?): Boolean {
        var cause = error
        while (cause != null) {
            if (cause is IOException) return true
            val name = cause::class.simpleName.orEmpty()
            if (name.contains("Timeout", true) ||
                name.contains("UnknownHost", true) ||
                name.contains("Connect", true)
            ) return true
            cause = cause.cause
        }
        return false
    }

    private fun translate(error: Throwable?): String {
        if (isNetworkError(error)) {
            return "Pas de connexion internet. Vérifie ton réseau et réessaie."
        }
        val raw = error?.message.orEmpty()
        return when {
            raw.contains("Invalid login", true) ||
                raw.contains("invalid_credentials", true) -> "Email ou mot de passe incorrect."
            raw.contains("already registered", true) ||
                raw.contains("user_already_exists", true) ->
                "Un compte existe déjà avec cet email. Connecte-toi."
            raw.contains("Email not confirmed", true) ->
                "Cet email n'est pas confirmé. Désactive « Confirm email » dans Supabase, ou clique sur le lien reçu."
            raw.contains("Password should be", true) ->
                "Le mot de passe doit contenir au moins $MIN_PASSWORD_LENGTH caractères."
            raw.isBlank() -> "Connexion impossible. Réessaie."
            else -> "Connexion impossible : $raw"
        }
    }

    // ── Empreinte locale du mot de passe (mode hors ligne) ────────────────────

    private suspend fun storePasswordHash(email: String, password: String) =
        withContext(Dispatchers.Default) {
            prefs.edit().putString(passwordKey(email), hashPassword(password)).apply()
        }

    private suspend fun verifyLocalPassword(email: String, password: String): Boolean =
        withContext(Dispatchers.Default) {
            val stored = prefs.getString(passwordKey(email), null) ?: return@withContext false
            val parts = stored.split(":")
            if (parts.size != 3 || parts[0] != PBKDF2_PREFIX) return@withContext false
            try {
                val salt = Base64.decode(parts[1], Base64.NO_WRAP)
                val expected = Base64.decode(parts[2], Base64.NO_WRAP)
                MessageDigest.isEqual(pbkdf2(password, salt), expected)
            } catch (e: Exception) {
                false
            }
        }

    private fun hashPassword(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(password, salt)
        return "$PBKDF2_PREFIX:${Base64.encodeToString(salt, Base64.NO_WRAP)}:" +
            Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun passwordKey(email: String) = "pwd_${email.trim().lowercase()}"

    companion object {
        private const val KEY_ACTIVE_EMAIL = "active_email"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts_list"
        private const val PBKDF2_PREFIX = "pbkdf2"
        private const val SALT_BYTES = 16
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
        const val MIN_PASSWORD_LENGTH = 8
    }
}
