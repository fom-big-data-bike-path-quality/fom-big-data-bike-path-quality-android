package de.florianschwanz.bikepathquality.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.services.TrackingForegroundService
import de.florianschwanz.bikepathquality.ui.details.BikeActivityDetailsActivity.Companion.RESULT_BIKE_ACTIVITY_UID

/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivitiesRepository)
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

        // Start tracking foreground service
        val trackingForegroundServiceIntent = Intent(this, TrackingForegroundService::class.java)
        ContextCompat.startForegroundService(this, trackingForegroundServiceIntent)
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
