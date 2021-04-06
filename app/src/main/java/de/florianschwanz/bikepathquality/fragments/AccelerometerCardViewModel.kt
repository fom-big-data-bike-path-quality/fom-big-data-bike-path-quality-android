package de.florianschwanz.bikepathquality.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.florianschwanz.bikepathquality.livedata.AccelerometerLiveData

class AccelerometerCardViewModel(application: Application) : AndroidViewModel(application) {

    val data = AccelerometerLiveData(application)
}