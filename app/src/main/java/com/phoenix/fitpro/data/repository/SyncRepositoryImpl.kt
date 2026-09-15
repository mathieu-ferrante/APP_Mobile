package com.phoenix.fitpro.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.phoenix.fitpro.data.local.PhoenixDatabase
import com.phoenix.fitpro.data.local.dao.SyncDao
import com.phoenix.fitpro.data.local.entity.AchievementEntity
import com.phoenix.fitpro.data.local.entity.SportEntity
import com.phoenix.fitpro.data.local.entity.UserProfileEntity
import com.phoenix.fitpro.data.remote.sync.*
import com.phoenix.fitpro.domain.model.AchievementDefinitions
import com.phoenix.fitpro.domain.model.DefaultSports
import com.phoenix.fitpro.domain.repository.SyncRepository
import com.phoenix.fitpro.domain.repository.SyncState
import com.phoenix.fitpro.domain.repository.SyncStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Synchronisation par snapshot complet : les données locales font autorité au push,
 * les données distantes font autorité au pull. Room reste la source d'affichage,
 * donc l'application continue de fonctionner hors ligne.
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: PhoenixDatabase,
    private val syncDao: SyncDao,
    private val backend: SupabaseBackend
) : SyncRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val coachPrefs =
        context.getSharedPreferences("ashes_coach_program_prefs", Context.MODE_PRIVATE)

    private val _status = MutableStateFlow(
        SyncStatus(state = if (backend.isConfigured) SyncState.IDLE else SyncState.DISABLED)
    )
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()

    override val isEnabled: Boolean get() = backend.isConfigured

    init {
        // La session Supabase est rechargée depuis le disque de façon asynchrone :
        // on l'attend une fois au démarrage pour éviter les « aucune session active ».
        if (backend.isConfigured) {
            scope.launch { runCatching { backend.awaitSessionRestore() } }
        }
    }

    // ── Pull ──────────────────────────────────────────────────────────────────

    override suspend fun pullFromCloud(): Result<Unit> {
        if (!backend.isConfigured) return Result.success(Unit)

        runCatching { backend.awaitSessionRestore() }
        _status.value = _status.value.copy(state = SyncState.SYNCING, message = null)

        return backend.pull()
            .mapCatching { snapshot ->
                if (snapshot == null) {
                    // Aucun snapshot distant : premier appareil de ce compte,
                    // on envoie ce que contient la base locale.
                    pushToCloud().getOrThrow()
                } else {
                    applySnapshot(snapshot)
                }
            }
            .onSuccess {
                _status.value = SyncStatus(SyncState.SUCCESS, System.currentTimeMillis())
            }
            .onFailure { error ->
                _status.value = _status.value.copy(
                    state = SyncState.ERROR,
                    message = error.message ?: "Synchronisation impossible."
                )
            }
    }

    private suspend fun applySnapshot(snapshot: SyncSnapshot) {
        if (snapshot.schemaVersion > SyncSnapshot.SCHEMA_VERSION) {
            error("Ces données ont été enregistrées par une version plus récente de Phoenix. Mets l'application à jour.")
        }

        db.withTransaction {
            wipeAllTables()

            syncDao.insertProfiles(snapshot.profiles.map { it.toEntity() })
            syncDao.insertWeightEntries(snapshot.weightEntries.map { it.toEntity() })
            syncDao.insertAchievements(snapshot.achievements.map { it.toEntity() })
            syncDao.insertSports(snapshot.sports.map { it.toEntity() })
            syncDao.insertSessions(snapshot.sessions.map { it.toEntity() })
            syncDao.insertExerciseSets(snapshot.exerciseSets.map { it.toEntity() })
            syncDao.insertMeals(snapshot.meals.map { it.toEntity() })
            syncDao.insertFoodItems(snapshot.foodItems.map { it.toEntity() })

            // Le seed initial de Room ne se rejoue jamais : on le réinstalle ici
            // si le snapshot distant ne contenait pas ces données de base.
            if (snapshot.sports.isEmpty()) seedDefaultSports()
            if (snapshot.achievements.isEmpty()) seedDefaultAchievements()
            if (snapshot.profiles.isEmpty()) syncDao.insertProfiles(listOf(UserProfileEntity()))
        }

        val editor = coachPrefs.edit()
        snapshot.coachProgramJson?.let { editor.putString(KEY_TRAINING_PROGRAM, it) }
        snapshot.coachProfileJson?.let { editor.putString(KEY_COACH_PROFILE, it) }
        editor.apply()
    }

    // ── Push ──────────────────────────────────────────────────────────────────

    override suspend fun pushToCloud(): Result<Unit> {
        if (!backend.isConfigured) return Result.success(Unit)

        _status.value = _status.value.copy(state = SyncState.SYNCING, message = null)

        return backend.push(buildSnapshot())
            .onSuccess {
                _status.value = SyncStatus(SyncState.SUCCESS, System.currentTimeMillis())
            }
            .onFailure { error ->
                _status.value = _status.value.copy(
                    state = SyncState.ERROR,
                    message = error.message ?: "Envoi impossible."
                )
            }
    }

    override fun pushInBackground() {
        if (!backend.isConfigured) return
        scope.launch {
            runCatching { backend.awaitSessionRestore() }
            if (backend.hasSession()) pushToCloud()
        }
    }

    private suspend fun buildSnapshot() = SyncSnapshot(
        profiles = syncDao.allProfiles().map { it.toDto() },
        weightEntries = syncDao.allWeightEntries().map { it.toDto() },
        sports = syncDao.allSports().map { it.toDto() },
        sessions = syncDao.allSessions().map { it.toDto() },
        exerciseSets = syncDao.allExerciseSets().map { it.toDto() },
        meals = syncDao.allMeals().map { it.toDto() },
        foodItems = syncDao.allFoodItems().map { it.toDto() },
        achievements = syncDao.allAchievements().map { it.toDto() },
        coachProgramJson = coachPrefs.getString(KEY_TRAINING_PROGRAM, null),
        coachProfileJson = coachPrefs.getString(KEY_COACH_PROFILE, null)
    )

    // ── Remise à zéro locale ──────────────────────────────────────────────────

    override suspend fun clearLocalData() {
        db.withTransaction {
            wipeAllTables()
            seedDefaultSports()
            seedDefaultAchievements()
            syncDao.insertProfiles(listOf(UserProfileEntity()))
        }
        coachPrefs.edit()
            .remove(KEY_TRAINING_PROGRAM)
            .remove(KEY_COACH_PROFILE)
            .apply()
    }

    /** Ordre imposé par les clés étrangères : enfants avant parents. */
    private suspend fun wipeAllTables() {
        syncDao.clearFoodItems()
        syncDao.clearMeals()
        syncDao.clearExerciseSets()
        syncDao.clearSessions()
        syncDao.clearSports()
        syncDao.clearWeightEntries()
        syncDao.clearAchievements()
        syncDao.clearProfiles()
    }

    private suspend fun seedDefaultSports() {
        syncDao.insertSports(
            DefaultSports.list.map { sport ->
                SportEntity(
                    id = sport.id,
                    name = sport.name,
                    category = sport.category.name,
                    iconName = sport.iconName,
                    colorArgb = sport.colorArgb,
                    isDefault = sport.isDefault
                )
            }
        )
    }

    private suspend fun seedDefaultAchievements() {
        syncDao.insertAchievements(
            AchievementDefinitions.all.map { AchievementEntity(id = it.id, isUnlocked = false) }
        )
    }

    companion object {
        private const val KEY_TRAINING_PROGRAM = "key_training_program"
        private const val KEY_COACH_PROFILE = "key_coach_profile"
    }
}
