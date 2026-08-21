package com.phoenix.fitpro.data.local.dao

import androidx.room.*
import com.phoenix.fitpro.data.local.entity.SportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SportDao {
    @Query("SELECT * FROM sports ORDER BY isDefault DESC, name ASC")
    fun getAllSports(): Flow<List<SportEntity>>

    @Query("SELECT * FROM sports WHERE id = :id")
    suspend fun getSportById(id: Long): SportEntity?

    @Query("SELECT * FROM sports WHERE category = :category ORDER BY name ASC")
    fun getSportsByCategory(category: String): Flow<List<SportEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSport(sport: SportEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSports(sports: List<SportEntity>)

    @Update
    suspend fun updateSport(sport: SportEntity)

    @Delete
    suspend fun deleteSport(sport: SportEntity)

    @Query("SELECT COUNT(*) FROM sports")
    suspend fun getSportCount(): Int
}
