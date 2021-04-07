package de.florianschwanz.bikepathquality

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.florianschwanz.bikepathquality.fragments.ActivityTransitionViewModel
import de.florianschwanz.bikepathquality.storage.LogEntry
import de.florianschwanz.bikepathquality.storage.LogEntryViewModel
import de.florianschwanz.bikepathquality.storage.LogEntryViewModelFactory

/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var activityTransitionViewModel: ActivityTransitionViewModel
    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((this.application as BikePathQualityApplication).logEntryRepository)
    }

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        navView.setupWithNavController(navController)

        activityTransitionViewModel =
            ViewModelProvider(this).get(ActivityTransitionViewModel::class.java)
        activityTransitionViewModel.data.observeForever {
            log(toTransitionType(it.transitionType) + " " + toActivityString(it.activityType))
        }

        log("Initialize app")
    }

    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }


    companion object {

        /**
         * Converts activity to a string
         */
        private fun toActivityString(activity: Int) = when (activity) {
            DetectedActivity.STILL -> "standing still"
            DetectedActivity.WALKING -> "walking"
            DetectedActivity.RUNNING -> "running"
            DetectedActivity.ON_BICYCLE -> "cycling"
            DetectedActivity.IN_VEHICLE -> "being in a vehicle"
            else -> "unknown activity"
        }

        /**
         * Converts transition type to string
         */
        private fun toTransitionType(transitionType: Int) =
            when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "Start"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "Stop"
                else -> "???"
            }
    }
}
