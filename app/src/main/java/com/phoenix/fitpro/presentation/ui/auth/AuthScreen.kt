package com.phoenix.fitpro.presentation.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) continuation.resume(task.result)
        else continuation.resumeWithException(task.exception ?: IllegalStateException("Tâche Firebase échouée"))
    }
}

@Composable
fun AuthScreen() {
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isCreatingAccount by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Phoenix", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text(
            if (isCreatingAccount) "Crée ton compte pour commencer" else "Connecte-toi pour accéder à Phoenix",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; message = null },
            label = { Text("Adresse email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; message = null },
            label = { Text("Mot de passe (8 caractères minimum)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    message = try {
                        if (email.isBlank() || password.length < 8) {
                            "Saisis un email valide et un mot de passe d'au moins 8 caractères."
                        } else if (isCreatingAccount) {
                            val result = auth.createUserWithEmailAndPassword(email.trim(), password).awaitResult()
                            result.user?.sendEmailVerification()?.awaitResult()
                            auth.signOut()
                            "Un email de vérification a été envoyé. Vérifie ta boîte mail avant de te connecter."
                        } else {
                            val user = auth.signInWithEmailAndPassword(email.trim(), password).awaitResult().user
                            if (user?.isEmailVerified == true) null
                            else {
                                user?.sendEmailVerification()?.awaitResult()
                                auth.signOut()
                                "Vérifie ton adresse email avant de te connecter. Un nouveau lien a été envoyé."
                            }
                        }
                    } catch (error: Exception) {
                        "Échec de l'authentification : ${error.message ?: "réessaie"}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) CircularProgressIndicator()
            else Text(if (isCreatingAccount) "Créer mon compte" else "Se connecter")
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { isCreatingAccount = !isCreatingAccount; message = null },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isCreatingAccount) "J'ai déjà un compte" else "Créer un compte")
        }

        if (!isCreatingAccount) {
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        message = try {
                            if (email.isBlank()) "Saisis ton adresse email pour recevoir le lien."
                            else {
                                auth.sendPasswordResetEmail(email.trim()).awaitResult()
                                "Un lien de réinitialisation a été envoyé."
                            }
                        } catch (error: Exception) {
                            "Impossible d'envoyer le lien : ${error.message ?: "réessaie"}"
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mot de passe oublié ?")
            }
        }

        message?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
