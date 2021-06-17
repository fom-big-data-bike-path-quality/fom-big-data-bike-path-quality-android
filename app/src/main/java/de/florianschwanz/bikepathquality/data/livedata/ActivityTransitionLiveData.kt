package de.florianschwanz.bikepathquality.data.livedata

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import de.florianschwanz.bikepathquality.BuildConfig
import de.florianschwanz.bikepathquality.data.model.tracking.ActivityTransition

/**
 * Activity transition live data
 */
class ActivityTransitionLiveData(val context: Context) :
    LiveData<ActivityTransition>() {

    /** Activity transitions pending intent */
    private var activityTransitionsPendingIntent: PendingIntent? = null

    /** Transition receiver */
    private var transitionsReceiver: TransitionsReceiver? = null

    /** List of activity transitions to detect */
    private var activityTransitionList =
        mutableListOf<com.google.android.gms.location.ActivityTransition>()

    init {
        initActivityTransitions()
    }

    //
    // Lifecycle phases
    //

    /**
     * Handles activity
     */
    override fun onActive() {
        super.onActive()
        ActivityRecognition.getClient(context)
            .requestActivityTransitionUpdates(
                ActivityTransitionRequest(activityTransitionList),
                activityTransitionsPendingIntent!!
            )
            .addOnSuccessListener {
                Log.i(TAG, "Transitions Api registered")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Transitions Api could NOT be registered: $e")
            }

        registerReceiver()
    }

    /**
     * Handles inactivity
     */
    override fun onInactive() {
        super.onInactive()
        ActivityRecognition.getClient(context)
            .removeActivityTransitionUpdates(activityTransitionsPendingIntent!!)
            .addOnSuccessListener {
                Log.i(TAG, "Transitions Api unregistered")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Transitions could not be unregistered: $e")
            }

        unregisterReceiver()
    }

    //
    // Helpers
    //

    /**
     * Initializes activity transitions
     */
    private fun initActivityTransitions() {

        // Add activity transitions to track
        activityTransitionList.addTransition(
            DetectedActivity.STILL,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.STILL,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.WALKING,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.WALKING,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.RUNNING,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.RUNNING,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.ON_BICYCLE,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.ON_BICYCLE,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.IN_VEHICLE,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.IN_VEHICLE,
            com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )

        // Initialize PendingIntent that will be triggered when a activity transition occurs
        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        activityTransitionsPendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, 0)

        // The receiver listens for the PendingIntent above that is triggered by the system when an activity transition occurs
        transitionsReceiver = TransitionsReceiver()
    }

    private fun MutableList<com.google.android.gms.location.ActivityTransition>.addTransition(
        activityType: Int,
        acvtivityTransition: Int
    ) = this.add(
        com.google.android.gms.location.ActivityTransition.Builder()
            .setActivityType(activityType)
            .setActivityTransition(acvtivityTransition)
            .build()
    )

    private fun registerReceiver() {
        context.registerReceiver(transitionsReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))
    }

    private fun unregisterReceiver() {
        context.unregisterReceiver(transitionsReceiver)
    }

    //
    // Inner classes
    //

    /**
     * Handles intents from the Transitions API
     */
    inner class TransitionsReceiver : BroadcastReceiver() {

        /**
         * Handles transition value change
         */
        override fun onReceive(context: Context, intent: Intent) {
            if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.action)) {
                Log.e(
                    TAG,
                    "Received an unsupported action in TransitionsReceiver: action = ${intent.action}"
                )
                return
            }

            if (ActivityTransitionResult.hasResult(intent)) {
                val result = ActivityTransitionResult.extractResult(intent)
                for (event in result!!.transitionEvents) {
                    value =
                        ActivityTransition(
                            event.activityType,
                            event.transitionType
                        )
                }
            }
        }
    }

    companion object {
        private const val TAG = "ActivityTransitionLiveData"
        private const val TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"
    }
}