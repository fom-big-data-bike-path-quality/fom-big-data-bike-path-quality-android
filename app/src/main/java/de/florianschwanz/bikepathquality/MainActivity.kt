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
import de.florianschwanz.bikepathquality.storage.bike_activity.*
import de.florianschwanz.bikepathquality.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.storage.log_entry.LogEntryViewModelFactory
import java.time.Instant

/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var activityTransitionViewModel: ActivityTransitionViewModel
    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((this.application as BikePathQualityApplication).logEntryRepository)
    }
    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivitiesRepository)
    }

    // Currently performed biking activity
    private var activeActivity: BikeActivity? = null
    private var activeActivityType = -1
    private var activeTransitionType = -1

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

        handleActiveBikeActivity()
        handleActivityTransitions()
    }

    /**
     * Retrieves most recent unfinished bike activity from the database
     */
    private fun handleActiveBikeActivity() {
        bikeActivityViewModel.allActiveBikeActivities.observe(this, {
            it.takeIf { it.isNotEmpty() }?.let { bikeActivities ->
                activeActivity = bikeActivities.first()
                log("new active activity ${activeActivity?.uid.toString().substring(0, 17)}...")
            }
        })
    }

    /**
     * Listens to activity transitions related to bicycle and if necessary
     * <li>creates a new bike activity
     * <li>finished an active bike activity
     */
    private fun handleActivityTransitions() {
        activityTransitionViewModel =
            ViewModelProvider(this).get(ActivityTransitionViewModel::class.java)
        activityTransitionViewModel.data.observe(this, {

            if ((it.activityType != activeActivityType || it.transitionType != activeTransitionType) &&
                it.activityType == DetectedActivity.ON_BICYCLE
            ) {

                log(toTransitionType(it.transitionType) + " " + toActivityString(it.activityType))

                when (it.transitionType) {
                    ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                        // Create new bike activity if there no ongoing one
                        if (activeActivity == null) {
                            activeActivity = BikeActivity()
                            bikeActivityViewModel.insert(activeActivity!!)
                        }
                    }
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                        // Finish active bike activity if there is one
                        activeActivity?.let { bikeActivity ->
                            bikeActivityViewModel.update(bikeActivity.copy(endTime = Instant.now()))
                            activeActivity = null
                        }
                    }
                }
            }

            activeActivityType = it.activityType
            activeTransitionType = it.transitionType
        })
    }

    /**
     * Logs message
     */
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
