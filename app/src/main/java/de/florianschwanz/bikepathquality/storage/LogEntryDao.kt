package de.florianschwanz.bikepathquality.storage

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
    fun insert(logEntry: LogEntry)

    @Insert
    fun insertAll(vararg logEntries: LogEntry)

    @Delete
    fun delete(logEntry: LogEntry)

    @Query("DELETE FROM logentry")
    fun deleteAll()
}