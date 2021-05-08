package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BikeActivityMeasurementViewModelFactory(private val repository: BikeActivityMeasurementRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BikeActivityMeasurementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BikeActivityMeasurementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}