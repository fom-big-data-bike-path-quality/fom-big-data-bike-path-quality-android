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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
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
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.storage.bike_activity.*
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.services.FirestoreService
import de.florianschwanz.bikepathquality.services.FirestoreServiceResultReceiver
import de.florianschwanz.bikepathquality.ui.details.adapters.BikeActivitySampleListAdapter
import de.florianschwanz.bikepathquality.ui.smoothness_type.SmoothnessTypeActivity
import de.florianschwanz.bikepathquality.ui.smoothness_type.SmoothnessTypeActivity.Companion.EXTRA_SMOOTHNESS_TYPE
import de.florianschwanz.bikepathquality.ui.smoothness_type.adapters.SmoothnessTypeListAdapter.SmoothnessTypeViewHolder.Companion.RESULT_SMOOTHNESS_TYPE
import de.florianschwanz.bikepathquality.ui.surface_type.SurfaceTypeActivity
import de.florianschwanz.bikepathquality.ui.surface_type.SurfaceTypeActivity.Companion.EXTRA_SURFACE_TYPE
import de.florianschwanz.bikepathquality.ui.surface_type.adapters.SurfaceTypeListAdapter.SurfaceTypeViewHolder.Companion.RESULT_SURFACE_TYPE
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class BikeActivityDetailsActivity : AppCompatActivity(), FirestoreServiceResultReceiver.Receiver,
    BikeActivitySampleListAdapter.OnItemClickListener {

    private lateinit var viewModel: BikeActivityDetailsViewModel

    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((this.application as BikePathQualityApplication).logEntryRepository)
    }
    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivityRepository)
    }
    private val bikeActivitySampleViewModel: BikeActivitySampleViewModel by viewModels {
        BikeActivitySampleViewModelFactory((this.application as BikePathQualityApplication).bikeActivitySampleRepository)
    }

    private var sdfShort: SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    private var sdf: SimpleDateFormat = SimpleDateFormat("MMM dd HH:mm:ss", Locale.ENGLISH)

    private var mapView: MapView? = null
    private lateinit var mapboxMap: MapboxMap

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(BikeActivityDetailsViewModel::class.java)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        setContentView(R.layout.activity_bike_activity_details)
        setTitle(R.string.empty)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val clDescription: ConstraintLayout = findViewById(R.id.clDescription)
        val ivCheck: ImageView = findViewById(R.id.ivCheck)
        val tvUploaded: TextView = findViewById(R.id.tvUploaded)
        val tvStartTime: TextView = findViewById(R.id.tvStartTime)
        val tvDelimiter: TextView = findViewById(R.id.tvDelimiter)
        val tvStopTime: TextView = findViewById(R.id.tvStopTime)
        val ivStop: ImageView = findViewById(R.id.ivStop)
        val btnSurfaceType: MaterialButton = findViewById(R.id.btnSurfaceType)
        val btnSmoothnessType: MaterialButton = findViewById(R.id.btnSmoothnessType)
        val tvDuration: TextView = findViewById(R.id.tvDuration)
        val tvSamples: TextView = findViewById(R.id.tvSamples)
        val recyclerView = findViewById<RecyclerView>(R.id.rvActivityDetails)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        val adapter = BikeActivitySampleListAdapter(this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (!intent.hasExtra(EXTRA_BIKE_ACTIVITY_UID)) {
            finish()
        }

        val bikeActivityUid = intent.getStringExtra(EXTRA_BIKE_ACTIVITY_UID)

        bikeActivityViewModel.singleBikeActivityWithSamples(bikeActivityUid!!)
            .observe(this, { bikeActivityWithSamples ->

                bikeActivityWithSamples?.let {
                    viewModel.bikeActivityWithDetails.value = bikeActivityWithSamples

                    toolbar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.action_delete -> {
                                val resultIntent = Intent()
                                resultIntent.putExtra(
                                    RESULT_BIKE_ACTIVITY_UID,
                                    bikeActivityWithSamples.bikeActivity.uid.toString()
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
                        bikeActivityWithSamples.bikeActivitySamples
                            .filter { it.lon != 0.0 || it.lat != 0.0 }
                            .map {
                                Point.fromLngLat(
                                    it.lon,
                                    it.lat
                                )
                            }

                    mapView?.getMapAsync {
                        mapboxMap = it


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
                            it.addSource(
                                GeoJsonSource(
                                    "circle-source",
                                    FeatureCollection.fromFeatures(
                                        mapRouteCoordinates.map {
                                            Feature.fromGeometry(it)
                                        }.toTypedArray()
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
                            it.addLayer(
                                CircleLayer("circleLayer", "circle-source").withProperties(
                                    PropertyFactory.circleRadius(7f),
                                    PropertyFactory.circleColor(
                                        Color.parseColor(
                                            getThemeColorInHex(
                                                R.attr.colorPrimaryVariant
                                            )
                                        )
                                    )
                                )
                            )

                            mapboxMap.uiSettings.isRotateGesturesEnabled = false

                            centerMap(mapboxMap, bikeActivityWithSamples.bikeActivitySamples)
                        }

                        clDescription.setOnClickListener {
                            centerMap(
                                mapboxMap,
                                bikeActivityWithSamples.bikeActivitySamples,
                                duration = 1_000
                            )
                        }
                    }

                    tvStartTime.text =
                        sdf.format(Date.from(bikeActivityWithSamples.bikeActivity.startTime))
                    tvStartTime.visibility = View.VISIBLE

                    if (bikeActivityWithSamples.bikeActivity.uploadStatus != BikeActivityStatus.UPLOADED) {
                        ivCheck.visibility = View.INVISIBLE
                        tvUploaded.visibility = View.INVISIBLE
                        btnSurfaceType.isEnabled = true
                        btnSmoothnessType.isEnabled = true

                        if (bikeActivityWithSamples.bikeActivity.surfaceType != null && bikeActivityWithSamples.bikeActivity.smoothnessType != null) {
                            fab.visibility = View.VISIBLE
                        } else {
                            fab.visibility = View.INVISIBLE
                        }
                    } else {
                        ivCheck.visibility = View.VISIBLE
                        tvUploaded.visibility = View.VISIBLE
                        btnSurfaceType.isEnabled = false
                        btnSmoothnessType.isEnabled = false
                        fab.visibility = View.INVISIBLE
                    }

                    if (bikeActivityWithSamples.bikeActivity.endTime != null) {
                        tvStopTime.text =
                            sdfShort.format(Date.from(bikeActivityWithSamples.bikeActivity.endTime))
                        tvDelimiter.visibility = View.VISIBLE
                        tvStopTime.visibility = View.VISIBLE

                        val diff =
                            bikeActivityWithSamples.bikeActivity.endTime.toEpochMilli() - bikeActivityWithSamples.bikeActivity.startTime.toEpochMilli()
                        val duration = (diff / 1000 / 60).toInt()
                        tvDuration.text =
                            resources.getQuantityString(R.plurals.duration, duration, duration)

                        ivStop.visibility = View.INVISIBLE
                    } else {
                        tvDelimiter.visibility = View.INVISIBLE
                        tvStopTime.visibility = View.INVISIBLE
                        ivStop.visibility = View.VISIBLE
                    }

                    tvSamples.text = resources.getQuantityString(
                        R.plurals.samples,
                        bikeActivityWithSamples.bikeActivitySamples.size,
                        bikeActivityWithSamples.bikeActivitySamples.size
                    )
                    ivStop.setOnClickListener {

                        // Update bike activity
                        viewModel.bikeActivityWithDetails.value?.bikeActivity?.let {
                            bikeActivityViewModel.update(it.copy(endTime = Instant.now()))
                        }
                    }

                    bikeActivityWithSamples.bikeActivity.surfaceType?.let {
                        btnSurfaceType.text = it
                            .replace("_", " ")
                            .replace(":", " ")
                    }
                    bikeActivityWithSamples.bikeActivity.smoothnessType?.let {
                        btnSmoothnessType.text = it
                    }

                    btnSurfaceType.setOnClickListener {
                        val intent = Intent(
                            applicationContext,
                            SurfaceTypeActivity::class.java
                        ).apply {
                            putExtra(
                                EXTRA_SURFACE_TYPE,
                                bikeActivityWithSamples.bikeActivity.surfaceType
                            )
                        }

                        @Suppress("DEPRECATION")
                        startActivityForResult(intent, REQUEST_SURFACE_TYPE)
                    }

                    btnSmoothnessType.setOnClickListener {
                        val intent = Intent(
                            applicationContext,
                            SmoothnessTypeActivity::class.java
                        ).apply {
                            putExtra(
                                EXTRA_SMOOTHNESS_TYPE,
                                bikeActivityWithSamples.bikeActivity.smoothnessType
                            )
                        }

                        @Suppress("DEPRECATION")
                        startActivityForResult(intent, REQUEST_SMOOTHNESS_TYPE)
                    }

                    fab.setOnClickListener {
                        if (bikeActivityWithSamples.bikeActivity.uploadStatus != BikeActivityStatus.UPLOADED) {
                            val serviceResultReceiver =
                                FirestoreServiceResultReceiver(Handler(Looper.getMainLooper()))
                            serviceResultReceiver.receiver = this
                            FirestoreService.enqueueWork(
                                this,
                                bikeActivityWithSamples,
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

        bikeActivitySampleViewModel.bikeActivitySamplesWithMeasurements(bikeActivityUid)
            .observe(this, {
                adapter.data = it
                if (adapter.data.isNotEmpty()) {
                    recyclerView.smoothScrollToPosition(adapter.data.size - 1)
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
     * Handles click on back button
     */
    override fun onBackPressed() {
        onNavigateUp()
    }

    /**
     * Handles option menu creation
     */
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
                            .singleBikeActivityWithSamples(it)
                            .observe(this, { bikeActivityWithDetails ->

                                // Update bike activity status
                                bikeActivityViewModel.update(
                                    bikeActivityWithDetails.bikeActivity.copy(
                                        uploadStatus = BikeActivityStatus.UPLOADED
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
     * Handles activity result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SURFACE_TYPE && resultCode == RESULT_OK) {
            data?.getStringExtra(RESULT_SURFACE_TYPE)?.let { surfaceType ->

                // Update bike activity
                viewModel.bikeActivityWithDetails.value?.bikeActivity?.let {
                    bikeActivityViewModel.update(it.copy(surfaceType = surfaceType))
                }
            }
        } else if (requestCode == REQUEST_SMOOTHNESS_TYPE && resultCode == RESULT_OK) {
            data?.getStringExtra(RESULT_SMOOTHNESS_TYPE)?.let { smoothnessType ->

                // Update bike activity
                viewModel.bikeActivityWithDetails.value?.bikeActivity?.let {
                    bikeActivityViewModel.update(it.copy(smoothnessType = smoothnessType))
                }
            }
        }
    }

    override fun onBikeActivitySampleItemClicked(bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements) {
        centerMap(
            mapboxMap,
            listOf(
                bikeActivitySampleWithMeasurements.bikeActivitySample,
                bikeActivitySampleWithMeasurements.bikeActivitySample
            ),
            duration = 1_000
        )
    }

    //
    // Helpers
    //

    /**
     * Centers map to include all samples
     */
    private fun centerMap(
        mapboxMap: MapboxMap,
        bikeActivitySamples: List<BikeActivitySample>,
        padding: Int = 250,
        duration: Int = 1
    ) {
        if (bikeActivitySamples.filter { bikeActivitySample ->
                bikeActivitySample.lon != 0.0 || bikeActivitySample.lat != 0.0
            }.size > 1) {
            val latLngBounds = LatLngBounds.Builder()

            bikeActivitySamples.filter { bikeActivitySample ->
                bikeActivitySample.lon != 0.0 || bikeActivitySample.lat != 0.0
            }.forEach { bikeActivityDetail ->
                latLngBounds.include(
                    LatLng(
                        bikeActivityDetail.lat,
                        bikeActivityDetail.lon
                    )
                )
            }

            mapboxMap.easeCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), padding),
                duration,
                true
            )
        }
    }

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
        const val REQUEST_SURFACE_TYPE = 1
        const val REQUEST_SMOOTHNESS_TYPE = 2

        const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"
        const val EXTRA_TRACKING_SERVICE_ENABLED = "extra.TRACKING_SERVICE_ENABLED"

        const val RESULT_BIKE_ACTIVITY_UID = "result.BIKE_ACTIVITY_UID"
    }
}