package de.florianschwanz.bikepathquality.ui.details

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
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

        val toolbar: Toolbar = findViewById(R.id.toolbar)
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

        bikeActivityViewModel.singleBikeActivityWithDetails(bikeActivityUid!!)
            .observe(this, { bikeActivityWithDetails ->

                bikeActivityWithDetails?.let {
                    toolbar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.action_delete -> {
                                val resultIntent = Intent()
                                resultIntent.putExtra(
                                    RESULT_BIKE_ACTIVITY_UID,
                                    bikeActivityWithDetails.bikeActivity.uid.toString()
                                )
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                            else -> {
                            }
                        }

                        false
                    }


                    val mapStyle =
                        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
                            Configuration.UI_MODE_NIGHT_NO -> Style.LIGHT
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.LIGHT
                            else -> Style.LIGHT
                        }

                    val mapRouteCoordinates: List<Point> =
                        bikeActivityWithDetails.bikeActivityDetails
                            .filter { it.lon != 0.0 || it.lat != 0.0 }
                            .map {
                                Point.fromLngLat(
                                    it.lon,
                                    it.lat
                                )
                            }

                    mapView?.getMapAsync { mapboxMap ->
                        mapboxMap.setStyle(mapStyle) {
                            it.addSource(
                                GeoJsonSource(
                                    "line-source",
                                    FeatureCollection.fromFeatures(
                                        arrayOf<Feature>(
                                            Feature.fromGeometry(
                                                LineString.fromLngLats(mapRouteCoordinates)
                                            )
                                        )
                                    )
                                )
                            )
                            it.addLayer(
                                LineLayer("linelayer", "line-source").withProperties(
                                    PropertyFactory.lineDasharray(arrayOf(0.01f, 2f)),
                                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                    PropertyFactory.lineWidth(5f),
                                    PropertyFactory.lineColor(Color.parseColor(getThemeColorInHex(R.attr.colorPrimary)))
                                )
                            )

                            if (bikeActivityWithDetails.bikeActivityDetails.filter { it.lon != 0.0 || it.lat != 0.0 }.size > 1) {
                                val latLngBounds = LatLngBounds.Builder()
                                bikeActivityWithDetails.bikeActivityDetails
                                    .filter { it.lon != 0.0 || it.lat != 0.0 }
                                    .forEach { bikeActivityDetail ->
                                        latLngBounds.include(
                                            LatLng(
                                                bikeActivityDetail.lat,
                                                bikeActivityDetail.lon
                                            )
                                        )
                                    }

                                mapboxMap.easeCamera(
                                    CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 250),
                                    1
                                )
                            }

                            mapboxMap.uiSettings.isZoomGesturesEnabled = false
                            mapboxMap.uiSettings.isScrollGesturesEnabled = false
                            mapboxMap.uiSettings.isRotateGesturesEnabled = false
                        }
                    }

                    tvStartTime.text =
                        sdf.format(Date.from(bikeActivityWithDetails.bikeActivity.startTime))
                    tvStartTime.visibility = View.VISIBLE

                    if (bikeActivityWithDetails.bikeActivity.status != BikeActivityStatus.UPLOADED) {
                        ivCheck.visibility = View.INVISIBLE
                        tvUploaded.visibility = View.INVISIBLE
                        spSurfaceType.isEnabled = true
                        spSmoothnessType.isEnabled = true

                        if (bikeActivityWithDetails.bikeActivity.surfaceType != null && bikeActivityWithDetails.bikeActivity.smoothnessType != null) {
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

                    if (bikeActivityWithDetails.bikeActivity.endTime != null) {
                        tvStopTime.text =
                            sdfShort.format(Date.from(bikeActivityWithDetails.bikeActivity.endTime))
                        tvDelimiter.visibility = View.VISIBLE
                        tvStopTime.visibility = View.VISIBLE

                        val diff =
                            bikeActivityWithDetails.bikeActivity.endTime.toEpochMilli() - bikeActivityWithDetails.bikeActivity.startTime.toEpochMilli()
                        val duration = (diff / 1000 / 60).toInt()
                        tvDuration.text =
                            resources.getQuantityString(R.plurals.duration, duration, duration)
                        tvDetails.text = resources.getQuantityString(
                            R.plurals.details,
                            bikeActivityWithDetails.bikeActivityDetails.size,
                            bikeActivityWithDetails.bikeActivityDetails.size
                        )
                    } else {
                        tvDelimiter.visibility = View.INVISIBLE
                        tvStopTime.visibility = View.INVISIBLE
                    }

                    adapter.data = bikeActivityWithDetails.bikeActivityDetails

                    spSurfaceType.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {
                                val selectedItem =
                                    if (position > 0) parent.getItemAtPosition(position)
                                        .toString() else null

                                // Update bike activity surface type
                                bikeActivityViewModel.update(
                                    bikeActivityWithDetails.bikeActivity.copy(surfaceType = selectedItem)
                                )
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                    spSmoothnessType.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {
                                val selectedItem =
                                    if (position > 0) parent.getItemAtPosition(position)
                                        .toString() else null

                                // Update bike activity smoothness type
                                bikeActivityViewModel.update(
                                    bikeActivityWithDetails.bikeActivity.copy(smoothnessType = selectedItem)
                                )
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }

                    fab.setOnClickListener {
                        if (bikeActivityWithDetails.bikeActivity.status != BikeActivityStatus.UPLOADED) {
                            val serviceResultReceiver =
                                FirestoreServiceResultReceiver(Handler(Looper.getMainLooper()))
                            serviceResultReceiver.receiver = this
                            FirestoreService.enqueueWork(
                                this,
                                bikeActivityWithDetails,
                                serviceResultReceiver
                            )
                        } else {
                            Toast.makeText(
                                applicationContext,
                                R.string.action_upload_already_done,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_bike_activity_details_activity, menu)
        return super.onCreateOptionsMenu(menu)
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

    //
    // Helpers
    //

    /**
     * Logs message
     */
    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }

    /**
     * Retrieves theme color
     */
    private fun getThemeColorInHex(@AttrRes attribute: Int): String {
        val outValue = TypedValue()
        theme.resolveAttribute(attribute, outValue, true)

        return String.format("#%06X", 0xFFFFFF and outValue.data)
    }

    companion object {
        const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"
        const val EXTRA_TRACKING_SERVICE_ENABLED = "extra.TRACKING_SERVICE_ENABLED"

        const val RESULT_BIKE_ACTIVITY_UID = "result.BIKE_ACTIVITY_UID"
    }
}