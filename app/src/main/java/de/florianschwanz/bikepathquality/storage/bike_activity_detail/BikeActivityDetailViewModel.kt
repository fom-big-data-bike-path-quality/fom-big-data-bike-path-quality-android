package de.florianschwanz.bikepathquality.storage.bike_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BikeActivityDetailViewModel(private val repository: BikeActivityDetailRepository) :
    ViewModel() {

    val allBikeActivityDetails: LiveData<List<BikeActivityDetail>> =
        repository.bikeActivities.asLiveData()

    fun insert(bikeActivityDetail: BikeActivityDetail) = viewModelScope.launch {
        repository.insert(bikeActivityDetail)
    }

    fun update(bikeActivityDetail: BikeActivityDetail) = viewModelScope.launch {
        repository.update(bikeActivityDetail)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
