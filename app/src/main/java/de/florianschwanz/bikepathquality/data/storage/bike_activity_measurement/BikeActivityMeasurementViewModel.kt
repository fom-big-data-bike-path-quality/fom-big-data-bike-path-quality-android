package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BikeActivityMeasurementViewModel(private val repository: BikeActivityMeasurementRepository) :
    ViewModel() {

    val allBikeActivityDetails: LiveData<List<BikeActivityMeasurement>> =
        repository.bikeActivities.asLiveData()

    fun insert(bikeActivityMeasurement: BikeActivityMeasurement) = viewModelScope.launch {
        repository.insert(bikeActivityMeasurement)
    }

    fun update(bikeActivityMeasurement: BikeActivityMeasurement) = viewModelScope.launch {
        repository.update(bikeActivityMeasurement)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
