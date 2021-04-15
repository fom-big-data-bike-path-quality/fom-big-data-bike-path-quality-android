package de.florianschwanz.bikepathquality.data.storage.log_entry

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class LogEntryRepository(private val logEntryDao: LogEntryDao) {

    val logEntries: Flow<List<LogEntry>> = logEntryDao.getAll()

    @WorkerThread
    suspend fun insert(logEntry: LogEntry) {
        logEntryDao.insert(logEntry)
    }

    @WorkerThread
    suspend fun deleteAll() {
        logEntryDao.deleteAll()
    }
}