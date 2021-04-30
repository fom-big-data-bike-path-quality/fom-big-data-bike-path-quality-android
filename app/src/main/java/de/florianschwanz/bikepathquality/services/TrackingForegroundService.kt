package de.florianschwanz.bikepathquality.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import de.florianschwanz.bikepathquality.BikePathQualityApplication
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.livedata.AccelerometerLiveData
import de.florianschwanz.bikepathquality.data.livedata.ActivityTransitionLiveData
import de.florianschwanz.bikepathquality.data.livedata.LocationLiveData
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityDetail
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityDetailViewModel
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityViewModel
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryViewModel
import de.florianschwanz.bikepathquality.ui.main.MainActivity
import java.time.Instant

/**
 * Tracking foreground service
 */
class TrackingForegroundService : LifecycleService() {

    private lateinit var logEntryViewModel: LogEntryViewModel
    private lateinit var bikeActivityViewModel: BikeActivityViewModel
    private lateinit var bikeActivityDetailViewModel: BikeActivityDetailViewModel

    private lateinit var activityTransitionLiveData: ActivityTransitionLiveData
    private lateinit var accelerometerLiveData: AccelerometerLiveData
    private lateinit var locationLiveData: LocationLiveData

    private val targetActivityType = DetectedActivity.ON_BICYCLE

    // Currently performed biking activity
    private var activeActivity: BikeActivity? = null
    private var activeActivityType = -1
    private var activeTransitionType = -1

    // Current location
    private var currentLon = 0.0
    private var currentLat = 0.0

    // Current accelerometer values
    private var currentAccelerometerX = 0.0f
    private var currentAccelerometerY = 0.0f
    private var currentAccelerometerZ = 0.0f

    private val activityDetailHandler = Handler(Looper.getMainLooper())
    private var activityDetailTracker: Runnable = object : Runnable {
        override fun run() {
            try {
                log("new tracking for ${activeActivity?.uid}")

                var startTime = Instant.now().toEpochMilli()
                var stopTime = Instant.now().toEpochMilli()

                repeat(TRACKING_SAMPLE_SIZE) {

                    log("new sample for ${activeActivity?.uid}")

                    // Sleep until next measurement
                    while (startTime - stopTime < TRACKING_SAMPLE_INTERVAL) {
                        Thread.sleep(TRACKING_SAMPLE_INTERVAL / 10)
                        startTime = Instant.now().toEpochMilli()
                    }

                    // Make a measurement
                    trackActivityDetail()

                    stopTime = Instant.now().toEpochMilli()
                }

            } finally {
                activityDetailHandler.postDelayed(this, TRACKING_DELAY)
            }
        }
    }

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
        bikeActivityViewModel = BikeActivityViewModel(app.bikeActivitiesRepository)
        bikeActivityDetailViewModel = BikeActivityDetailViewModel(app.bikeActivityDetailsRepository)

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
                showNotification(
                    title = R.string.action_tracking_bike_activity_idle,
                    text = R.string.action_tracking_bike_activity_idle_description,
                    icon = R.drawable.ic_baseline_pause_24
                )

                handleActiveBikeActivity()
                handleActivityTransitions()
                handleSensorData()

                val broadCastIntent = Intent(TAG)
                broadCastIntent.putExtra(EXTRA_STATUS, STATUS_STARTED)
                sendBroadcast(broadCastIntent)
                status = STATUS_STARTED

