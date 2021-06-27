package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import java.time.Instant
import java.util.*

@Entity(
    foreignKeys = [ForeignKey(
        entity = BikeActivitySample::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("bike_activity_sample_uid"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class BikeActivityMeasurement(

    @PrimaryKey val uid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "bike_activity_sample_uid") val bikeActivitySampleUid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "timestamp") val timestamp: Instant = Instant.now(),
    @ColumnInfo(name = "lon") val lon: Double,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "speed") val speed: Float,
    @ColumnInfo(name = "accelerometer_x") val accelerometerX: Float,
    @ColumnInfo(name = "accelerometer_y") val accelerometerY: Float,
    @ColumnInfo(name = "accelerometer_z") val accelerometerZ: Float,
)
