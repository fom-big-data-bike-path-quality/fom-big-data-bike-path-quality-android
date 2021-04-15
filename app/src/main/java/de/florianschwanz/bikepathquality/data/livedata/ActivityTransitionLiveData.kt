package de.florianschwanz.bikepathquality.data.livedata

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import de.florianschwanz.bikepathquality.BuildConfig
import de.florianschwanz.bikepathquality.data.model.ActivityTransitionModel

/**
 * Activity transition live data
 */
class ActivityTransitionLiveData(val context: Context) :
    LiveData<ActivityTransitionModel>() {

    /** Name of transition receiver action */
    private val TRANSITIONS_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"

    /** Activity transitions pending intent */
    private var activityTransitionsPendingIntent: PendingIntent? = null

    /** Transition receiver */
    private var transitionsReceiver: TransitionsReceiver? = null

    /** List of activity transitions to detect */
    private var activityTransitionList = mutableListOf<ActivityTransition>()

    init {
        initActivityTransitions()
        registerReceiver()
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
            ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.STILL,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.WALKING,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.WALKING,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.RUNNING,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.RUNNING,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.ON_BICYCLE,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.ON_BICYCLE,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )
        activityTransitionList.addTransition(
            DetectedActivity.IN_VEHICLE,
            ActivityTransition.ACTIVITY_TRANSITION_ENTER
        )
        activityTransitionList.addTransition(
            DetectedActivity.IN_VEHICLE,
            ActivityTransition.ACTIVITY_TRANSITION_EXIT
        )

        // Initialize PendingIntent that will be triggered when a activity transition occurs
        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        activityTransitionsPendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, 0)

        // The receiver listens for the PendingIntent above that is triggered by the system when an activity transition occurs
        transitionsReceiver = TransitionsReceiver()
    }

    private fun MutableList<ActivityTransition>.addTransition(
        activityType: Int,
        acvtivityTransition: Int
    ) = this.add(
        ActivityTransition.Builder()
            .setActivityType(activityType)
            .setActivityTransition(acvtivityTransition)
            .build()
    )

    private fun registerReceiver() {
        context.registerReceiver(transitionsReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))
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
                    value = ActivityTransitionModel(event.activityType, event.transitionType)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ActivityTransitionLiveData"
    }
}