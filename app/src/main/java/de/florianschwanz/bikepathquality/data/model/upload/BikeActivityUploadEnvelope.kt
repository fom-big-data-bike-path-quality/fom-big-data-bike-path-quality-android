package de.florianschwanz.bikepathquality.data.model.upload

import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleWithMeasurements
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData

/**
 * Represents an upload envelope for a bike activity
 */
data class BikeActivityUploadEnvelope(
    val bikeActivity: BikeActivity,
    val bikeActivitySamplesWithMeasurements: List<BikeActivitySampleWithMeasurements>,
    val userData: UserData,
)
