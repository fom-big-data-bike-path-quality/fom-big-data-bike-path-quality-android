package de.florianschwanz.bikepathquality

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.florianschwanz.bikepathquality.livedata.AccelerometerLiveData
import de.florianschwanz.bikepathquality.livedata.ActivityTransitionLiveData
import de.florianschwanz.bikepathquality.livedata.LocationLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    val activityTransitionLiveData = ActivityTransitionLiveData(application)
    val accelerometerLiveData = AccelerometerLiveData(application)
    val locationLiveData = LocationLiveData(application)
}