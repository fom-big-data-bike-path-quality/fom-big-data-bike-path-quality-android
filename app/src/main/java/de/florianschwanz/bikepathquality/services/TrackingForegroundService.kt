package de.florianschwanz.bikepathquality.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.preference.PreferenceManager
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.livedata.AccelerometerLiveData
import de.florianschwanz.bikepathquality.data.livedata.ActivityTransitionLiveData
import de.florianschwanz.bikepathquality.data.livedata.LocationLiveData
import de.florianschwanz.bikepathquality.data.storage.bike_activity.*
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataViewModel
import de.florianschwanz.bikepathquality.ui.main.MainActivity
import java.lang.System.currentTimeMillis
import java.time.Instant

/**
 * Tracking foreground service
 */
class TrackingForegroundService : LifecycleService() {

    private lateinit var logEntryViewModel: LogEntryViewModel
    private lateinit var bikeActivityViewModel: BikeActivityViewModel
    private lateinit var bikeActivitySampleViewModel: BikeActivitySampleViewModel
    private lateinit var bikeActivityMeasurementViewModel: BikeActivityMeasurementViewModel
    private lateinit var userDataViewModel: UserDataViewModel

    private lateinit var activityTransitionLiveData: ActivityTransitionLiveData
    private lateinit var accelerometerLiveData: AccelerometerLiveData
    private lateinit var locationLiveData: LocationLiveData

    private val targetActivityType = DetectedActivity.ON_BICYCLE

    private var userData: UserData? = null

    // Currently performed bike activity
    private var activeBikeActivity: BikeActivity? = null
    private var activeActivityType = -1
    private var activeTransitionType = -1

    // Current location
    private var currentLon = 0.0
    private var currentLat = 0.0
    private var currentSpeed = 0.0f

    // Current accelerometer values
    private var currentAccelerometerX = 0.0f
    private var currentAccelerometerY = 0.0f
    private var currentAccelerometerZ = 0.0f

    private var activitySampleStart = 0L
    private val activitySampleHandler = Handler(Looper.getMainLooper())
    private val activitySampleTracker: Runnable = object : Runnable {
        override fun run() {

            activitySampleStart = currentTimeMillis()

            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val activitySampleInterval = sharedPreferences.getInt(
                resources.getString(R.string.setting_sample_interval),
                DEFAULT_ACTIVITY_SAMPLE_INTERVAL
            ) * 1_000
            val activitySampleSize = sharedPreferences.getInt(
                resources.getString(R.string.setting_measurements_per_sample),
                DEFAULT_ACTIVITY_SAMPLE_SIZE
            )
            val activityMeasurementInterval = sharedPreferences.getInt(
                resources.getString(R.string.setting_measurement_interval),
                DEFAULT_ACTIVITY_MEASUREMENT_INTERVAL
            )

            try {
                activitySampleTrackerRunning = true

                // Track bike activity sample
                val bikeActivitySample = trackBikeActivitySample(activeBikeActivity)

                var measurements = 0
                var activityMeasurementStart = 0L
                val activityMeasurementHandler = Handler(Looper.getMainLooper())
                val activityMeasurementTracker: Runnable = object : Runnable {
                    override fun run() {
                        try {
                            activityMeasurementStart = currentTimeMillis()

                            // Track bike activity measurement
                            trackBikeActivityMeasurement(bikeActivitySample)
                            measurements++
                        } finally {
                            if (measurements < activitySampleSize) {

                                val activityMeasurementEnd = currentTimeMillis()
                                val activityMeasurementDuration =
                                    activityMeasurementEnd - activityMeasurementStart
                                val activityMeasurementDelay =
                                    activityMeasurementInterval.toLong() - activityMeasurementDuration

                                activityMeasurementHandler.postDelayed(
                                    this,
                                    activityMeasurementDelay
                                )
                            } else {
                                activityMeasurementHandler.removeCallbacks(this)
                            }
                        }
                    }
                }

                activityMeasurementTracker.run()
            } finally {
                val activitySampleEnd = currentTimeMillis()
                val activitySampleDuration = activitySampleEnd - activitySampleStart
                val activitySampleDelay = maxOf(
                    activitySampleInterval,
                    activityMeasurementInterval * activitySampleSize
                ).toLong() - activitySampleDuration

                activitySampleHandler.postDelayed(
                    this,
                    activitySampleDelay
                )
            }
        }
    }
    private var activitySampleTrackerRunning = false

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate() {
        super.onCreate()

        val app = application as BikePathQualityApplication

        logEntryViewModel = LogEntryViewModel(app.logEntryRepository)
        bikeActivityViewModel = BikeActivityViewModel(app.bikeActivityRepository)
        bikeActivitySampleViewModel = BikeActivitySampleViewModel(app.bikeActivitySampleRepository)
        bikeActivityMeasurementViewModel =
            BikeActivityMeasurementViewModel(app.bikeActivityMeasurementRepository)
        userDataViewModel = UserDataViewModel(app.userDataRepository)

        activityTransitionLiveData = ActivityTransitionLiveData(this)
        accelerometerLiveData = AccelerometerLiveData(this)
        locationLiveData = LocationLiveData(this)
    }

