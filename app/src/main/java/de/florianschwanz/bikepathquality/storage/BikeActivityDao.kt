package de.florianschwanz.bikepathquality.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeActivityDao {

    @Query("SELECT * FROM bikeactivity")
    fun getAll(): Flow<List<BikeActivity>>

    @Query("SELECT * FROM bikeactivity WHERE end_time IS NULL ORDER BY start_time DESC")
    fun getAllActive(): Flow<List<BikeActivity>>

    @Insert
    suspend fun insert(bikeActivity: BikeActivity)

    @Update
    suspend fun update(bikeActivity: BikeActivity)

    @Delete
    suspend fun delete(bikeActivity: BikeActivity)

    @Query("DELETE FROM bikeactivity")
    suspend fun deleteAll()
}