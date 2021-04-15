package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*

@Entity
data class BikeActivity(

    @PrimaryKey val uid: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "start_time") val startTime: Instant = Instant.now(),
    @ColumnInfo(name = "end_time") val endTime: Instant? = null
)
