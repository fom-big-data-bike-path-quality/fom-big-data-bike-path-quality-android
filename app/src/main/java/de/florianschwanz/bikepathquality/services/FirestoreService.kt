package de.florianschwanz.bikepathquality.services

import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import androidx.core.app.JobIntentService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityWithSamples
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData
import de.florianschwanz.bikepathquality.utils.InstantTypeConverter
import java.time.Instant

/**
 * Handles Firestore uploads
 */
class FirestoreService : JobIntentService() {

    /**
     * Handles work
     */
    override fun onHandleWork(intent: Intent) {

        val bikeActivity = gson.fromJson(
            intent.getStringExtra(EXTRA_BIKE_ACTIVITY),
            BikeActivityWithSamples::class.java
        )

        when (intent.action) {
            ACTION_UPLOAD_BIKE_ACTIVITY -> {
                val resultReceiver = intent.getParcelableExtra<ResultReceiver>(EXTRA_RECEIVER)
                val bundle = Bundle()
                val uid = bikeActivity.bikeActivity.uid.toString()
                val database = FirebaseFirestore.getInstance()

                database.collection("BikeActivities")
                    .document(uid).set(bikeActivity)
                    .addOnSuccessListener {
                        bundle.putString(EXTRA_BIKE_ACTIVITY_UID, uid)
                        resultReceiver?.send(RESULT_SUCCESS, bundle)
                    }
                    .addOnFailureListener { e ->
                        bundle.putString(EXTRA_ERROR_MESSAGE, e.toString())
                        resultReceiver?.send(RESULT_FAILURE, bundle)
                    }
            }
        }

    }

    companion object {

        const val EXTRA_RECEIVER = "extra.RECEIVER"
        const val EXTRA_BIKE_ACTIVITY = "extra.BIKE_ACTIVITY"
        const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"
        const val EXTRA_ERROR_MESSAGE = "extra.ERROR_MESSAGE"
        const val RESULT_SUCCESS = 0
        const val RESULT_FAILURE = 1

        private const val UPLOAD_JOB_ID = 1000
        private const val ACTION_UPLOAD_BIKE_ACTIVITY = "action.UPLOAD_DATA"

        private val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeConverter())
            .create()

        /**
         * Enqueues work for this service
         */
        fun enqueueWork(
            context: Context,
            bikeActivityWithSamples: BikeActivityWithSamples,
            userData: UserData,
            firestoreServiceResultReceiver: FirestoreServiceResultReceiver?
        ) {
            val intent = Intent(context, JobService::class.java)
            intent.putExtra(EXTRA_RECEIVER, firestoreServiceResultReceiver)
            intent.action = ACTION_UPLOAD_BIKE_ACTIVITY
            intent.putExtra(
                EXTRA_BIKE_ACTIVITY,
                gson.toJson(UploadEnvelope(bikeActivityWithSamples, userData))
            )

            enqueueWork(context, FirestoreService::class.java, UPLOAD_JOB_ID, intent)
        }
    }
}

data class UploadEnvelope(
    private val bikeActivityWithSamples: BikeActivityWithSamples,
    private val userData: UserData
)

class FirestoreServiceResultReceiver(handler: Handler?) : ResultReceiver(handler) {

    var receiver: Receiver? = null

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        receiver?.onReceiveFirestoreServiceResult(resultCode, resultData)
    }

    interface Receiver {
        fun onReceiveFirestoreServiceResult(resultCode: Int, resultData: Bundle?)
    }
}