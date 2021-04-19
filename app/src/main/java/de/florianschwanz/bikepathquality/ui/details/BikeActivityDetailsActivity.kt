package de.florianschwanz.bikepathquality.ui.details

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityStatus
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.services.FirestoreService
import de.florianschwanz.bikepathquality.services.FirestoreServiceResultReceiver
import de.florianschwanz.bikepathquality.ui.details.adapters.BikeActivityDetailListAdapter
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"

class BikeActivityDetailsActivity : AppCompatActivity(), FirestoreServiceResultReceiver.Receiver {

    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((this.application as BikePathQualityApplication).logEntryRepository)
    }
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

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tvStartTime: TextView = findViewById(R.id.tvStartTime)
        val tvDelimiter: TextView = findViewById(R.id.tvDelimiter)
        val tvStopTime: TextView = findViewById(R.id.tvStopTime)
        val tvDuration: TextView = findViewById(R.id.tvDuration)
        val tvDetails: TextView = findViewById(R.id.tvDetails)
        val recyclerView = findViewById<RecyclerView>(R.id.rvActivityDetails)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        val adapter = BikeActivityDetailListAdapter()

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

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

            fab.setOnClickListener { view ->
                val serviceResultReceiver =
                    FirestoreServiceResultReceiver(Handler(Looper.getMainLooper()))
                serviceResultReceiver.receiver = this
                FirestoreService.enqueueWork(this, it, serviceResultReceiver)
            }
        })
    }

    /**
     * Handles results of Firestore service
     */
    override fun onReceiveFirestoreServiceResult(resultCode: Int, resultData: Bundle?) {
        when (resultCode) {
            FirestoreService.RESULT_SUCCESS -> {

                resultData
                    ?.getString(FirestoreService.EXTRA_BIKE_ACTIVITY_UID)
                    ?.let {
                        bikeActivityViewModel
                            .singleBikeActivityWithDetails(it)
                            .observe(this, { bikeActivityWithDetails ->

                                // Update bike activity status
                                bikeActivityViewModel.update(
                                    bikeActivityWithDetails.bikeActivity.copy(
                                        status = BikeActivityStatus.UPLOADED
                                    )
                                )
                            })
                    }
            }
            FirestoreService.RESULT_FAILURE -> {
                resultData?.getString(FirestoreService.EXTRA_ERROR_MESSAGE)?.let { log(it) }
            }
        }
    }

    /**
     * Logs message
     */
    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }
}