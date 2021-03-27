package de.florianschwanz.bikepathquality

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import de.florianschwanz.bikepathquality.logger.LogFragment
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main activity
 */
class MainActivity : AppCompatActivity() {

    private var activityTrackingEnabled = false
    private var activityTransitionList = mutableListOf<ActivityTransition>()

    // Action fired when transitions are triggered
    private val TRANSITIONS_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"
    private var activityTransitionsPendingIntent: PendingIntent? = null
    private var transitionsReceiver: MainActivity.TransitionsReceiver? = null
    private var mLogFragment: LogFragment? = null

    //
    // Lifecycle phases
    //

    /**
     * Handles on-create lifecycle phase
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        mLogFragment = supportFragmentManager.findFragmentById(R.id.log_fragment) as LogFragment?
        activityTrackingEnabled = false

        // Add activity transitions to track
        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        activityTransitionList.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )

        // Initialize PendingIntent that will be triggered when a activity transition occurs
        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        activityTransitionsPendingIntent =
            PendingIntent.getBroadcast(this@MainActivity, 0, intent, 0)

        // The receiver listens for the PendingIntent above that is triggered by the system when an activity transition occurs
        transitionsReceiver = TransitionsReceiver()

        printToScreen("App initialized.")
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
     * Handles on-pause lifecycle phase
     */
    override fun onPause() {

        // Disable activity transitions when user leaves the app
        if (activityTrackingEnabled) {
            disableActivityTransitions()
        }
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
            .requestActivityTransitionUpdates(request, activityTransitionsPendingIntent)

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
            .removeActivityTransitionUpdates(activityTransitionsPendingIntent)
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
