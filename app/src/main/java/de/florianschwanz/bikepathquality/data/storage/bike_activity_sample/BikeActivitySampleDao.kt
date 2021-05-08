package de.florianschwanz.bikepathquality.data.storage.bike_activity_sample

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeActivitySampleDao {

    @Query("SELECT * FROM bikeactivitysample")
    fun getAll(): Flow<List<BikeActivitySample>>

    @Insert
    suspend fun insert(bikeActivitySample: BikeActivitySample)

    @Update
    suspend fun update(bikeActivitySample: BikeActivitySample)

    @Delete
    suspend fun delete(bikeActivitySample: BikeActivitySample)

    @Query("DELETE FROM bikeactivitysample")
    suspend fun deleteAll()
}
