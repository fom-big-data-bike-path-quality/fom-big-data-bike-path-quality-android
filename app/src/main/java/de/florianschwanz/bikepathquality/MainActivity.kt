package de.florianschwanz.bikepathquality

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import de.florianschwanz.bikepathquality.fragments.AccelerometerCardViewModel
import de.florianschwanz.bikepathquality.fragments.AccelerometerDto
import de.florianschwanz.bikepathquality.fragments.ActivityTransitionDto
import de.florianschwanz.bikepathquality.fragments.ActivityTransitionViewModel
import de.florianschwanz.bikepathquality.logger.LogFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main activity
 */
class MainActivity : AppCompatActivity(), SensorEventListener {

    private var activityTrackingEnabled = false
    private var activityTransitionList = mutableListOf<ActivityTransition>()

    // Action fired when transitions are triggered
    private val TRANSITIONS_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"
    private var activityTransitionsPendingIntent: PendingIntent? = null
    private var transitionsReceiver: MainActivity.TransitionsReceiver? = null
    private var mLogFragment: LogFragment? = null

    private lateinit var sensorManager: SensorManager
    private var mAccelerometer: Sensor? = null

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initActivityTransitions()
        initSensors()

        printToScreen("App initialized.")
    }

    /**
     * Initializes activity transitions
     */
    private fun initActivityTransitions() {
        activityTrackingEnabled = false

        // Add activity transitions to track
        activityTransitionList.addTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        activityTransitionList.addTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        activityTransitionList.addTransition(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        activityTransitionList.addTransition(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        activityTransitionList.addTransition(DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        activityTransitionList.addTransition(DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        activityTransitionList.addTransition(DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        activityTransitionList.addTransition(DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT)
        activityTransitionList.addTransition(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        activityTransitionList.addTransition(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT)

        // Initialize PendingIntent that will be triggered when a activity transition occurs
        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        activityTransitionsPendingIntent =
            PendingIntent.getBroadcast(this@MainActivity, 0, intent, 0)

        // The receiver listens for the PendingIntent above that is triggered by the system when an activity transition occurs
        transitionsReceiver = TransitionsReceiver()
    }

    fun MutableList<ActivityTransition>.addTransition(activityType: Int, acvtivityTransition: Int) = this.add(
        ActivityTransition.Builder()
            .setActivityType(activityType)
            .setActivityTransition(acvtivityTransition)
            .build()
    )

    /**
     * Initializes view
     */
    private fun initView() {
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        mLogFragment = supportFragmentManager.findFragmentById(R.id.log_fragment) as LogFragment?
    }

    /**
     * Initializes sensory
     */
    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    /**
     * Handles on-start lifecycle phase
     */
    override fun onStart() {
        super.onStart()

        // Register the BroadcastReceiver to listen for activity transitions
        registerReceiver(transitionsReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))
    }

    /**
     * Handles on-resume lifecycle phase
     */
    override fun onResume() {
        super.onResume()

        mAccelerometer?.also { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    /**
     * Handles on-pause lifecycle phase
     */
    override fun onPause() {

        // Disable activity transitions when user leaves the app
        if (activityTrackingEnabled) {
            disableActivityTransitions()
        }

        sensorManager.unregisterListener(this)

        super.onPause()
    }

    /**
     * Handles on-pause lifecycle phase
     */
    override fun onStop() {

        // Unregister activity transition receiver when user leaves the app
        unregisterReceiver(transitionsReceiver)
        super.onStop()
    }

    /**
     * Handles on-activity-result lifecycle phase
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Start activity recognition if the permission was approved
        if (activityRecognitionPermissionApproved() && !activityTrackingEnabled) {
            enableActivityTransitions()
        }

        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }

    //
    // Actions
    //

    /**
     * Handles click on activity recognition button
     */
    fun onClickEnableOrDisableActivityRecognition(view: View?) {

        // Enable/Disable activity tracking and ask for permissions if needed
        if (activityRecognitionPermissionApproved()) {
            if (activityTrackingEnabled) {
                disableActivityTransitions()
            } else {
                enableActivityTransitions()
            }
        } else {
            // Request permission and start activity for result. If the permission is approved, we
            // want to make sure we start activity recognition tracking
            val startIntent = Intent(this, PermissionRationalActivity::class.java)

            @Suppress("DEPRECATION")
            startActivityForResult(startIntent, 0)
        }
    }

    /**
     * Handles sensor value changes
     */
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometerSensorEvent(event)
            }
        }
    }

    /**
     * Handles sensor accuracy changes
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    //
    // Helpers
    //

    /**
     * Registers callbacks for [ActivityTransition] events via a custom
     * [BroadcastReceiver]
     */
    private fun enableActivityTransitions() {
        Log.d(TAG, "enableActivityTransitions()")

        // Create request and listen for activity changes
        val request = ActivityTransitionRequest(activityTransitionList)

        // Register for Transitions Updates.
        val task = ActivityRecognition.getClient(this)
            .requestActivityTransitionUpdates(request, activityTransitionsPendingIntent!!)

        task.addOnSuccessListener {
            activityTrackingEnabled = true
            printToScreen("Transitions Api was successfully registered.")
        }
        task.addOnFailureListener { e ->
            printToScreen("Transitions Api could NOT be registered: $e")
            Log.e(TAG, "Transitions Api could NOT be registered: $e")
        }
    }

    /**
     * Unregisters callbacks for [ActivityTransition] events via a custom
     * [BroadcastReceiver]
     */
    private fun disableActivityTransitions() {
        Log.d(TAG, "disableActivityTransitions()")

        // Stop listening for activity changes.
        ActivityRecognition.getClient(this)
            .removeActivityTransitionUpdates(activityTransitionsPendingIntent!!)
            .addOnSuccessListener {
                activityTrackingEnabled = false
                printToScreen("Transitions successfully unregistered.")
            }
            .addOnFailureListener { e ->
                printToScreen("Transitions could not be unregistered: $e")
                Log.e(TAG, "Transitions could not be unregistered: $e")
            }
    }

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private fun activityRecognitionPermissionApproved(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
    }

    /**
     * Handles accelerometer sensor event
     */
    private fun handleAccelerometerSensorEvent(event: SensorEvent?) {

        val viewModel: AccelerometerCardViewModel by viewModels()

        if (event != null) {
            viewModel.data.value =
                AccelerometerDto(event.values[0], event.values[1], event.values[2])
        } else {
            viewModel.data.value = AccelerometerDto(0f, 0f, 0f)
        }
    }

    /**
     * Prints message to screen
     */
    private fun printToScreen(message: String) {
        mLogFragment?.logView?.println(message)
        Log.d(TAG, message)
    }

    //
    // Inner classes
    //

    /**
     * Handles intents from the Transitions API
     */
    inner class TransitionsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive(): $intent")
            if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.action)) {
                printToScreen(
                    "Received an unsupported action in TransitionsReceiver: action = " +
                            intent.action
                )
                return
            }

            if (ActivityTransitionResult.hasResult(intent)) {
                val result = ActivityTransitionResult.extractResult(intent)
                for (event in result!!.transitionEvents) {

                    val viewModel: ActivityTransitionViewModel by viewModels()
                    viewModel.data.value = ActivityTransitionDto(event.activityType, event.transitionType)

                    val info = "Transition: " + toActivityString(event.activityType) +
                            " (" + toTransitionType(event.transitionType) + ")" + "   " +
                            SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                    printToScreen(info)
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        /**
         * Converts activity to a string
         */
        private fun toActivityString(activity: Int): String {
            return when (activity) {
                DetectedActivity.STILL -> "STILL"
                DetectedActivity.WALKING -> "WALKING"
                else -> "UNKNOWN"
            }
        }

        /**
         * Converts transition type to string
         */
        private fun toTransitionType(transitionType: Int): String {
            return when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
                else -> "UNKNOWN"
            }
        }
    }
}
