package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BikeActivityViewModelFactory(private val repository: BikeActivityRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BikeActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BikeActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}