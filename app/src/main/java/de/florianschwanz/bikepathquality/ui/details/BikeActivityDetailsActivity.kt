package de.florianschwanz.bikepathquality.ui.details

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_BIKE_ACTIVITY_UID = "bike_aktivity_uid"

class BikeActivityDetailsActivity : AppCompatActivity() {

    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivitiesRepository)
    }

    private var sdfShort: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    private var sdf: SimpleDateFormat = SimpleDateFormat("MMM dd HH:mm:ss", Locale.ENGLISH)

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bike_activity_details)

        val tvTitle: TextView = findViewById(R.id.tvTitle)
        val tvStartTime: TextView = findViewById(R.id.tvStartTime)
        val tvDelimiter: TextView = findViewById(R.id.tvDelimiter)
        val tvStopTime: TextView = findViewById(R.id.tvStopTime)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!intent.hasExtra(EXTRA_BIKE_ACTIVITY_UID)) {
            finish()
        }

        val bikeActivityUid = intent.getStringExtra(EXTRA_BIKE_ACTIVITY_UID)

        bikeActivityViewModel.singleBikeActivityWithDetails(bikeActivityUid!!).observe(this, {
            tvStartTime.text = sdf.format(Date.from(it.bikeActivity.startTime))

            if (it.bikeActivity.endTime != null) {
                tvStopTime.text = sdfShort.format(Date.from(it.bikeActivity.endTime))
                tvDelimiter.visibility = View.VISIBLE
                tvStopTime.visibility = View.VISIBLE
            } else {
                tvDelimiter.visibility = View.INVISIBLE
                tvStopTime.visibility = View.INVISIBLE
            }
        })
    }
}