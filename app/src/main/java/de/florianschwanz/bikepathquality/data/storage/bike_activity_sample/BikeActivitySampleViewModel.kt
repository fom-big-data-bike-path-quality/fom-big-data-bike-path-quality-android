package de.florianschwanz.bikepathquality.data.storage.bike_activity_sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BikeActivitySampleViewModel(private val repository: BikeActivitySampleRepository) :
    ViewModel() {

    val allBikeActivitySamples: LiveData<List<BikeActivitySample>> =
        repository.bikeActivitySamples.asLiveData()

    fun insert(bikeActivitySample: BikeActivitySample) = viewModelScope.launch {
        repository.insert(bikeActivitySample)
    }

    fun update(bikeActivitySample: BikeActivitySample) = viewModelScope.launch {
        repository.update(bikeActivitySample)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
