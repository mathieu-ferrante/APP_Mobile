package com.phoenix.fitpro.presentation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.fitpro.presentation.theme.*

/**
 * Écran de connexion / création de compte.
 * Le compte est hébergé sur Supabase quand la synchronisation est configurée,
 * avec repli local pour permettre l'ouverture de l'application hors ligne.
 */
@Composable
fun AuthScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Phoenix", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            if (state.isCreatingAccount) "Crée ton compte pour commencer"
            else "Connecte-toi pour accéder à Phoenix",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::setEmail,
            label = { Text("Adresse email") },
            singleLine = true,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = authFieldColors()
        )

        if (state.isCreatingAccount) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.displayName,
                onValueChange = viewModel::setDisplayName,
                label = { Text("Prénom (optionnel)") },
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::setPassword,
            label = { Text("Mot de passe (8 caractères minimum)") },
            singleLine = true,
            enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (state.isCreatingAccount) ImeAction.Next else ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = authFieldColors()
        )

        if (state.isCreatingAccount) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::setConfirmPassword,
                label = { Text("Confirme le mot de passe") },
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = viewModel::submit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(if (state.isCreatingAccount) "Créer mon compte" else "Se connecter")
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = viewModel::toggleMode,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isCreatingAccount) "J'ai déjà un compte" else "Créer un compte")
        }

        if (!state.isCreatingAccount && viewModel.isCloudEnabled) {
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = viewModel::sendPasswordReset,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mot de passe oublié ?")
            }
        }

        state.infoMessage?.let { info ->
            Spacer(Modifier.height(16.dp))
            Text(
                info,
                style = MaterialTheme.typography.bodyMedium,
                color = NeonGreen,
                textAlign = TextAlign.Center
            )
        }

        state.errorMessage?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(
                error,
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorRed,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            if (viewModel.isCloudEnabled)
                "Tes données sont liées à ton compte : retrouve-les sur n'importe quel appareil."
            else
                "Ton compte et tes données restent sur cet appareil.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = ElectricBlue,
    unfocusedBorderColor = DarkOutline,
    focusedLabelColor = ElectricBlueLight,
    unfocusedLabelColor = TextSecondary,
    cursorColor = ElectricBlue
)
