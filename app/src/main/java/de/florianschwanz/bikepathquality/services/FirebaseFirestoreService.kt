package de.florianschwanz.bikepathquality.services

import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import androidx.core.app.JobIntentService
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import de.florianschwanz.bikepathquality.data.model.upload.BikeActivityMetadataUploadEnvelope
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData

/**
 * Handles Firebase Firestore uploads
 */
class FirebaseFirestoreService : JobIntentService() {

    /**
     * Handles work
     */
    override fun onHandleWork(intent: Intent) {

        when (intent.action) {
            ACTION_UPLOAD_BIKE_ACTIVITY -> {
                val bundle = Bundle()

                val bikeActivityUid = intent.getStringExtra(EXTRA_BIKE_ACTIVITY_UID)
                val collection = intent.getStringExtra(EXTRA_COLLECTION)
                val documentUid = intent.getStringExtra(EXTRA_DOCUMENT_UID)
                val uploadEnvelope = uploadEnvelopes[documentUid]

                val database = Firebase.firestore

                database
                    .collection(collection!!)
                    .document(documentUid.toString()).set(uploadEnvelope!!)
                    .addOnSuccessListener {
                        bundle.putString(EXTRA_BIKE_ACTIVITY_UID, bikeActivityUid)
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

        const val EXTRA_BIKE_ACTIVITY_UID = "extra.BIKE_ACTIVITY_UID"
        const val EXTRA_COLLECTION = "extra.COLLECTION"
        const val EXTRA_DOCUMENT_UID = "extra.DOCUMENT_UID"
        const val EXTRA_ERROR_MESSAGE = "extra.ERROR_MESSAGE"
        const val RESULT_SUCCESS = 0
        const val RESULT_FAILURE = 1

        private const val UPLOAD_JOB_ID = 1001
        private const val ACTION_UPLOAD_BIKE_ACTIVITY = "action.UPLOAD_DATA"

        private var uploadEnvelopes = mutableMapOf<String, BikeActivityMetadataUploadEnvelope>()
        private var resultReceiver: FirebaseFirestoreServiceResultReceiver? = null

        /**
         * Enqueues work for this service
         */
        fun enqueueWork(
            context: Context,
            collection: String,
            bikeActivity: BikeActivity,
            bikeActivitySamples: List<BikeActivitySample>,
            userData: UserData,
            firebaseFirestoreServiceResultReceiver: FirebaseFirestoreServiceResultReceiver?,
            chunkSize: Int = 1_000
        ) {
            var chunkIndex = 0

            this.resultReceiver = firebaseFirestoreServiceResultReceiver
            bikeActivitySamples.chunked(chunkSize) { chunk ->
                val documentUid = if (bikeActivitySamples.size > chunkSize)
                    "${bikeActivity.uid}-${chunkIndex}" else bikeActivity.uid

                val uploadEnvelope = BikeActivityMetadataUploadEnvelope(
                    bikeActivity,
                    chunk.size,
                    buildBounds(chunk),
                    userData
                )

                uploadEnvelopes[documentUid] = uploadEnvelope

                val intent = Intent(context, JobService::class.java)
                intent.action = ACTION_UPLOAD_BIKE_ACTIVITY
                intent.putExtra(EXTRA_BIKE_ACTIVITY_UID, bikeActivity.uid)
                intent.putExtra(EXTRA_COLLECTION, collection)
                intent.putExtra(EXTRA_DOCUMENT_UID, documentUid)

                enqueueWork(context, FirebaseFirestoreService::class.java, UPLOAD_JOB_ID, intent)
                chunkIndex++
            }
        }

        /**
         * Enqueues work for this service
         */
        fun enqueueWork(
            context: Context,
            collection: String,
            bikeActivity: BikeActivity,
            bikeActivitySamples: List<BikeActivitySample>,
            userData: UserData,
            firebaseFirestoreServiceResultReceiver: FirebaseFirestoreServiceResultReceiver?,
        ) {
            this.resultReceiver = firebaseFirestoreServiceResultReceiver
            val documentUid = bikeActivity.uid
            val uploadEnvelope = BikeActivityMetadataUploadEnvelope(
                bikeActivity,
                bikeActivitySamples.size,
                buildBounds(bikeActivitySamples),
                userData
            )

            uploadEnvelopes[documentUid] = uploadEnvelope

            val intent = Intent(context, JobService::class.java)
            intent.action = ACTION_UPLOAD_BIKE_ACTIVITY
            intent.putExtra(EXTRA_BIKE_ACTIVITY_UID, bikeActivity.uid)
            intent.putExtra(EXTRA_COLLECTION, collection)
            intent.putExtra(EXTRA_DOCUMENT_UID, documentUid)

            enqueueWork(context, FirebaseFirestoreService::class.java, UPLOAD_JOB_ID, intent)
        }

        //
        // Helpers
        //

        /**
         * Creates bounds around bike activity samples
         */
        private fun buildBounds(bikeActivitySamples: List<BikeActivitySample>): LatLngBounds? {
            return if (bikeActivitySamples.filter { bikeActivitySample ->
                    bikeActivitySample.lon != 0.0 || bikeActivitySample.lat != 0.0
                }.size > 1) {
                val latLngBounds = LatLngBounds.Builder()

                bikeActivitySamples.filter { bikeActivitySample ->
                    bikeActivitySample.lon != 0.0 || bikeActivitySample.lat != 0.0
                }.forEach { bikeActivityDetail ->
                    latLngBounds.include(
                        LatLng(
                            bikeActivityDetail.lat,
                            bikeActivityDetail.lon
                        )
                    )
                }

                latLngBounds.build()
            } else null
        }
    }
}

class FirebaseFirestoreServiceResultReceiver(handler: Handler?) : ResultReceiver(handler) {

    var receiver: Receiver? = null

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        receiver?.onReceiveFirebaseFirestoreServiceResult(resultCode, resultData)
    }

    interface Receiver {
        fun onReceiveFirebaseFirestoreServiceResult(resultCode: Int, resultData: Bundle?)
    }
}