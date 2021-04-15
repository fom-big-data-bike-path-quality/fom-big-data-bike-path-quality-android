package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BikeActivityDetailViewModelFactory(private val repository: BikeActivityDetailRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BikeActivityDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BikeActivityDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}