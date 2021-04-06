package de.florianschwanz.bikepathquality.storage

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class LogEntryViewModel(private val repository: LogEntryRepository) : ViewModel() {

    val allLogEntries: LiveData<List<LogEntry>> = repository.logEntries.asLiveData()

    fun insert(logEntry: LogEntry) = viewModelScope.launch {
        repository.insert(logEntry)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
