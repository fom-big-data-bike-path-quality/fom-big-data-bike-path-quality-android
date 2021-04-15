package de.florianschwanz.bikepathquality.data.storage.log_entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LogEntryViewModelFactory(private val repository: LogEntryRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}