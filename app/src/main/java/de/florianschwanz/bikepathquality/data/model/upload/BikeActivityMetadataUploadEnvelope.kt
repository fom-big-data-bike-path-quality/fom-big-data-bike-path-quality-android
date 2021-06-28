package de.florianschwanz.bikepathquality.data.model.upload

import com.mapbox.mapboxsdk.geometry.LatLngBounds
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData

/**
 * Represents an upload envelope for metadata ofa bike activity
 */
data class BikeActivityMetadataUploadEnvelope(
    val bikeActivity: BikeActivity,
    val bikeActivitySamples: Int = 0,
    val bikeActivityBounds: LatLngBounds?,
    val userData: UserData,
)
