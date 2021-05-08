package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.Embedded
import androidx.room.Relation
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample

data class BikeActivityWithSamples(

    @Embedded val bikeActivity: BikeActivity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "bike_activity_uid"
    )
    val bikeActivitySamples: List<BikeActivitySample>
)
