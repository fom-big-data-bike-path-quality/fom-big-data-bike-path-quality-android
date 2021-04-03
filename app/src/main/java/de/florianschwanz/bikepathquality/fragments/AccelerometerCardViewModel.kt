package de.florianschwanz.bikepathquality.fragments

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class AccelerometerDto(val x: Float, val y: Float, val z: Float)

class AccelerometerCardViewModel : ViewModel() {

    val data = MutableLiveData<AccelerometerDto>()
}