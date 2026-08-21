package com.phoenix.fitpro.data.repository

import com.phoenix.fitpro.data.local.dao.AchievementDao
import com.phoenix.fitpro.data.local.dao.UserProfileDao
import com.phoenix.fitpro.data.local.entity.AchievementEntity
import com.phoenix.fitpro.data.local.entity.UserProfileEntity
import com.phoenix.fitpro.data.local.entity.WeightEntryEntity
import com.phoenix.fitpro.domain.model.*
import com.phoenix.fitpro.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val profileDao: UserProfileDao,
    private val achievementDao: AchievementDao
) : UserRepository {

    override fun observeProfile(): Flow<UserProfile?> {
        return profileDao.observeProfile().map { it?.toDomain() }
    }

    override suspend fun getProfile(): UserProfile {
        return profileDao.getProfile()?.toDomain() ?: UserProfile()
    }

    override suspend fun saveProfile(profile: UserProfile) {
        profileDao.upsertProfile(profile.toEntity())
    }

    override suspend fun addWeightEntry(date: LocalDate, weightKg: Float) {
        profileDao.insertWeightEntry(WeightEntryEntity(date = date.toEpochDay(), weightKg = weightKg))
    }

    override fun getRecentWeightEntries(): Flow<List<WeightEntry>> {
        return profileDao.getRecentWeightEntries().map { entities ->
            entities.map { WeightEntry(id = it.id, date = LocalDate.ofEpochDay(it.date), weightKg = it.weightKg) }
        }
    }

    override fun observeAchievements(): Flow<List<Achievement>> {
        return achievementDao.observeAll().map { entities ->
            AchievementDefinitions.all.map { definition ->
                val record = entities.find { it.id == definition.id }
                definition.copy(
                    isUnlocked = record?.isUnlocked ?: false,
                    unlockedAt = record?.unlockedAt?.let { LocalDate.ofEpochDay(it) }
                )
            }
        }
    }

    /** Returns the ID of the newly unlocked achievement, or null if already unlocked */
    override suspend fun unlockAchievement(id: String): Achievement? {
        val rowsAffected = achievementDao.unlock(id, LocalDate.now().toEpochDay())
        if (rowsAffected == 0) return null   // already unlocked or not found

        val definition = AchievementDefinitions.findById(id) ?: return null
        // Add XP reward
        val profile = profileDao.getProfile() ?: return null
        val newXp = profile.xp + definition.xpReward
        val newLevel = 1 + newXp / 500
        profileDao.upsertProfile(profile.copy(xp = newXp, level = newLevel))
        return definition.copy(isUnlocked = true, unlockedAt = LocalDate.now())
    }

    override suspend fun updateStreak(activeToday: Boolean): Int {
        val profile = profileDao.getProfile() ?: return 0
        val today = LocalDate.now()
        val lastActive = profile.lastActivityDate?.let { LocalDate.ofEpochDay(it) }

        val newStreak = when {
            lastActive == null && !activeToday -> profile.currentStreak
            lastActive == today -> profile.currentStreak // already counted
            lastActive == today.minusDays(1) && activeToday -> profile.currentStreak + 1
            activeToday -> 1   // streak broken, restart
            else -> profile.currentStreak
        }

        val newXp = if (lastActive != today && activeToday) {
            profile.xp + UserProfile.XP_PER_STREAK_DAY
        } else profile.xp
        val newLevel = 1 + newXp / 500

        val updatedProfile = profile.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(profile.longestStreak, newStreak),
            lastActivityDate = if (activeToday) today.toEpochDay() else profile.lastActivityDate,
            xp = newXp,
            level = newLevel
        )
        profileDao.upsertProfile(updatedProfile)
        return newStreak
    }

    override suspend fun addXp(amount: Int) {
        val profile = profileDao.getProfile() ?: return
        val newXp = profile.xp + amount
        val newLevel = 1 + newXp / 500
        profileDao.upsertProfile(profile.copy(xp = newXp, level = newLevel))
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun UserProfileEntity.toDomain() = UserProfile(
        id = id, name = name, photoUri = photoUri,
        weightKg = weightKg, heightCm = heightCm,
        objectiveType = ObjectiveType.valueOf(objectiveType),
        level = level, xp = xp,
        currentStreak = currentStreak, longestStreak = longestStreak,
        lastActivityDate = lastActivityDate?.let { LocalDate.ofEpochDay(it) },
        preferredLanguage = preferredLanguage,
        notificationsEnabled = notificationsEnabled,
        firebaseUid = firebaseUid
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        id = id, name = name, photoUri = photoUri,
        weightKg = weightKg, heightCm = heightCm,
        objectiveType = objectiveType.name,
        level = level, xp = xp,
        currentStreak = currentStreak, longestStreak = longestStreak,
        lastActivityDate = lastActivityDate?.toEpochDay(),
        preferredLanguage = preferredLanguage,
        notificationsEnabled = notificationsEnabled,
        firebaseUid = firebaseUid
    )
}
