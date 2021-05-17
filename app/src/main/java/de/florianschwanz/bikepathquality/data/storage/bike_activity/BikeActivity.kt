package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*

enum class BikeActivityTrackingType {
    NONE,
    MANUAL,
    AUTOMATIC,
}

enum class BikeActivityStatus {
    LOCAL,
    SCHEDUELED_FOR_UPLOAD,
    UPLOADED
}

@Entity
data class BikeActivity(

    @PrimaryKey val uid: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "user_data_uid") val userDataUid: String,
    @ColumnInfo(name = "start_time") val startTime: Instant = Instant.now(),
    @ColumnInfo(name = "end_time") val endTime: Instant? = null,
    @ColumnInfo(name = "tracking_type") val trackingType: BikeActivityTrackingType? = BikeActivityTrackingType.NONE,
    @ColumnInfo(name = "upload_status") val uploadStatus: BikeActivityStatus = BikeActivityStatus.LOCAL,
    @ColumnInfo(name = "surface_type") val surfaceType: String? = null,
    @ColumnInfo(name = "smoothness_type") val smoothnessType: String? = null
)
