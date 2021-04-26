package de.florianschwanz.bikepathquality.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.*
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.ui.details.BikeActivityDetailsActivity.Companion.RESULT_BIKE_ACTIVITY_UID
import java.time.Instant

/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((this.application as BikePathQualityApplication).logEntryRepository)
    }
    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivitiesRepository)
    }
    private val bikeActivityDetailViewModel: BikeActivityDetailViewModel by viewModels {
        BikeActivityDetailViewModelFactory((this.application as BikePathQualityApplication).bikeActivityDetailsRepository)
    }

    private val targetActivityType = DetectedActivity.ON_BICYCLE

    // Currently performed biking activity
    private var activeActivity: BikeActivity? = null
    private var activeActivityType = -1
    private var activeTransitionType = -1

    private var currentLon = 0.0
    private var currentLat = 0.0
    private var currentAccelerometerX = 0.0f
    private var currentAccelerometerY = 0.0f
    private var currentAccelerometerZ = 0.0f

    private val activityDetailInterval = 10_000L
    private val activityDetailHandler = Handler(Looper.getMainLooper())
    private var activityDetailTracker: Runnable = object : Runnable {
        override fun run() {
            try {
                trackActivityDetail()
            } finally {
                activityDetailHandler.postDelayed(this, activityDetailInterval)
            }
        }
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

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        handleActiveBikeActivity()
        handleActivityTransitions()
        handleActivityDetailTracking()
    }

    /**
     * Handles activity result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_BIKE_ACTIVITY_DETAILS && resultCode == RESULT_OK) {
            data?.getStringExtra(RESULT_BIKE_ACTIVITY_UID)?.let { bikeActivityUid ->
                bikeActivityViewModel.singleBikeActivityWithDetails(bikeActivityUid)
                    .observe(this) { bikeActivityWithDetails ->
                        bikeActivityWithDetails?.let {
                            bikeActivityViewModel.delete(it.bikeActivity)
                            Toast.makeText(
                                applicationContext,
                                R.string.action_activity_deleted,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    //
    // Helpers
    //

    /**
     * Retrieves most recent unfinished bike activity from the database
     */
    private fun handleActiveBikeActivity() {
        bikeActivityViewModel.activeBikeActivity.observe(this, {
            activeActivity = it
            log("new active activity ${activeActivity?.uid.toString()}")

            if (activeActivity != null) {
                activityDetailTracker.run()
            } else {
                activityDetailHandler.removeCallbacks(activityDetailTracker)
            }
        })
    }

    /**
     * Listens to activity transitions related to bicycle and if necessary
     * <li>creates a new bike activity
     * <li>finished an active bike activity
     */
    private fun handleActivityTransitions() {

        viewModel.activityTransitionLiveData.observe(this, {

            log(toTransitionType(it.transitionType) + " " + toActivityString(it.activityType))

            if (it.activityType == targetActivityType) {
                when (it.transitionType) {
                    ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                        // Create new bike activity if there no ongoing one
                        if (activeActivity == null) {
                            bikeActivityViewModel.insert(BikeActivity())
                        }
                    }
                    ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                        // Finish active bike activity if there is one
                        activeActivity?.let { bikeActivity ->
                            bikeActivityViewModel.update(bikeActivity.copy(endTime = Instant.now()))
                        }
                    }
                }
            }

            activeActivityType = it.activityType
            activeTransitionType = it.transitionType
        })
    }

    private fun handleActivityDetailTracking() {
        viewModel.accelerometerLiveData.observe(this, {
            currentAccelerometerX = it.x
            currentAccelerometerY = it.y
            currentAccelerometerZ = it.z
        })
        viewModel.locationLiveData.observe(this, {
            currentLon = it.lon
            currentLat = it.lat
        })
    }

    /**
     * Logs message
     */
    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }

    /**
     * Tracks an activity detail and persists it
     */
    private fun trackActivityDetail() = activeActivity?.let {

        log("new tracking for ${it.uid}")

        bikeActivityDetailViewModel.insert(
            BikeActivityDetail(
                activityUid = it.uid,
                lon = currentLon,
                lat = currentLat,
                accelerometerX = currentAccelerometerX,
                accelerometerY = currentAccelerometerY,
                accelerometerZ = currentAccelerometerZ
            )
        )
    }

    companion object {

        val REQUEST_BIKE_ACTIVITY_DETAILS = 1


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
