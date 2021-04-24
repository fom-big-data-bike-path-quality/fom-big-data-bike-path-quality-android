package de.florianschwanz.bikepathquality.ui.details

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
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

    private var mapView: MapView? = null

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_bike_activity_details)
        setTitle(R.string.empty)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)

        val ivCheck: ImageView = findViewById(R.id.ivCheck)
        val tvUploaded: TextView = findViewById(R.id.tvUploaded)
        val tvStartTime: TextView = findViewById(R.id.tvStartTime)
        val tvDelimiter: TextView = findViewById(R.id.tvDelimiter)
        val tvStopTime: TextView = findViewById(R.id.tvStopTime)
        val spSurfaceType: Spinner = findViewById(R.id.spSurfaceType)
        val spSmoothnessType: Spinner = findViewById(R.id.spSmoothnessType)
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

        ArrayAdapter.createFromResource(
            this, R.array.surface_array, android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spSurfaceType.adapter = it
        }

        ArrayAdapter.createFromResource(
            this, R.array.smoothness_array, android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spSmoothnessType.adapter = it
        }

        bikeActivityViewModel.singleBikeActivityWithDetails(bikeActivityUid!!).observe(this, {

            val mapStyle =
                when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> Style.DARK
                    Configuration.UI_MODE_NIGHT_NO -> Style.LIGHT
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.LIGHT
                    else -> Style.LIGHT
                }

            mapView?.getMapAsync { mapboxMap ->
                mapboxMap.setStyle(mapStyle) {
                }
            }

            tvStartTime.text = sdf.format(Date.from(it.bikeActivity.startTime))
            tvStartTime.visibility = View.VISIBLE

            if (it.bikeActivity.status != BikeActivityStatus.UPLOADED) {
                ivCheck.visibility = View.INVISIBLE
                tvUploaded.visibility = View.INVISIBLE
                spSurfaceType.isEnabled = true
                spSmoothnessType.isEnabled = true

                if (it.bikeActivity.surfaceType != null && it.bikeActivity.smoothnessType != null) {
                    fab.visibility = View.VISIBLE
                } else {
                    fab.visibility = View.INVISIBLE
                }
            } else {
                ivCheck.visibility = View.VISIBLE
                tvUploaded.visibility = View.VISIBLE
                spSurfaceType.isEnabled = false
                spSmoothnessType.isEnabled = false
                fab.visibility = View.INVISIBLE
            }

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

            spSurfaceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem =
                        if (position > 0) parent.getItemAtPosition(position).toString() else null

                    // Update bike activity surface type
                    bikeActivityViewModel.update(
                        it.bikeActivity.copy(surfaceType = selectedItem)
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            spSmoothnessType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem =
                        if (position > 0) parent.getItemAtPosition(position).toString() else null

                    // Update bike activity smoothness type
                    bikeActivityViewModel.update(
                        it.bikeActivity.copy(smoothnessType = selectedItem)
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            fab.setOnClickListener { _ ->
                if (it.bikeActivity.status != BikeActivityStatus.UPLOADED) {
                    val serviceResultReceiver =
                        FirestoreServiceResultReceiver(Handler(Looper.getMainLooper()))
                    serviceResultReceiver.receiver = this
                    FirestoreService.enqueueWork(this, it, serviceResultReceiver)
                } else {
                    Toast.makeText(
                        applicationContext,
                        R.string.action_upload_already_done,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    /**
     * Handles on-resume lifecycle phase
     */
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    /**
     * Handles on-start lifecycle phase
     */
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    /**
     * Handles on-stop lifecycle phase
     */
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    /**
     * Handles on-pause lifecycle phase
     */
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    /**
     * Handles on-low-memory lifecycle phase
     */
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    /**
     * Handles on-destroy lifecycle phase
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    /**
     * Handles results of Firestore service
     */
    override fun onReceiveFirestoreServiceResult(resultCode: Int, resultData: Bundle?) {
        when (resultCode) {
            FirestoreService.RESULT_SUCCESS -> {

                Toast.makeText(
                    applicationContext,
                    R.string.action_upload_successful,
                    Toast.LENGTH_LONG
                ).show()

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

                Toast.makeText(
                    applicationContext,
                    R.string.action_upload_failed,
                    Toast.LENGTH_LONG
                ).show()

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