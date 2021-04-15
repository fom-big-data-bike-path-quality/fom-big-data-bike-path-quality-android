package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeActivityDetailDao {

    @Query("SELECT * FROM bikeactivitydetail")
    fun getAll(): Flow<List<BikeActivityDetail>>

    @Insert
    suspend fun insert(bikeActivityDetail: BikeActivityDetail)

    @Update
    suspend fun update(bikeActivityDetail: BikeActivityDetail)

    @Delete
    suspend fun delete(bikeActivityDetail: BikeActivityDetail)

    @Query("DELETE FROM bikeactivitydetail")
    suspend fun deleteAll()
}