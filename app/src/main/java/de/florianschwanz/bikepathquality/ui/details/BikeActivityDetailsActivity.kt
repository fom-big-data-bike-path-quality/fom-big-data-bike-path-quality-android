package de.florianschwanz.bikepathquality.ui.details

import androidx.lifecycle.Observer
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
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
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityStatus
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityWithSamples
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModelFactory
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModel
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModelFactory
import de.florianschwanz.bikepathquality.services.FirebaseFirestoreService
import de.florianschwanz.bikepathquality.services.FirebaseFirestoreServiceResultReceiver
import de.florianschwanz.bikepathquality.services.FirebaseStorageService
import de.florianschwanz.bikepathquality.services.FirebaseStorageServiceResultReceiver
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

class BikeActivityDetailsActivity : AppCompatActivity(),
    FirebaseFirestoreServiceResultReceiver.Receiver,
    FirebaseStorageServiceResultReceiver.Receiver,
    BikeActivitySampleListAdapter.OnItemClickListener {

    private val logEntryViewModel: LogEntryViewModel by viewModels {
        LogEntryViewModelFactory((this.application as BikePathQualityApplication).logEntryRepository)
    }
    private val bikeActivityViewModel: BikeActivityViewModel by viewModels {
        BikeActivityViewModelFactory((this.application as BikePathQualityApplication).bikeActivityRepository)
    }
    private val bikeActivitySampleViewModel: BikeActivitySampleViewModel by viewModels {
        BikeActivitySampleViewModelFactory((this.application as BikePathQualityApplication).bikeActivitySampleRepository)
    }
    private val userDataViewModel: UserDataViewModel by viewModels {
        UserDataViewModelFactory((this.application as BikePathQualityApplication).userDataRepository)
    }

    private lateinit var viewModel: BikeActivityDetailsViewModel
    private lateinit var mapboxMap: MapboxMap
    private var mapView: MapView? = null

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
        val ivWarning: ImageView = findViewById(R.id.ivWarning)
        val tvChangedAfterUpload: TextView = findViewById(R.id.tvChangedAfterUpload)
        val tvStartTime: TextView = findViewById(R.id.tvStartTime)
        val tvDelimiter: TextView = findViewById(R.id.tvDelimiter)
        val tvStopTime: TextView = findViewById(R.id.tvStopTime)
        val tvSamples: TextView = findViewById(R.id.tvSamples)
        val ivStop: ImageView = findViewById(R.id.ivStop)
        val btnSurfaceType: MaterialButton = findViewById(R.id.btnSurfaceType)
        val btnSmoothnessType: MaterialButton = findViewById(R.id.btnSmoothnessType)
        val btnPhonePosition: MaterialButton = findViewById(R.id.btnPhonePosition)
        val spPhonePosition: Spinner = findViewById(R.id.spPhonePosition)
        val btnBikeType: MaterialButton = findViewById(R.id.btnBikeType)
        val spBikeType: Spinner = findViewById(R.id.spBikeType)
        val rvBikeActivitySamples: RecyclerView = findViewById(R.id.rvBikeActivitySamples)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        val adapter = BikeActivitySampleListAdapter(this, this)

        rvBikeActivitySamples.adapter = adapter
        rvBikeActivitySamples.layoutManager = LinearLayoutManager(this)

        if (!intent.hasExtra(EXTRA_BIKE_ACTIVITY_UID)) {
            finish()
        }

        viewModel = ViewModelProvider(this).get(BikeActivityDetailsViewModel::class.java)
        viewModel.bikeActivityWithSamples.observe(this, { bikeActivityWithSamples ->

            adapter.uploadStatus = bikeActivityWithSamples.bikeActivity.uploadStatus

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete -> {
                        viewModel.bikeActivityWithSamples.removeObservers(this)

                        val resultIntent = Intent()
                        resultIntent.putExtra(
                            RESULT_BIKE_ACTIVITY_UID,
                            bikeActivityWithSamples.bikeActivity.uid.toString()
                        )
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }

                false
            }

            mapView?.getMapAsync { mapAsync ->

                mapboxMap = mapAsync

                val bikeActivitySamplesFocus: List<BikeActivitySample> =
                    viewModel.bikeActivitySampleInFocusPosition.value?.let { position ->
                        listOfNotNull(
                            if (position > 0) bikeActivityWithSamples.bikeActivitySamples[position - 1] else null,
                            bikeActivityWithSamples.bikeActivitySamples[position],
                            if (position < bikeActivityWithSamples.bikeActivitySamples.size - 1) bikeActivityWithSamples.bikeActivitySamples[position + 1] else null,
                        )
                    } ?: run {
                        bikeActivityWithSamples.bikeActivitySamples
                    }

                centerMap(mapboxMap, bikeActivitySamplesFocus, duration = 1_000)
                setMapStyle(
                    mapboxMap,
                    buildMapStyle(),
                    buildMapCoordinates(bikeActivityWithSamples.bikeActivitySamples),
                    viewModel.bikeActivitySampleInFocus.value?.bikeActivitySample?.let {
                        buildMapCoordinate(it)
                    }
                )

                clDescription.setOnClickListener {
                    setMapStyle(
                        mapboxMap,
                        buildMapStyle(),
                        buildMapCoordinates(bikeActivityWithSamples.bikeActivitySamples),
                        null
                    )
                    centerMap(
                        mapboxMap,
                        bikeActivityWithSamples.bikeActivitySamples,
                        duration = 1_000
                    )

                    viewModel.bikeActivitySampleInFocusPosition.value = null
                    viewModel.bikeActivitySampleInFocus.value = null
                }
            }

            tvStartTime.text =
                sdf.format(Date.from(bikeActivityWithSamples.bikeActivity.startTime))
            tvStartTime.visibility = View.VISIBLE

            val activeColor = Color.parseColor(getThemeColorInHex(R.attr.colorPrimary))
            val inactiveColor = getColor(R.color.grey_500)

            ivCheck.visibility =
                if (bikeActivityWithSamples.isUploaded()) View.VISIBLE else View.INVISIBLE
            tvUploaded.visibility =
                if (bikeActivityWithSamples.isUploaded()) View.VISIBLE else View.INVISIBLE
            ivWarning.visibility =
                if (bikeActivityWithSamples.isChangedAfterUpload()) View.VISIBLE else View.INVISIBLE
            tvChangedAfterUpload.visibility =
                if (bikeActivityWithSamples.isChangedAfterUpload()) View.VISIBLE else View.INVISIBLE

            fab.backgroundTintList =
                if (bikeActivityWithSamples.isLabelledCompletely() && !bikeActivityWithSamples.isUploaded()) ColorStateList.valueOf(
                    activeColor
                ) else ColorStateList.valueOf(
                    inactiveColor
                )

            tvStopTime.text = if (bikeActivityWithSamples.isFinished())
                sdfShort.format(Date.from(bikeActivityWithSamples.bikeActivity.endTime)) else ""
            tvDelimiter.visibility =
                if (bikeActivityWithSamples.isFinished()) View.VISIBLE else View.INVISIBLE
            tvStopTime.visibility =
                if (bikeActivityWithSamples.isFinished()) View.VISIBLE else View.INVISIBLE
            ivStop.visibility =
                if (bikeActivityWithSamples.isFinished()) View.INVISIBLE else View.VISIBLE
            tvSamples.text = resources.getQuantityString(
                R.plurals.samples,
                bikeActivityWithSamples.bikeActivitySamples.size,
                bikeActivityWithSamples.bikeActivitySamples.size
            )

            ivStop.setOnClickListener {
                viewModel.bikeActivityWithSamples.value?.bikeActivity?.let {
                    bikeActivityViewModel.update(it.copy(endTime = Instant.now()))
                }
            }

            bikeActivityWithSamples.bikeActivity.surfaceType?.let {
                btnSurfaceType.text = it
                    .replace("_", " ")
                    .replace(":", " ")
            } ?: run {
                btnSurfaceType.text = resources.getString(R.string.default_surface_type)
            }
            bikeActivityWithSamples.bikeActivity.smoothnessType?.let {
                btnSmoothnessType.text = it
                    .replace("_", " ")
                    .replace(":", " ")
            } ?: run {
                btnSmoothnessType.text = resources.getString(R.string.default_smoothness_type)
            }
            bikeActivityWithSamples.bikeActivity.phonePosition?.let {
                btnPhonePosition.text = it
            } ?: run {
                btnPhonePosition.text = resources.getString(R.string.default_phone_position)
            }
            bikeActivityWithSamples.bikeActivity.bikeType?.let {
                btnBikeType.text = it
            } ?: run {
                btnBikeType.text = resources.getString(R.string.default_bike_type)
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

            var spPhonePositionClicked = false

            btnPhonePosition.setOnClickListener {
                spPhonePosition.performClick()
                spPhonePositionClicked = true
            }

            ArrayAdapter.createFromResource(
                this,
                R.array.phone_position_array,
                android.R.layout.simple_spinner_item
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spPhonePosition.adapter = it
            }

            spPhonePosition.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spPhonePositionClicked) {
                            val selectedItem =
                                if (position > 0) parent.getItemAtPosition(position)
                                    .toString() else null

                            bikeActivityViewModel.update(
                                bikeActivityWithSamples.bikeActivity.copy(phonePosition = selectedItem)
                            )
                        }

                        spPhonePositionClicked = false
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }

            var spBikeTypeClicked = false

            btnBikeType.setOnClickListener {
                spBikeType.performClick()
                spBikeTypeClicked = true
            }

            ArrayAdapter.createFromResource(
                this,
                R.array.bike_type_array,
                android.R.layout.simple_spinner_item
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spBikeType.adapter = it
            }

            spBikeType.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (spBikeTypeClicked) {
                            val selectedItem =
                                if (position > 0) parent.getItemAtPosition(position)
                                    .toString() else null

                            bikeActivityViewModel.update(
                                bikeActivityWithSamples.bikeActivity.copy(bikeType = selectedItem)
                            )
                        }

                        spBikeTypeClicked = false
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }

            fab.setOnClickListener {
                if (!bikeActivityWithSamples.isLabelledCompletely()) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_complete_data_title)
                        .setMessage(R.string.dialog_complete_data_message)
                        .setPositiveButton(R.string.action_got_it) { _, _ ->
                        }
                        .create()
                        .show()
                } else if (bikeActivityWithSamples.isUploaded()) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_nothing_changed_title)
                        .setMessage(R.string.dialog_nothing_changed_message)
                        .setPositiveButton(R.string.action_okay) { _, _ ->
                            uploadData(bikeActivityWithSamples)
                        }
                        .setNegativeButton(R.string.action_cancel) { _, _ ->
                        }
                        .create()
                        .show()
                } else if (bikeActivityWithSamples.isChangedAfterUpload()) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_upload_again_title)
                        .setMessage(R.string.dialog_upload_again_message)
                        .setPositiveButton(R.string.action_okay) { _, _ ->
                            uploadData(bikeActivityWithSamples)
                        }
                        .setNegativeButton(R.string.action_cancel) { _, _ ->
                        }
                        .create()
                        .show()
                } else {
                    uploadData(bikeActivityWithSamples)
                }
            }
        })
        viewModel.bikeActivitySamplesWithMeasurements.observe(this, {
            adapter.data = it
            if (adapter.data.isNotEmpty()) {

                rvBikeActivitySamples.smoothScrollToPosition(
                    viewModel.bikeActivitySampleInFocusPosition.value ?: adapter.data.size - 1
                )
            }
        })
        viewModel.bikeActivitySampleInFocus.observe(this, { bikeActivitySampleInFocus ->
            adapter.focus = bikeActivitySampleInFocus

            viewModel.bikeActivityWithSamples.value?.let { bikeActivityWithSamples ->
                viewModel.bikeActivitySampleInFocusPosition.value?.let { position ->
                    val bikeActivitySamples =
                        listOfNotNull(
                            if (position > 0) bikeActivityWithSamples.bikeActivitySamples[position - 1] else null,
                            bikeActivityWithSamples.bikeActivitySamples[position],
                            if (position < bikeActivityWithSamples.bikeActivitySamples.size - 1) bikeActivityWithSamples.bikeActivitySamples[position + 1] else null,
                        )

                    setMapStyle(
                        mapboxMap,
                        buildMapStyle(),
                        buildMapCoordinates(bikeActivityWithSamples.bikeActivitySamples),
                        bikeActivitySampleInFocus?.bikeActivitySample?.let { buildMapCoordinate(it) }
                    )
                    centerMap(mapboxMap, bikeActivitySamples, duration = 1_000)
                }
            }
        })

        intent.getStringExtra(EXTRA_BIKE_ACTIVITY_UID)?.let { initializeData(it) }
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
     * Handles results of Firebase Firestore service
     */
    override fun onReceiveFirebaseFirestoreServiceResult(resultCode: Int, resultData: Bundle?) {
        when (resultCode) {
            FirebaseFirestoreService.RESULT_SUCCESS -> {

                Toast.makeText(
                    applicationContext,
                    R.string.action_upload_successful,
                    Toast.LENGTH_LONG
                ).show()

                val bikeActivityUid =
                    resultData?.getString(FirebaseFirestoreService.EXTRA_BIKE_ACTIVITY_UID)

                markBikeActivityAsUploaded(bikeActivityUid)
            }
            FirebaseFirestoreService.RESULT_FAILURE -> {

                Toast.makeText(
                    applicationContext,
                    R.string.action_upload_failed,
                    Toast.LENGTH_LONG
                ).show()

                resultData?.getString(FirebaseFirestoreService.EXTRA_ERROR_MESSAGE)?.let { log(it) }
            }
        }
    }

    /**
     * Handles results of Firebase Storage service
     */
    override fun onReceiveFirebaseStorageServiceResult(resultCode: Int, resultData: Bundle?) {
        when (resultCode) {
            FirebaseFirestoreService.RESULT_SUCCESS -> {

                Toast.makeText(
                    applicationContext,
                    R.string.action_upload_successful,
                    Toast.LENGTH_LONG
                ).show()

                val bikeActivityUid =
                    resultData?.getString(FirebaseFirestoreService.EXTRA_BIKE_ACTIVITY_UID)

                markBikeActivityAsUploaded(bikeActivityUid)
            }
            FirebaseFirestoreService.RESULT_FAILURE -> {

                Toast.makeText(
                    applicationContext,
                    R.string.action_upload_failed,
                    Toast.LENGTH_LONG
                ).show()

                resultData?.getString(FirebaseFirestoreService.EXTRA_ERROR_MESSAGE)?.let { log(it) }
            }
        }
    }

    /**
     * Handles activity result
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            RESULT_OK -> {
                when (requestCode) {
                    REQUEST_SURFACE_TYPE -> {
                        viewModel.bikeActivityWithSamples.value?.bikeActivity?.let {
                            bikeActivityViewModel.update(
                                it.copy(
                                    surfaceType = data?.getStringExtra(
                                        RESULT_SURFACE_TYPE
                                    ),
                                    uploadStatus = if (it.uploadStatus != BikeActivityStatus.LOCAL) BikeActivityStatus.CHANGED_AFTER_UPLOAD else BikeActivityStatus.LOCAL
                                )
                            )
                        }
                    }
                    REQUEST_SMOOTHNESS_TYPE -> {
                        viewModel.bikeActivityWithSamples.value?.bikeActivity?.let {
                            bikeActivityViewModel.update(
                                it.copy(
                                    smoothnessType = data?.getStringExtra(
                                        RESULT_SMOOTHNESS_TYPE
                                    ),
                                    uploadStatus = if (it.uploadStatus != BikeActivityStatus.LOCAL) BikeActivityStatus.CHANGED_AFTER_UPLOAD else BikeActivityStatus.LOCAL
                                )
                            )
                        }
                    }
                    REQUEST_SURFACE_TYPE_FOR_SAMPLE -> {
                        viewModel.bikeActivitySampleInFocus.value?.bikeActivitySample?.let {
                            bikeActivitySampleViewModel.update(
                                it.copy(
                                    surfaceType = data?.getStringExtra(
                                        RESULT_SURFACE_TYPE
                                    )
                                )
                            )
                        }
                        viewModel.bikeActivityWithSamples.value?.bikeActivity?.let {
                            bikeActivityViewModel.update(
                                it.copy(
                                    uploadStatus = if (it.uploadStatus != BikeActivityStatus.LOCAL) BikeActivityStatus.CHANGED_AFTER_UPLOAD else BikeActivityStatus.LOCAL
                                )
                            )
                        }
                    }
                }
            }
        }

        viewModel.bikeActivitySampleInFocusPosition.postValue(viewModel.bikeActivitySampleInFocusPosition.value)
        viewModel.bikeActivitySampleInFocus.postValue(viewModel.bikeActivitySampleInFocus.value)
    }

    /**
     * Handles click on activity sample item
     */
    override fun onBikeActivitySampleItemClicked(
        bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements,
        position: Int
    ) {
        viewModel.bikeActivitySampleInFocusPosition.value = position
        viewModel.bikeActivitySampleInFocus.value = bikeActivitySampleWithMeasurements
    }

    /**
     * Handles click on bike activity sample item
     */
    override fun onBikeActivitySampleSurfaceTypeClicked(
        bikeActivitySampleWithMeasurements: BikeActivitySampleWithMeasurements,
        position: Int
    ) {
        viewModel.bikeActivitySampleInFocusPosition.value = position
        viewModel.bikeActivitySampleInFocus.value = bikeActivitySampleWithMeasurements

        val intent = Intent(
            applicationContext,
            SurfaceTypeActivity::class.java
        ).apply {
            putExtra(
                EXTRA_SURFACE_TYPE,
                bikeActivitySampleWithMeasurements.bikeActivitySample.surfaceType
            )
        }

        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_SURFACE_TYPE_FOR_SAMPLE)
    }

    //
    // Initialization
    //

    /**
     * Initializes data
     */
    private fun initializeData(bikeActivityUid: String) {
        bikeActivityViewModel.singleBikeActivityWithSamples(bikeActivityUid).observe(this, {
            viewModel.bikeActivityWithSamples.value = it
        })

        bikeActivitySampleViewModel.bikeActivitySamplesWithMeasurements(bikeActivityUid)
            .observe(this, {
                viewModel.bikeActivitySamplesWithMeasurements.value = it
            })

        userDataViewModel.singleUserData().observe(this, {
            viewModel.userData.value = it
        })
    }

    private fun markBikeActivityAsUploaded(bikeActivityUid: String?) = bikeActivityUid?.let {
        bikeActivityViewModel
            .singleBikeActivityWithSamples(it)
            .observeOnce(this, { bikeActivityWithDetails ->
                bikeActivityViewModel.update(
                    bikeActivityWithDetails.bikeActivity.copy(
                        uploadStatus = BikeActivityStatus.UPLOADED
                    )
                )
            })
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    //
    // Helpers
    //

    private fun BikeActivityWithSamples.isLabelledCompletely() =
        this.bikeActivity.surfaceType != null &&
                this.bikeActivity.smoothnessType != null &&
                this.bikeActivity.phonePosition != null &&
                this.bikeActivity.bikeType != null

    private fun BikeActivityWithSamples.isUploaded() =
        this.bikeActivity.uploadStatus == BikeActivityStatus.UPLOADED

    private fun BikeActivityWithSamples.isChangedAfterUpload() =
        this.bikeActivity.uploadStatus == BikeActivityStatus.CHANGED_AFTER_UPLOAD

    private fun BikeActivityWithSamples.isFinished() = this.bikeActivity.endTime != null

    private fun uploadData(bikeActivityWithSamples: BikeActivityWithSamples) {
        val serviceResultReceiver =
            FirebaseStorageServiceResultReceiver(Handler(Looper.getMainLooper()))
        serviceResultReceiver.receiver = this

        FirebaseStorageService.enqueueWork(
            this,
            bikeActivityWithSamples.bikeActivity,
            viewModel.bikeActivitySamplesWithMeasurements.value ?: listOf(),
            viewModel.userData.value ?: UserData(),
            serviceResultReceiver
        )
    }

    /**
     * Builds map style based on night mode
     */
    private fun buildMapStyle() =
        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO -> Style.LIGHT
            Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.LIGHT
            else -> Style.LIGHT
        }

    /**
     * Builds map coordinates based on bike activity samples
     */
    private fun buildMapCoordinates(bikeActivitySamples: List<BikeActivitySample>) =
        bikeActivitySamples
            .filter { it.lon != 0.0 || it.lat != 0.0 }
            .map(this::buildMapCoordinate)

    /**
     * Builds map coordinate based on bike activity sample
     */
    private fun buildMapCoordinate(bikeActivitySample: BikeActivitySample) = Point.fromLngLat(
        bikeActivitySample.lon,
        bikeActivitySample.lat
    )

    /**
     * Sets map style
     */
    private fun setMapStyle(
        mapboxMap: MapboxMap,
        mapStyle: String,
        mapRouteCoordinates: List<Point>,
        mapFocusCoordinate: Point?
    ) {
        val line: Pair<String, String> = Pair("line-source", "line-layer")
        val samples: Pair<String, String> = Pair("sample-source", "sample-layer")
        val highlight: Pair<String, String> = Pair("highlight-source", "highlight-layer")

        mapboxMap.setStyle(mapStyle) { style ->

            style.addLineSource(line.first, mapRouteCoordinates)
            style.addPointSource(samples.first, mapRouteCoordinates)
            mapFocusCoordinate?.let { style.addPointSource(highlight.first, listOf(it)) }

            style.addLineLayer(
                line.second,
                line.first,
                floatArrayOf(0.01f, 2f),
                Property.LINE_CAP_ROUND,
                Property.LINE_JOIN_ROUND,
                3f,
                Color.parseColor(getThemeColorInHex(R.attr.colorButtonNormal))
            )
            style.addCircleLayer(samples.second, samples.first, 5f, R.attr.colorPrimary)
            style.addCircleLayer(
                highlight.second,
                highlight.first,
                10f,
                R.attr.colorSecondary
            )

            mapboxMap.uiSettings.isRotateGesturesEnabled = false
        }
    }

    private fun Style.addLineSource(name: String, points: List<Point>) = this.addSource(
        GeoJsonSource(
            name,
            FeatureCollection.fromFeatures(
                arrayOf<Feature>(
                    Feature.fromGeometry(
                        LineString.fromLngLats(points)
                    )
                )
            )
        )
    )

    private fun Style.addPointSource(name: String, points: List<Point>) = this.addSource(
        GeoJsonSource(
            name,
            FeatureCollection.fromFeatures(
                points.map { point ->
                    Feature.fromGeometry(point)
                }.toTypedArray()
            )
        )
    )

    private fun Style.addLineLayer(
        layerName: String,
        sourceName: String,
        lineDashArray: FloatArray,
        lineCap: String,
        lineJoin: String,
        lineWidth: Float,
        lineColor: Int
    ) = this.addLayer(
        LineLayer(layerName, sourceName).withProperties(
            PropertyFactory.lineDasharray(lineDashArray.toTypedArray()),
            PropertyFactory.lineCap(lineCap),
            PropertyFactory.lineJoin(lineJoin),
            PropertyFactory.lineWidth(lineWidth),
            PropertyFactory.lineColor(lineColor)
        )
    )

    private fun Style.addCircleLayer(
        layerName: String,
        sourceName: String,
        circleRadius: Float,
        circleColor: Int
    ) = this.addLayer(
        CircleLayer(layerName, sourceName).withProperties(
            PropertyFactory.circleRadius(circleRadius),
            PropertyFactory.circleColor(Color.parseColor(getThemeColorInHex(circleColor)))
        )
    )

    /**
     * Creates bounds around bike activity samples
     */
    private fun buildBounds(bikeActivitySamples: List<BikeActivitySample>): LatLngBounds? {
        return if (bikeActivitySamples.filter { bikeActivitySample ->
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

            latLngBounds.build()
        } else null
    }

    /**
     * Centers map to include all samples
     */
    private fun centerMap(
        mapboxMap: MapboxMap,
        bikeActivitySamples: List<BikeActivitySample>,
        padding: Int = 250,
        duration: Int = 1
    ) {
        buildBounds(bikeActivitySamples)?.let { bounds ->
            mapboxMap.easeCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, padding),
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
        const val REQUEST_SURFACE_TYPE_FOR_SAMPLE = 3

        const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"
        const val EXTRA_TRACKING_SERVICE_ENABLED = "extra.TRACKING_SERVICE_ENABLED"

        const val RESULT_BIKE_ACTIVITY_UID = "result.BIKE_ACTIVITY_UID"
    }
}