package de.florianschwanz.bikepathquality.storage.log_entry

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

@Entity
data class LogEntry(

    @PrimaryKey val uid: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "timestamp") val timestamp: Instant = Instant.now(),
    @ColumnInfo(name = "message") val message: String?
)
