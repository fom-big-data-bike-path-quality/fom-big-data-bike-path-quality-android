package de.florianschwanz.bikepathquality.data.storage.bike_activity_sample

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import java.time.Instant
import java.util.*

@Entity(
    foreignKeys = [ForeignKey(
        entity = BikeActivity::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("bike_activity_uid"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class BikeActivitySample(

    @PrimaryKey val uid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "bike_activity_uid") val bikeActivityUid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "timestamp") val timestamp: Instant = Instant.now(),
    @ColumnInfo(name = "lon") val lon: Double,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "speed") val speed: Float,
    @ColumnInfo(name = "surface_type") val surfaceType: String? = null,
    @ColumnInfo(name = "smoothness_type") val smoothnessType: String? = null
)
