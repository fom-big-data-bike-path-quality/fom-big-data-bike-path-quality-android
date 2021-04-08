package de.florianschwanz.bikepathquality.storage

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class BikeActivityViewModel(private val repository: BikeActivityRepository) : ViewModel() {

    val allBikeActivities: LiveData<List<BikeActivity>> = repository.bikeActivities.asLiveData()

    val allActiveBikeActivities: LiveData<List<BikeActivity>> = repository.activeActivities.asLiveData()

    fun insert(bikeActivity: BikeActivity) = viewModelScope.launch {
        repository.insert(bikeActivity)
    }

    fun update(bikeActivity: BikeActivity) = viewModelScope.launch {
        repository.update(bikeActivity)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
