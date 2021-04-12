package de.florianschwanz.bikepathquality.storage.log_entry

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    @Query("SELECT * FROM logentry")
    fun getAll(): Flow<List<LogEntry>>

    @Insert
    suspend fun insert(logEntry: LogEntry)

    @Insert
    suspend fun insertAll(vararg logEntries: LogEntry)

    @Delete
    suspend fun delete(logEntry: LogEntry)

    @Query("DELETE FROM logentry")
    suspend fun deleteAll()
}