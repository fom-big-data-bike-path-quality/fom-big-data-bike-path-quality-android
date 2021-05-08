package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeActivityMeasurementDao {

    @Query("SELECT * FROM bikeactivitymeasurement")
    fun getAll(): Flow<List<BikeActivityMeasurement>>

    @Insert
    suspend fun insert(bikeActivityMeasurement: BikeActivityMeasurement)

    @Update
    suspend fun update(bikeActivityMeasurement: BikeActivityMeasurement)

    @Delete
    suspend fun delete(bikeActivityMeasurement: BikeActivityMeasurement)

    @Query("DELETE FROM bikeactivitymeasurement")
    suspend fun deleteAll()
}
