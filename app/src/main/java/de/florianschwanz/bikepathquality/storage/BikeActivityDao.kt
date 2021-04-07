package de.florianschwanz.bikepathquality.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BikeActivityDao {

    @Query("SELECT * FROM bikeactivity")
    fun getAll(): Flow<List<BikeActivity>>

    @Insert
    suspend fun insert(bikeActivity: BikeActivity)

    @Delete
    suspend fun delete(bikeActivity: BikeActivity)

    @Query("DELETE FROM bikeactivity")
    suspend fun deleteAll()
}