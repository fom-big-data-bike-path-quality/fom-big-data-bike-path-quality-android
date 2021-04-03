package de.florianschwanz.bikepathquality.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.florianschwanz.bikepathquality.livedata.AccelerometerLiveData
import de.florianschwanz.bikepathquality.model.AccelerometerModel

class AccelerometerCardViewModel(application: Application) : AndroidViewModel(application) {

    val data = AccelerometerLiveData(application)
}