package de.florianschwanz.bikepathquality.data.storage.bike_activity_sample

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeActivitySampleDao {

    @Query("SELECT * FROM bikeactivitysample")
    fun getAll(): Flow<List<BikeActivitySample>>

    @Query("SELECT * FROM bikeactivitysample")
    fun getAllWithMeasurements(): Flow<List<BikeActivitySampleWithMeasurements>>

    @Query("SELECT * FROM bikeactivitysample where bike_activity_uid=:bikeActivityUid")
    fun getWithMeasurements(bikeActivityUid: String): Flow<List<BikeActivitySampleWithMeasurements>>

    @Query("SELECT * FROM bikeactivitysample WHERE uid=:uid")
    fun getSingleWithMeasurements(uid: String): Flow<BikeActivitySampleWithMeasurements>

    @Insert
    suspend fun insert(bikeActivitySample: BikeActivitySample)

    @Update
    suspend fun update(bikeActivitySample: BikeActivitySample)

    @Delete
    suspend fun delete(bikeActivitySample: BikeActivitySample)

    @Query("DELETE FROM bikeactivitysample")
    suspend fun deleteAll()
}
