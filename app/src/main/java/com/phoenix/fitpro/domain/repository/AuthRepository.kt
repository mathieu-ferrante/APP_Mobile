package com.phoenix.fitpro.domain.repository

import com.phoenix.fitpro.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class AuthAccount(
    val email: String,
    val displayName: String = "",
    val lastLoginEpochMs: Long = System.currentTimeMillis()
)

interface AuthRepository {
    val currentEmailFlow: StateFlow<String?>
    fun getCurrentEmail(): String?
    fun observeSavedAccounts(): Flow<List<AuthAccount>>
    suspend fun signInWithEmail(email: String, displayName: String? = null): UserProfile
    suspend fun switchAccount(email: String): UserProfile
    suspend fun signOut()
    suspend fun deleteAccount(email: String)
}
