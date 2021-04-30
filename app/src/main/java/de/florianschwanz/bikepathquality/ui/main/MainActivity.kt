package de.florianschwanz.bikepathquality.ui.main

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.services.TrackingForegroundService
import de.florianschwanz.bikepathquality.services.TrackingForegroundService.Companion.EXTRA_ENABLED
import de.florianschwanz.bikepathquality.ui.details.BikeActivityDetailsActivity.Companion.RESULT_BIKE_ACTIVITY_UID
import de.florianschwanz.bikepathquality.ui.rationale.ActivityTransitionPermissionRationaleActivity
import de.florianschwanz.bikepathquality.ui.rationale.LocationPermissionRationaleActivity


/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivitiesRepository)
    }

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var broadcastReceiver: BroadcastReceiver

    private lateinit var clNotification: ConstraintLayout

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
        clNotification = findViewById(R.id.clNotification)

        navView.setupWithNavController(navController)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                viewModel.trackingServiceEnabled.value = intent.extras?.getBoolean(EXTRA_ENABLED)
            }
        }

        requestActivityTransitionPermission()
        requestLocationPermission()
    }

    /**
     * Handles on-resume lifecycle phase
     */
    override fun onResume() {
        super.onResume()
        registerReceiver(
            broadcastReceiver, IntentFilter(TrackingForegroundService.TAG)
        )

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.trackingServiceEnabled.observe(this, { trackingServiceEnabled ->
            updateNotificationBanner(clNotification, trackingServiceEnabled)
        })

        updateNotificationBanner(clNotification, TrackingForegroundService.status)
    }

    /**
     * Handles on-pause lifecycle phase
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    /**
     * Handles activity result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        @Suppress("DEPRECATION")
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
        } else if (requestCode == REQUEST_ACTIVITY_TRANSITION_PERMISSION && resultCode == Activity.RESULT_CANCELED) {
            finishAffinity();
        } else if (requestCode == REQUEST_LOCATION_PERMISSION && resultCode == Activity.RESULT_CANCELED) {
            finishAffinity();
        }
    }

    //
    // Helpers
    //

    /**
     * Requests activity transition permission
     */
    private fun requestActivityTransitionPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !isGranted(Manifest.permission.ACTIVITY_RECOGNITION)
        ) {
            val startIntent =
                Intent(this, ActivityTransitionPermissionRationaleActivity::class.java)

            @Suppress("DEPRECATION")
            startActivityForResult(startIntent, REQUEST_ACTIVITY_TRANSITION_PERMISSION)
        }
    }

    /**
     * Requests location permission
     */
    private fun requestLocationPermission() {
        if (!isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            val startIntent =
                Intent(this, LocationPermissionRationaleActivity::class.java)

            @Suppress("DEPRECATION")
            startActivityForResult(startIntent, REQUEST_LOCATION_PERMISSION)
        }
    }

    /**
     * Determines if a given permission is granted
     */
    private fun isGranted(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Updates notification banner based on tracking service state
     */
    private fun updateNotificationBanner(
        clNotification: ConstraintLayout,
        trackingServiceEnabled: Boolean
    ) {
        clNotification.maxHeight = if (trackingServiceEnabled) 100 else 0
    }

    companion object {
        const val REQUEST_ACTIVITY_TRANSITION_PERMISSION = 1
        const val REQUEST_LOCATION_PERMISSION = 2
        const val REQUEST_BIKE_ACTIVITY_DETAILS = 3
    }
}
