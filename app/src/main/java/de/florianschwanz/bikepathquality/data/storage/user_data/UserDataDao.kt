package de.florianschwanz.bikepathquality.data.storage.user_data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {

    @Query("SELECT EXISTS(SELECT * FROM userdata)")
    fun exists(): Flow<Boolean>

    @Query("SELECT * FROM userdata")
    fun getSingle(): Flow<UserData>

    @Insert
    suspend fun insert(userData: UserData)

    @Update
    suspend fun update(userData: UserData)

    @Delete
    suspend fun delete(userData: UserData)
}