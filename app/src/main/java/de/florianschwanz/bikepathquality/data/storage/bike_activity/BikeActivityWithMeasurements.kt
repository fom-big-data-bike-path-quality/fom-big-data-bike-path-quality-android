package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.Embedded
import androidx.room.Relation

data class BikeActivityWithMeasurements(

    @Embedded val bikeActivity: BikeActivity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "activity_uid"
    )
    val bikeActivityMeasurements: List<BikeActivityMeasurement>
)
