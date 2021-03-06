package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeActivityDao {

    @Query("SELECT * FROM bikeactivity")
    fun getAll(): Flow<List<BikeActivity>>

    @Transaction
    @Query("SELECT * FROM bikeactivity")
    fun getAllWithSamples(): Flow<List<BikeActivityWithSamples>>

    @Query("SELECT * FROM bikeactivity WHERE end_time IS NULL ORDER BY start_time DESC")
    fun getAllActive(): Flow<List<BikeActivity>>

    @Query("SELECT * FROM bikeactivity WHERE uid=:uid")
    fun getSingleWithSamples(uid: String): Flow<BikeActivityWithSamples>

    @Query("SELECT * FROM bikeactivity WHERE end_time IS NULL ORDER BY start_time DESC")
    fun getActive(): Flow<BikeActivity>

    @Query("SELECT * FROM bikeactivity WHERE end_time IS NULL ORDER BY start_time DESC")
    fun getActiveWithSamples(): Flow<BikeActivityWithSamples>

    @Insert
    suspend fun insert(bikeActivity: BikeActivity)

    @Update
    suspend fun update(bikeActivity: BikeActivity)

    @Delete
    suspend fun delete(bikeActivity: BikeActivity)

    @Query("DELETE FROM bikeactivity")
    suspend fun deleteAll()
}