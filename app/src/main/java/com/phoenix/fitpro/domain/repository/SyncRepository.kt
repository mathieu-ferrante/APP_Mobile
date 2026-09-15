package com.phoenix.fitpro.domain.repository

import kotlinx.coroutines.flow.StateFlow

enum class SyncState { IDLE, SYNCING, SUCCESS, ERROR, DISABLED }

data class SyncStatus(
    val state: SyncState = SyncState.IDLE,
    val lastSyncEpochMs: Long? = null,
    val message: String? = null
)

interface SyncRepository {
    val status: StateFlow<SyncStatus>

    /** true si les identifiants Supabase sont présents dans le build. */
    val isEnabled: Boolean

    /** Récupère le snapshot distant et remplace les données locales. */
    suspend fun pullFromCloud(): Result<Unit>

    /** Envoie les données locales vers le cloud. */
    suspend fun pushToCloud(): Result<Unit>

    /** Push silencieux, à appeler quand l'application passe en arrière-plan. */
    fun pushInBackground()

    /**
     * Efface toutes les données locales et réinstalle les valeurs par défaut.
     * Appelé à la déconnexion pour qu'un autre compte ne récupère pas les données
     * du précédent utilisateur de cet appareil.
     */
    suspend fun clearLocalData()
}
