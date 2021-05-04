package de.florianschwanz.bikepathquality.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.florianschwanz.bikepathquality.data.livedata.AccelerometerLiveData
import de.florianschwanz.bikepathquality.data.livedata.ActivityTransitionLiveData
import de.florianschwanz.bikepathquality.data.livedata.LocationLiveData
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityWithDetails

class BikeActivityDetailsViewModel(application: Application) : AndroidViewModel(application) {

    val bikeActivityWithDetails = MutableLiveData<BikeActivityWithDetails>()
}