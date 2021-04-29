package de.florianschwanz.bikepathquality.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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


/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivitiesRepository)
    }

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var broadcastReceiver: BroadcastReceiver

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
        val clNotification: ConstraintLayout = findViewById(R.id.clNotification)

        navView.setupWithNavController(navController)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.trackingServiceEnabled.observe(this, { trackingServiceEnabled ->
            if (trackingServiceEnabled) {
                clNotification.maxHeight = 100
            } else {
                clNotification.maxHeight = 0
            }
        })

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                viewModel.trackingServiceEnabled.value = intent.extras?.getBoolean(EXTRA_ENABLED)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            broadcastReceiver, IntentFilter(TrackingForegroundService.TAG)
        )
    }

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
        }
    }

    companion object {
        const val REQUEST_BIKE_ACTIVITY_DETAILS = 1
    }
}
