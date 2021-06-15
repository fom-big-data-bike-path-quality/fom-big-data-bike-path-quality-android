package de.florianschwanz.bikepathquality.services

import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import androidx.core.app.JobIntentService
import com.google.firebase.firestore.FirebaseFirestore
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData

/**
 * Handles Firestore uploads
 */
class FirestoreService : JobIntentService() {

    /**
     * Handles work
     */
    override fun onHandleWork(intent: Intent) {

        when (intent.action) {
            ACTION_UPLOAD_BIKE_ACTIVITY -> {
                uploadEnvelope?.let { uploadEnvelope ->
                    val bundle = Bundle()

                    val bikeActivityUid = intent.getStringExtra(EXTRA_BIKE_ACTIVITY_UID)
                    val chunkIndex = intent.getStringExtra(EXTRA_CHUNK_INDEX)

                    val uid = "$bikeActivityUid-$chunkIndex"
                    val database = FirebaseFirestore.getInstance()

                    database.collection("BikeActivities")
                        .document(uid).set(uploadEnvelope)
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

    }

    companion object {

        const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"
        const val EXTRA_CHUNK_INDEX = "extra.CHUNK_INDEX"
        const val EXTRA_ERROR_MESSAGE = "extra.ERROR_MESSAGE"
        const val RESULT_SUCCESS = 0
        const val RESULT_FAILURE = 1

        private const val UPLOAD_JOB_ID = 1000
        private const val ACTION_UPLOAD_BIKE_ACTIVITY = "action.UPLOAD_DATA"

        private const val CHUNK_SIZE = 100

        private var uploadEnvelope: UploadEnvelope? = null
        private var resultReceiver: FirestoreServiceResultReceiver? = null

        /**
         * Enqueues work for this service
         */
        fun enqueueWork(
            context: Context,
            bikeActivity: BikeActivity,
            bikeActivitySamplesWithMeasurements: List<BikeActivitySampleWithMeasurements>,
            userData: UserData,
            firestoreServiceResultReceiver: FirestoreServiceResultReceiver?
        ) {
            var chunkIndex = 0
            bikeActivitySamplesWithMeasurements.chunked(CHUNK_SIZE) { bikeActivitySamplesWithMeasurementsChunk ->
                uploadEnvelope =
                    UploadEnvelope(bikeActivity, bikeActivitySamplesWithMeasurementsChunk, userData)
                resultReceiver = firestoreServiceResultReceiver

                val intent = Intent(context, JobService::class.java)
                intent.putExtra(EXTRA_BIKE_ACTIVITY_UID, bikeActivity.uid.toString())
                intent.putExtra(EXTRA_CHUNK_INDEX, chunkIndex.toString())
                intent.action = ACTION_UPLOAD_BIKE_ACTIVITY

                enqueueWork(context, FirestoreService::class.java, UPLOAD_JOB_ID, intent)
                chunkIndex++
            }
        }
    }
}

data class UploadEnvelope(
    val bikeActivity: BikeActivity,
    val bikeActivitySamplesWithMeasurements: List<BikeActivitySampleWithMeasurements>,
    val userData: UserData
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