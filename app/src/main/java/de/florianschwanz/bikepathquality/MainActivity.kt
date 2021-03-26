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
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import de.florianschwanz.bikepathquality.logger.LogFragment
import java.util.*


class MainActivity : AppCompatActivity() {

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private var activityTrackingEnabled = false
    private var activityTransitionList: List<ActivityTransition>? = null

    // Action fired when transitions are triggered.
    private val TRANSITIONS_RECEIVER_ACTION: String =
        BuildConfig.APPLICATION_ID.toString() + "TRANSITIONS_RECEIVER_ACTION"
    private val mActivityTransitionsPendingIntent: PendingIntent? = null
    private var mTransitionsReceiver: de.florianschwanz.bikepathquality.MainActivity.TransitionsReceiver? =
        null
    private var mLogFragment: LogFragment? = null

    //
    // Lifecycle phases
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        mLogFragment = supportFragmentManager.findFragmentById(R.id.log_fragment) as LogFragment?
        activityTrackingEnabled = false

        // List of activity transitions to track.
        activityTransitionList = ArrayList()

        // TODO: Add activity transitions to track.


        // TODO: Initialize PendingIntent that will be triggered when a activity transition occurs.


        // The receiver listens for the PendingIntent above that is triggered by the system when an activity transition occurs.
        mTransitionsReceiver = TransitionsReceiver()

        printToScreen("App initialized.")
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(mTransitionsReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION));
    }

    override fun onPause() {

        // TODO: Disable activity transitions when user leaves the app.


        super.onPause()
    }

    override fun onStop() {
        unregisterReceiver(mTransitionsReceiver);
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Start activity recognition if the permission was approved.
        if (activityRecognitionPermissionApproved() && !activityTrackingEnabled) {
            enableActivityTransitions()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //
    // Actions
    //

    fun onClickEnableOrDisableActivityRecognition(view: View?) {

        if (activityRecognitionPermissionApproved()) {
            if (activityTrackingEnabled) {
                disableActivityTransitions()
            } else {
                enableActivityTransitions()
            }
        } else {
            // Request permission and start activity for result. If the permission is approved, we
            // want to make sure we start activity recognition tracking.
            val startIntent = Intent(this, PermissionRationalActivity::class.java)
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


        // TODO: Create request and listen for activity changes.
    }

    /**
     * Unregisters callbacks for [ActivityTransition] events via a custom
     * [BroadcastReceiver]
     */
    private fun disableActivityTransitions() {
        Log.d(TAG, "disableActivityTransitions()")


        // TODO: Stop listening for activity changes.
    }

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private fun activityRecognitionPermissionApproved(): Boolean {

        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
    }

    /**
     * Prints message to screen
     */
    private fun printToScreen(message: String) {
        mLogFragment?.getLogView()?.println(message)
        Log.d(TAG, message)
    }

    //
    // Inner classes
    //

    /**
     * Handles intents from from the Transitions API.
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

            // TODO: Extract activity transition information from listener.
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private fun toActivityString(activity: Int): String {
            return when (activity) {
                DetectedActivity.STILL -> "STILL"
                DetectedActivity.WALKING -> "WALKING"
                else -> "UNKNOWN"
            }
        }

        private fun toTransitionType(transitionType: Int): String {
            return when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
                else -> "UNKNOWN"
            }
        }
    }
}
