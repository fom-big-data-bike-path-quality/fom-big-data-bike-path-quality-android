package de.florianschwanz.bikepathquality.storage

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class LogEntryRepository(private val logEntryDao: LogEntryDao) {

    val logEntries: Flow<List<LogEntry>> = logEntryDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(logEntry: LogEntry) {
        logEntryDao.insert(logEntry)
    }
}