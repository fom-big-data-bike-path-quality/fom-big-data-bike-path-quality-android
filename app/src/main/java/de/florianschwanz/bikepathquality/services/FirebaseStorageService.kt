package de.florianschwanz.bikepathquality.services

import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import androidx.core.app.JobIntentService
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import de.florianschwanz.bikepathquality.R
import de.florianschwanz.bikepathquality.data.model.upload.BikeActivityUploadEnvelope
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData
import de.florianschwanz.bikepathquality.utils.InstantTypeConverter
import de.florianschwanz.bikepathquality.utils.UuidTypeConverter
import java.time.Instant
import java.util.*

/**
 * Handles Firebase Storage uploads
 */
class FirebaseStorageService : JobIntentService() {

    lateinit var storage: FirebaseStorage

    /**
     * Handles work
     */
    override fun onHandleWork(intent: Intent) {

        when (intent.action) {
            ACTION_UPLOAD_BIKE_ACTIVITY -> {
                val bundle = Bundle()

                val bikeActivityUid = intent.getStringExtra(EXTRA_BIKE_ACTIVITY_UID)
                val documentUid = intent.getStringExtra(EXTRA_DOCUMENT_UID)
                val uploadEnvelope = uploadEnvelopes[documentUid]

                val storage = Firebase.storage(resources.getString(R.string.firebase_storage_bucket_url))
                val storageRef = storage.reference
                val bikeActivityRef = storageRef.child("measurements").child("json").child("$documentUid.json")

                val uploadEnvelopeJson = gson.toJson(uploadEnvelope)
                val uploadTask = bikeActivityRef.putBytes(uploadEnvelopeJson.toByteArray())

                uploadTask
                    .addOnSuccessListener {
                        bundle.putString(EXTRA_BIKE_ACTIVITY_UID, bikeActivityUid)
                        resultReceiver?.send(RESULT_SUCCESS, bundle)
                    }
                    .addOnFailureListener { e: Exception ->
                        bundle.putString(EXTRA_ERROR_MESSAGE, e.toString())
                        resultReceiver?.send(RESULT_FAILURE, bundle)
                    }
            }
        }

    }

    companion object {

        const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"
        const val EXTRA_DOCUMENT_UID = "extra.DOCUMENT_UID"
        const val EXTRA_ERROR_MESSAGE = "extra.ERROR_MESSAGE"
        const val RESULT_SUCCESS = 0
        const val RESULT_FAILURE = 1

        private const val UPLOAD_JOB_ID = 1000
        private const val ACTION_UPLOAD_BIKE_ACTIVITY = "action.UPLOAD_DATA"

        private var uploadEnvelopes = mutableMapOf<String, BikeActivityUploadEnvelope>()
        private var resultReceiver: FirebaseStorageServiceResultReceiver? = null

        private val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeConverter())
            .registerTypeAdapter(UUID::class.java, UuidTypeConverter())
            .create()

        /**
         * Enqueues work for this service
         */
        fun enqueueWork(
            context: Context,
            bikeActivity: BikeActivity,
            bikeActivitySamplesWithMeasurements: List<BikeActivitySampleWithMeasurements>,
            userData: UserData,
            firebaseStorageServiceResultReceiver: FirebaseStorageServiceResultReceiver?,
            chunkSize: Int = 1_000
        ) {
            var chunkIndex = 0

            resultReceiver = firebaseStorageServiceResultReceiver
            bikeActivitySamplesWithMeasurements.chunked(chunkSize) { bikeActivitySamplesWithMeasurementsChunk ->
                val documentUid = if (bikeActivitySamplesWithMeasurements.size > chunkSize)
                    "${bikeActivity.uid}-${chunkIndex}" else bikeActivity.uid.toString()
                val uploadEnvelope = BikeActivityUploadEnvelope(
                    bikeActivity,
                    bikeActivitySamplesWithMeasurementsChunk,
                    userData
                )

                uploadEnvelopes[documentUid] = uploadEnvelope

                val intent = Intent(context, JobService::class.java)
                intent.action = ACTION_UPLOAD_BIKE_ACTIVITY
                intent.putExtra(EXTRA_BIKE_ACTIVITY_UID, bikeActivity.uid.toString())
                intent.putExtra(EXTRA_DOCUMENT_UID, documentUid)

                enqueueWork(context, FirebaseStorageService::class.java, UPLOAD_JOB_ID, intent)
                chunkIndex++
            }
        }
    }
}

class FirebaseStorageServiceResultReceiver(handler: Handler?) : ResultReceiver(handler) {

    var receiver: Receiver? = null

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        receiver?.onReceiveFirebaseStorageServiceResult(resultCode, resultData)
    }

    interface Receiver {
        fun onReceiveFirebaseStorageServiceResult(resultCode: Int, resultData: Bundle?)
    }
}