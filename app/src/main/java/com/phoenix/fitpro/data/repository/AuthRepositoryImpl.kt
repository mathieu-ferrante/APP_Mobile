package com.phoenix.fitpro.data.repository

import android.content.Context
import com.phoenix.fitpro.domain.model.CoachUserProfile
import com.phoenix.fitpro.domain.model.TrainingProgram
import com.phoenix.fitpro.domain.model.UserProfile
import com.phoenix.fitpro.domain.repository.AuthAccount
import com.phoenix.fitpro.domain.repository.AuthRepository
import com.phoenix.fitpro.domain.repository.ProgramRepository
import com.phoenix.fitpro.domain.repository.UserRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val userRepo: UserRepository,
    private val programRepo: ProgramRepository
) : AuthRepository {

    private val prefs = context.getSharedPreferences("ashes_auth_accounts_prefs", Context.MODE_PRIVATE)

    private val _currentEmail = MutableStateFlow<String?>(null)
    override val currentEmailFlow: StateFlow<String?> = _currentEmail.asStateFlow()

    private val _savedAccounts = MutableStateFlow<List<AuthAccount>>(emptyList())

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        val activeEmail = prefs.getString(KEY_ACTIVE_EMAIL, null)
        _currentEmail.value = activeEmail

        val accountsJson = prefs.getString(KEY_SAVED_ACCOUNTS, null)
        if (accountsJson != null) {
            try {
                val type = object : TypeToken<List<AuthAccount>>() {}.type
                val list: List<AuthAccount> = gson.fromJson(accountsJson, type)
                _savedAccounts.value = list
            } catch (e: Exception) {
                _savedAccounts.value = emptyList()
            }
        }
    }

    override fun getCurrentEmail(): String? = _currentEmail.value

    override fun observeSavedAccounts(): Flow<List<AuthAccount>> = _savedAccounts.asStateFlow()

    override suspend fun signInWithEmail(email: String, displayName: String?): UserProfile {
        val cleanEmail = email.trim().lowercase()
        val cleanName = displayName?.trim().orEmpty()

        // 1. Update saved accounts list
        val currentAccounts = _savedAccounts.value.toMutableList()
        val existingIndex = currentAccounts.indexOfFirst { it.email.equals(cleanEmail, ignoreCase = true) }
        val updatedAccount = AuthAccount(
            email = cleanEmail,
            displayName = if (cleanName.isNotBlank()) cleanName else (currentAccounts.getOrNull(existingIndex)?.displayName ?: cleanEmail.substringBefore("@")),
            lastLoginEpochMs = System.currentTimeMillis()
        )

        if (existingIndex >= 0) {
            currentAccounts[existingIndex] = updatedAccount
        } else {
            currentAccounts.add(0, updatedAccount)
        }
        _savedAccounts.value = currentAccounts
        prefs.edit().putString(KEY_SAVED_ACCOUNTS, gson.toJson(currentAccounts)).apply()

        // 2. Set as active email
        _currentEmail.value = cleanEmail
        prefs.edit().putString(KEY_ACTIVE_EMAIL, cleanEmail).apply()

        // 3. Load or initialize profile for this email
        val profileKey = "profile_$cleanEmail"
        val savedProfileJson = prefs.getString(profileKey, null)
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

    override suspend fun switchAccount(email: String): UserProfile {
        return signInWithEmail(email, null)
    }

    override suspend fun signOut() {
        // Save current profile state before signing out
        val activeEmail = _currentEmail.value
        if (activeEmail != null) {
            val currentProfile = userRepo.getProfile()
            saveProfileForEmail(activeEmail, currentProfile)
        }

        _currentEmail.value = null
        prefs.edit().remove(KEY_ACTIVE_EMAIL).apply()

        // Revert to default guest profile
        userRepo.saveProfile(UserProfile(name = "Invité", email = null))
    }

    override suspend fun deleteAccount(email: String) {
        val cleanEmail = email.trim().lowercase()
        val currentAccounts = _savedAccounts.value.filterNot { it.email.equals(cleanEmail, ignoreCase = true) }
        _savedAccounts.value = currentAccounts
        prefs.edit()
            .putString(KEY_SAVED_ACCOUNTS, gson.toJson(currentAccounts))
            .remove("profile_$cleanEmail")
            .apply()

        if (_currentEmail.value.equals(cleanEmail, ignoreCase = true)) {
            signOut()
        }
    }

    private fun saveProfileForEmail(email: String, profile: UserProfile) {
        prefs.edit().putString("profile_${email.lowercase()}", gson.toJson(profile)).apply()
    }

    companion object {
        private const val KEY_ACTIVE_EMAIL = "active_email"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts_list"
    }
}
