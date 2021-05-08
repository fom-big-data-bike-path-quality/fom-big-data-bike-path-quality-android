package de.florianschwanz.bikepathquality.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityWithSamples

class BikeActivityDetailsViewModel(application: Application) : AndroidViewModel(application) {

    val bikeActivityWithDetails = MutableLiveData<BikeActivityWithSamples>()
}