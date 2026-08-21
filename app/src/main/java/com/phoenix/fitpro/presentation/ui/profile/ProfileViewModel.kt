package com.phoenix.fitpro.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.domain.repository.AuthAccount
import com.phoenix.fitpro.domain.repository.AuthRepository
import com.phoenix.fitpro.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile = UserProfile(),
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val weightEntries: List<Pair<LocalDate, Float>> = emptyList(),
    val isEditing: Boolean = false,
    val saveSuccess: Boolean = false,
    val savedAccounts: List<AuthAccount> = emptyList(),
    val showAuthDialog: Boolean = false,
    val authInputEmail: String = "",
    val authInputName: String = "",
    val authError: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userRepo.observeProfile(),
                userRepo.observeAchievements(),
                userRepo.getRecentWeightEntries(),
                authRepo.observeSavedAccounts()
            ) { profile, achievements, weightEntries, accounts ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        profile = profile ?: UserProfile(),
                        achievements = achievements,
                        unlockedCount = achievements.count { it.isUnlocked },
                        weightEntries = weightEntries.map { it.date to it.weightKg },
                        savedAccounts = accounts
                    )
                }
            }.collect()
        }
    }

    fun saveProfile(
        name: String,
        weightKg: Float?,
        heightCm: Float?,
        objective: ObjectiveType,
        language: String,
        notificationsEnabled: Boolean
    ) {
        viewModelScope.launch {
            val current = userRepo.getProfile()
            userRepo.saveProfile(
                current.copy(
                    name = name,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    objectiveType = objective,
                    preferredLanguage = language,
                    notificationsEnabled = notificationsEnabled
                )
            )
            if (weightKg != null) {
                userRepo.addWeightEntry(LocalDate.now(), weightKg)
            }
            _uiState.update { it.copy(isEditing = false, saveSuccess = true) }
        }
    }

    fun startEditing() = _uiState.update { it.copy(isEditing = true) }
    fun cancelEditing() = _uiState.update { it.copy(isEditing = false) }
    fun clearSaveSuccess() = _uiState.update { it.copy(saveSuccess = false) }

    // ── Auth Account Actions ──────────────────────────────────────────────────
    fun openAuthDialog() = _uiState.update { it.copy(showAuthDialog = true, authError = null) }
    fun closeAuthDialog() = _uiState.update { it.copy(showAuthDialog = false, authError = null) }
    fun setAuthInputEmail(email: String) = _uiState.update { it.copy(authInputEmail = email, authError = null) }
    fun setAuthInputName(name: String) = _uiState.update { it.copy(authInputName = name) }

    fun signInWithEmail() {
        val email = _uiState.value.authInputEmail.trim()
        val name = _uiState.value.authInputName.trim()

        if (email.isBlank() || !email.contains("@") || !email.contains(".")) {
            _uiState.update { it.copy(authError = "Veuillez saisir une adresse email valide.") }
            return
        }

        viewModelScope.launch {
            authRepo.signInWithEmail(email, name.ifBlank { null })
            _uiState.update {
                it.copy(
                    showAuthDialog = false,
                    authInputEmail = "",
                    authInputName = "",
                    authError = null
                )
            }
        }
    }

    fun switchAccount(email: String) {
        viewModelScope.launch {
            authRepo.switchAccount(email)
            _uiState.update { it.copy(showAuthDialog = false) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepo.signOut()
            _uiState.update { it.copy(showAuthDialog = false) }
        }
    }

    fun deleteAccount(email: String) {
        viewModelScope.launch {
            authRepo.deleteAccount(email)
        }
    }
}
