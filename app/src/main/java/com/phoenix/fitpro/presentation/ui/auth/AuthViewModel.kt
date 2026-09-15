package com.phoenix.fitpro.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.fitpro.domain.repository.AuthRepository
import com.phoenix.fitpro.domain.repository.AuthResult
import com.phoenix.fitpro.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isCreatingAccount: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val syncRepo: SyncRepository
) : ViewModel() {

    /** Email du compte connecté, ou null. Sert de garde d'accès à l'application. */
    val currentEmail: StateFlow<String?> = authRepo.currentEmailFlow

    /** true si la synchronisation cloud est configurée dans ce build. */
    val isCloudEnabled: Boolean get() = syncRepo.isEnabled

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setEmail(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun setPassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun setConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun setDisplayName(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }

    fun toggleMode() = _uiState.update {
        it.copy(
            isCreatingAccount = !it.isCreatingAccount,
            password = "",
            confirmPassword = "",
            errorMessage = null,
            infoMessage = null
        )
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        if (state.isCreatingAccount && state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Les deux mots de passe ne correspondent pas.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

            val result = if (state.isCreatingAccount) {
                authRepo.signUp(state.email, state.password, state.displayName.ifBlank { null })
            } else {
                authRepo.signIn(state.email, state.password)
            }

            when (result) {
                // La navigation est déclenchée par currentEmail ; on nettoie les champs.
                is AuthResult.Success -> _uiState.value = AuthUiState()
                is AuthResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            when (val result = authRepo.sendPasswordReset(email)) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        infoMessage = "Un lien de réinitialisation a été envoyé à $email."
                    )
                }
                is AuthResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authRepo.signOut() }
    }
}
