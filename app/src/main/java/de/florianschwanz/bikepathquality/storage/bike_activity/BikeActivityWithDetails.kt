package de.florianschwanz.bikepathquality.storage.bike_activity

import androidx.room.Embedded
import androidx.room.Relation

data class BikeActivityWithDetails(

    @Embedded val bikeActivity: BikeActivity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "activity_uid"
    )
    val bikeActivityDetails: List<BikeActivityDetail>
)
