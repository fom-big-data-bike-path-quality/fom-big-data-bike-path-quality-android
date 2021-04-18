package de.florianschwanz.bikepathquality.ui.details

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.ui.details.adapters.BikeActivityDetailListAdapter
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
        setTitle(R.string.empty)

        val tvStartTime: TextView = findViewById(R.id.tvStartTime)
        val tvDelimiter: TextView = findViewById(R.id.tvDelimiter)
        val tvStopTime: TextView = findViewById(R.id.tvStopTime)
        val tvDuration: TextView = findViewById(R.id.tvDuration)
        val tvDetails: TextView = findViewById(R.id.tvDetails)
        val recyclerView = findViewById<RecyclerView>(R.id.rvActivityDetails)

        val adapter = BikeActivityDetailListAdapter()

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

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

                val diff =
                    it.bikeActivity.endTime.toEpochMilli() - it.bikeActivity.startTime.toEpochMilli()
                val duration = (diff / 1000 / 60).toInt()
                tvDuration.text =
                    resources.getQuantityString(R.plurals.duration, duration, duration)
                tvDetails.text = resources.getQuantityString(
                    R.plurals.details,
                    it.bikeActivityDetails.size,
                    it.bikeActivityDetails.size
                )
            } else {
                tvDelimiter.visibility = View.INVISIBLE
                tvStopTime.visibility = View.INVISIBLE
            }

            adapter.data = it.bikeActivityDetails
        })
    }
}