package de.florianschwanz.bikepathquality.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityWithSamples
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData

class BikeActivityDetailsViewModel(application: Application) : AndroidViewModel(application) {

    val bikeActivityWithSamples = MutableLiveData<BikeActivityWithSamples>()
    val bikeActivitySamplesWithMeasurements =
        MutableLiveData<List<BikeActivitySampleWithMeasurements>>()
    val bikeActivitySampleInFocus = MutableLiveData<BikeActivitySampleWithMeasurements>()
    val userData = MutableLiveData<UserData>()
}