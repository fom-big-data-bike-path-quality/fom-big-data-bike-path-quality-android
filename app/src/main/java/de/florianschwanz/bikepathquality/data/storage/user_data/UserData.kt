package de.florianschwanz.bikepathquality.data.storage.user_data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*

@Entity
data class UserData(

    @PrimaryKey val uid: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "creation_time") val startTime: Instant = Instant.now(),
)
