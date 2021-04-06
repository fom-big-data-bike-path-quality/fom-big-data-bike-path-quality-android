package de.florianschwanz.bikepathquality.fragments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.florianschwanz.bikepathquality.livedata.ActivityTransitionLiveData

class ActivityTransitionViewModel(application: Application) : AndroidViewModel(application) {

    val data = ActivityTransitionLiveData(application)
}