package de.florianschwanz.bikepathquality.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class ActivityTransitionDto(val activityType: Int, val transitionType: Int)

class ActivityTransitionViewModel : ViewModel() {

    val data = MutableLiveData<ActivityTransitionDto>()
}