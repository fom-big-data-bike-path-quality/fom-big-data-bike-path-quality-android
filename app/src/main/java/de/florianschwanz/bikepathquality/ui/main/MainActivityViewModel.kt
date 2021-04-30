package de.florianschwanz.bikepathquality.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.florianschwanz.bikepathquality.data.livedata.AccelerometerLiveData
import de.florianschwanz.bikepathquality.data.livedata.ActivityTransitionLiveData
import de.florianschwanz.bikepathquality.data.livedata.LocationLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    val activityTransitionLiveData = ActivityTransitionLiveData(application)
    val accelerometerLiveData = AccelerometerLiveData(application)
    val locationLiveData = LocationLiveData(application)

    val trackingServiceStatus = MutableLiveData<String>()
}