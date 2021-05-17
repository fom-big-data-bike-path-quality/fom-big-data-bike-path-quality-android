package de.florianschwanz.bikepathquality.data.storage.user_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UserDataViewModelFactory(private val repository: UserDataRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}