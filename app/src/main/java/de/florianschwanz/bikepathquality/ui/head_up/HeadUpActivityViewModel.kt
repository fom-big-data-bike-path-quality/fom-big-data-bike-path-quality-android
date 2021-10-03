package de.florianschwanz.bikepathquality.ui.head_up

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.florianschwanz.bikepathquality.data.livedata.AccelerometerLiveData
import de.florianschwanz.bikepathquality.data.livedata.LocationLiveData
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityWithSamples
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData

class HeadUpActivityViewModel(application: Application) : AndroidViewModel(application) {

    val accelerometerLiveData = AccelerometerLiveData(application)
    val locationLiveData = LocationLiveData(application)

    val activeBikeActivityWithSamples = MutableLiveData<BikeActivityWithSamples?>()
    val userData = MutableLiveData<UserData?>()
}