                log("Start tracking service")
            }
            ACTION_START_MANUALLY -> {
                showNotification(
                    title = R.string.action_tracking_bike_activity_idle,
                    text = R.string.action_tracking_bike_activity_idle_description,
                    icon = R.drawable.ic_baseline_pause_24
                )

                handleActiveBikeActivity()
                unhandleActivityTransitions()
                handleSensorData()

                val broadCastIntent = Intent(TAG)
                broadCastIntent.putExtra(EXTRA_STATUS, STATUS_STARTED_MANUALLY)
                sendBroadcast(broadCastIntent)
                status = STATUS_STARTED_MANUALLY

                log("Start tracking service (manually)")
            }
            ACTION_STOP -> {
                stopForeground(true)
                stopSelfResult(startId)

                val broadCastIntent = Intent(TAG)
                broadCastIntent.putExtra(EXTRA_STATUS, STATUS_STOPPED)
                sendBroadcast(broadCastIntent)
                status = STATUS_STOPPED

                log("Stop tracking service")
            }
        }

        return START_STICKY
    }

    //
    // Helpers
    //

    /**
     * Shows a notification
     */
    private fun showNotification(
        title: Int,
        text: Int,
        icon: Int
    ) {
        val notification = createNotification(
            title = title,
            text = text,
            icon = icon
        )
        startForeground(1, notification)
    }


    /**
     * Creates notification
     */
    private fun createNotification(
        notificationChannelId: String = createNotificationChannel(),
        title: Int,
        text: Int,
        icon: Int
    ): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(getString(title))
            .setContentText(getString(text))
            .setSmallIcon(icon)
            .setContentIntent(pendingIntent)
            .build()
    }

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
     * Retrieves most recent unfinished bike activity from the database
     */
    private fun handleActiveBikeActivity() {
        bikeActivityViewModel.activeBikeActivity.observe(this, {
            activeActivity = it

            if (activeActivity != null) {

                log("Start bike activity")

                showNotification(
                    title = R.string.action_tracking_bike_activity,
                    text = R.string.action_tracking_bike_activity_description,
                    icon = R.drawable.ic_baseline_pedal_bike_24
                )

                activityDetailTracker.run()
            } else {

                log("Stop bike activity")

                showNotification(
                    title = R.string.action_tracking_bike_activity_idle,
                    text = R.string.action_tracking_bike_activity_idle_description,
                    icon = R.drawable.ic_baseline_pause_24
                )

                activityDetailHandler.removeCallbacks(activityDetailTracker)
            }
        })
    }

    /**
     * Listens to activity transitions related to bicycle and if necessary
     * <li>creates a new bike activity
     * <li>finished an active bike activity
     */
    private fun handleActivityTransitions() {

        activityTransitionLiveData.observe(this, {

            log(toTransitionType(it.transitionType) + " " + toActivityString(it.activityType))

            if (it.activityType != activeActivityType || it.transitionType != activeTransitionType) {

                if (activeActivity == null
                    && it.activityType == targetActivityType
                    && it.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER
                ) {
                    bikeActivityViewModel.insert(BikeActivity())
                } else {
                    activeActivity?.let { bikeActivity ->
                        bikeActivityViewModel.update(bikeActivity.copy(endTime = Instant.now()))

                        showNotification(
                            title = R.string.action_tracking_bike_activity_idle,
                            text = R.string.action_tracking_bike_activity_idle_description,
                            icon = R.drawable.ic_baseline_pause_24
                        )
                    }
                }
            }

            activeActivityType = it.activityType
            activeTransitionType = it.transitionType
        })
    }

    /**
     * Unregisters from activity transitions
     */
    private fun unhandleActivityTransitions() {
        activityTransitionLiveData = ActivityTransitionLiveData(this)
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
        })
    }

    /**
     * Tracks an activity detail and persists it
     */
    private fun trackActivityDetail() = activeActivity?.let {

        bikeActivityDetailViewModel.insert(
            BikeActivityDetail(
                activityUid = it.uid,
                lon = currentLon,
                lat = currentLat,
                accelerometerX = currentAccelerometerX,
                accelerometerY = currentAccelerometerY,
                accelerometerZ = currentAccelerometerZ
            )
        )
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

        /** Delay between samples in millis */
        const val TRACKING_DELAY = 10_000L

        /** Time between measurements in a sample in millis */
        const val TRACKING_SAMPLE_INTERVAL = 100L

        /** Number of measurements per sample */
        const val TRACKING_SAMPLE_SIZE = 5

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