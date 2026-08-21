package com.phoenix.fitpro.data.local.dao

import androidx.room.*
import com.phoenix.fitpro.data.local.entity.UserProfileEntity
import com.phoenix.fitpro.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    // Weight entries
    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 90")
    fun getRecentWeightEntries(): Flow<List<WeightEntryEntity>>

    @Insert
    suspend fun insertWeightEntry(entry: WeightEntryEntity)

    @Delete
    suspend fun deleteWeightEntry(entry: WeightEntryEntity)
}
