package de.florianschwanz.bikepathquality.data.storage.user_data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityWithSamples
import kotlinx.coroutines.launch

class UserDataViewModel(private val repository: UserDataRepository) : ViewModel() {

    fun exists() = repository.exists().asLiveData()

    fun singleUserData(): LiveData<UserData> = repository.singleUserData().asLiveData()

    fun insert(userData: UserData) = viewModelScope.launch {
        repository.insert(userData)
    }

    fun update(userData: UserData) = viewModelScope.launch {
        repository.update(userData)
    }

    fun delete(userData: UserData) = viewModelScope.launch {
        repository.delete(userData)
    }
}
