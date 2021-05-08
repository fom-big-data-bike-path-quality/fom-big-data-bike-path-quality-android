package de.florianschwanz.bikepathquality.data.storage.bike_activity_sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BikeActivitySampleViewModelFactory(private val repository: BikeActivitySampleRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BikeActivitySampleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BikeActivitySampleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}