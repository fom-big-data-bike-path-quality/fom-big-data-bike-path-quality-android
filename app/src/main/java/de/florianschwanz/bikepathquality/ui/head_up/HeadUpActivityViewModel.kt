package de.florianschwanz.bikepathquality.ui.head_up

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.florianschwanz.bikepathquality.data.livedata.AccelerometerLiveData

class HeadUpActivityViewModel(application: Application) : AndroidViewModel(application) {

    val accelerometerLiveData = AccelerometerLiveData(application)
}