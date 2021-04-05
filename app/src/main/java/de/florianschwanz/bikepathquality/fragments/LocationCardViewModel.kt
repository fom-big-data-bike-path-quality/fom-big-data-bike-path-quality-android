package de.florianschwanz.bikepathquality.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.florianschwanz.bikepathquality.livedata.LocationLiveData

class LocationCardViewModel(application: Application) : AndroidViewModel(application) {

    val data = LocationLiveData(application)
}