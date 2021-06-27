package de.florianschwanz.bikepathquality.data.storage.log_entry

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*

@Entity
data class LogEntry(

    @PrimaryKey val uid: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "timestamp") val timestamp: Instant = Instant.now(),
    @ColumnInfo(name = "message") val message: String?
)
