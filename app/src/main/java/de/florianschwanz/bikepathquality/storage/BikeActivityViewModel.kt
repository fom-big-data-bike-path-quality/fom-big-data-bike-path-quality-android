package de.florianschwanz.bikepathquality.storage

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class BikeActivityViewModel(private val repository: BikeActivityRepository) : ViewModel() {

    val allBikeActivities: LiveData<List<BikeActivity>> = repository.bikeActivities.asLiveData()

    fun insert(bikeActivity: BikeActivity) = viewModelScope.launch {
        repository.insert(bikeActivity)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