    /**
     * Handles on-start lifecycle phase
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {

                NotificationCompat.Builder(this, createNotificationChannel())
                    .setContentTitle(getString(R.string.action_tracking_bike_activity_idle))
                    .setContentText(getString(R.string.action_tracking_bike_activity_idle_description))
                    .setSmallIcon(R.drawable.ic_baseline_pause_24)
                    .setContentIntent(buildPendingIntent())
                    .setPriority(IMPORTANCE_HIGH)
                    .setWhen(0)
                    .build()
                    .startForeground()

                handleUserData()

                handleActiveBikeActivity()
                handleActivityTransitions()
                handleSensorData()

                val broadCastIntent = Intent(TAG)
                broadCastIntent.putExtra(EXTRA_STATUS, STATUS_STARTED)
                sendBroadcast(broadCastIntent)
                status = STATUS_STARTED

                setSharedPreferenceTrackingAutomatic(true)
                log("Start tracking service")
            }
            ACTION_START_MANUALLY -> {

                NotificationCompat.Builder(this, createNotificationChannel())
                    .setContentTitle(getString(R.string.action_tracking_bike_activity_idle))
                    .setContentText(getString(R.string.action_tracking_bike_activity_idle_description))
                    .setSmallIcon(R.drawable.ic_baseline_pause_24)
                    .setContentIntent(buildPendingIntent())
                    .setPriority(IMPORTANCE_HIGH)
                    .setWhen(0)
                    .build()
                    .startForeground()

                handleActiveBikeActivity()
                unhandleActivityTransitions()
                handleSensorData()

                val broadCastIntent = Intent(TAG)
                broadCastIntent.putExtra(EXTRA_STATUS, STATUS_STARTED_MANUALLY)
                sendBroadcast(broadCastIntent)
                status = STATUS_STARTED_MANUALLY

                setSharedPreferenceTrackingAutomatic(false)
                log("Start tracking service (manually)")
            }
            ACTION_STOP -> {
                stopForeground(true)
                stopSelfResult(startId)

                activitySampleTrackerRunning = false
                activitySampleHandler.removeCallbacks(activitySampleTracker)

                val broadCastIntent = Intent(TAG)
                broadCastIntent.putExtra(EXTRA_STATUS, STATUS_STOPPED)
                sendBroadcast(broadCastIntent)
                status = STATUS_STOPPED

                setSharedPreferenceTrackingAutomatic(false)
                log("Stop tracking service")
            }
        }

        return START_STICKY
    }

    //
    // Helpers
    //

    /**
     * Creates notification channel with a given id and name
     *
     * @param channelId channel ID
     * @param channelName channel name
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        channelId: String = CHANNEL_ID,
        channelName: String = CHANNEL_NAME
    ): String {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)
        return channelId
    }

    /**
     * Builds pending intent
     */
    private fun buildPendingIntent(): PendingIntent? = PendingIntent.getActivity(
        this,
        8,
        Intent(this, MainActivity::class.java),
        if (isAndroid12()) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_IMMUTABLE
        }
    )

    private fun isAndroid12() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /**
     * Sets shared preference to a given value
     */
    private fun setSharedPreferenceTrackingAutomatic(value: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit()
            .putBoolean(resources.getString(R.string.setting_tracking_automatic), value).apply()
    }

    /**
     * Retrieves user data from the database
     */
    private fun handleUserData() {
        userDataViewModel.singleUserData().observe(this) { singleUserData ->
            userData = singleUserData
        }
    }

    /**
     * Retrieves most recent unfinished bike activity from the database
     */
    private fun handleActiveBikeActivity() {
        bikeActivityViewModel.activeBikeActivity.observe(this, {
            activeBikeActivity = it

            if (activeBikeActivity != null) {

                log("Start bike activity")

                NotificationCompat.Builder(this, createNotificationChannel())
                    .setContentTitle(getString(R.string.action_tracking_bike_activity))
                    .setContentText(getString(R.string.action_tracking_bike_activity_description))
                    .setSmallIcon(R.drawable.ic_baseline_pedal_bike_24)
                    .setContentIntent(buildPendingIntent())
                    .setPriority(IMPORTANCE_HIGH)
                    .setWhen(0)
                    .build()
                    .startForeground()

                if (!activitySampleTrackerRunning) {
                    activitySampleTracker.run()
                }
            } else {

                log("Stop bike activity")

                NotificationCompat.Builder(this, createNotificationChannel())
                    .setContentTitle(getString(R.string.action_tracking_bike_activity_idle))
                    .setContentText(getString(R.string.action_tracking_bike_activity_idle_description))
                    .setSmallIcon(R.drawable.ic_baseline_pause_24)
                    .setContentIntent(buildPendingIntent())
                    .setPriority(IMPORTANCE_HIGH)
                    .setWhen(0)
                    .build()
                    .startForeground()

                activitySampleTrackerRunning = false
                activitySampleHandler.removeCallbacks(activitySampleTracker)
            }
        })
    }

    private fun Notification.startForeground(id: Int = 1) = startForeground(id, this)

    /**
     * Listens to activity transitions related to bicycle and if necessary
     * <li>creates a new bike activity
     * <li>finished an active bike activity
     */
    private fun handleActivityTransitions() {

        activityTransitionLiveData.observe(this, {

            log(toTransitionType(it.transitionType) + " " + toActivityString(it.activityType))

            if (userData != null && (it.activityType != activeActivityType || it.transitionType != activeTransitionType)) {

                if (activeBikeActivity == null
                    && it.activityType == targetActivityType
                    && it.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                ) {
                    startBikeActivity()
                } else {
                    stopBikeActivity()
                }
            }

            activeActivityType = it.activityType
            activeTransitionType = it.transitionType
        })
    }

    private fun startBikeActivity() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        bikeActivityViewModel.insert(
            BikeActivity(
                trackingType = BikeActivityTrackingType.AUTOMATIC,
                phonePosition = sharedPreferences.getString(
                    resources.getString(R.string.setting_phone_position),
                    null
                ),
                bikeType = sharedPreferences.getString(
                    resources.getString(R.string.setting_bike_type),
                    null
                )
            )
        )
    }

    private fun stopBikeActivity() = activeBikeActivity?.let { bikeActivity ->
        bikeActivityViewModel.update(bikeActivity.copy(endTime = Instant.now()))

        NotificationCompat.Builder(this, createNotificationChannel())
            .setContentTitle(getString(R.string.action_tracking_bike_activity_idle))
            .setContentText(getString(R.string.action_tracking_bike_activity_idle_description))
            .setSmallIcon(R.drawable.ic_baseline_pause_24)
            .setContentIntent(buildPendingIntent())
            .setPriority(IMPORTANCE_HIGH)
            .setWhen(0)
            .build()
            .startForeground()

    }

    /**
     * Unregisters from activity transitions
     */
    private fun unhandleActivityTransitions() {
        activityTransitionLiveData.removeObservers(this)
    }

    /**
     * Handles sensor data
     */
    private fun handleSensorData() {
        accelerometerLiveData.observe(this, {
            currentAccelerometerX = it.x
            currentAccelerometerY = it.y
            currentAccelerometerZ = it.z
        })
        locationLiveData.observe(this, {
            currentLon = it.lon
            currentLat = it.lat
            currentSpeed = it.speed
        })
    }

    /**
     * Tracks an activity sample and persists it
     */
    private fun trackBikeActivitySample(bikeActivity: BikeActivity?): BikeActivitySample? =
        bikeActivity?.let {

            BikeActivitySample(
                bikeActivityUid = bikeActivity.uid,
                lon = currentLon,
                lat = currentLat,
                speed = currentSpeed
            ).also {
                bikeActivitySampleViewModel.insert(it)
            }
        }

    /**
     * Tracks an activity measurement and persists it
     */
    private fun trackBikeActivityMeasurement(bikeActivitySample: BikeActivitySample?): BikeActivityMeasurement? =
        bikeActivitySample?.let {

            BikeActivityMeasurement(
                bikeActivitySampleUid = bikeActivitySample.uid,
                lon = currentLon,
                lat = currentLat,
                speed = currentSpeed,
                accelerometerX = currentAccelerometerX,
                accelerometerY = currentAccelerometerY,
                accelerometerZ = currentAccelerometerZ
            ).also {
                bikeActivityMeasurementViewModel.insert(it)
            }
        }

    /**
     * Logs message
     */
    private fun log(message: String) {
        logEntryViewModel.insert(LogEntry(message = message))
    }

    companion object {
        const val TAG = "TrackingForegroundService"

        const val ACTION_START = "action.START"
        const val ACTION_START_MANUALLY = "action.START_MANUALLY"
        const val ACTION_STOP = "action.STOP"

        const val CHANNEL_ID = "channel.TRACKING"
        const val CHANNEL_NAME = "channel.TRACKING"

        const val EXTRA_STATUS = "extra.STATUS"

        const val STATUS_STARTED = "status.STARTED"
        const val STATUS_STARTED_MANUALLY = "status.STARTED_MANUALLY"
        const val STATUS_STOPPED = "status.STOPPED"
        var status = STATUS_STOPPED

        /** Interval between samples in millis */
        const val DEFAULT_ACTIVITY_SAMPLE_INTERVAL = 10

        /** Interval between measurements in a sample in millis */
        const val DEFAULT_ACTIVITY_MEASUREMENT_INTERVAL = 50

        /** Number of measurements per sample */
        const val DEFAULT_ACTIVITY_SAMPLE_SIZE = 250

        /**
         * Converts activity to a string
         */
        private fun toActivityString(activity: Int) = when (activity) {
            DetectedActivity.STILL -> "standing still"
            DetectedActivity.WALKING -> "walking"
            DetectedActivity.RUNNING -> "running"
            DetectedActivity.ON_BICYCLE -> "cycling"
            DetectedActivity.IN_VEHICLE -> "being in a vehicle"
            else -> "unknown activity"
        }

        /**
         * Converts transition type to string
         */
        private fun toTransitionType(transitionType: Int) =
            when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "Start"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "Stop"
                else -> "???"
            }
    }
}