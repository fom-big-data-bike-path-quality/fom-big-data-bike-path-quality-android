package de.florianschwanz.bikepathquality.data.storage.bike_activity_sample

import androidx.room.Embedded
import androidx.room.Relation
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurement

data class BikeActivitySampleWithMeasurements(

    @Embedded val bikeActivitySample: BikeActivitySample,
    @Relation(
        parentColumn = "uid",
        entityColumn = "activity_sample_uid"
    )
    val bikeActivityMeasurements: List<BikeActivityMeasurement>
)
