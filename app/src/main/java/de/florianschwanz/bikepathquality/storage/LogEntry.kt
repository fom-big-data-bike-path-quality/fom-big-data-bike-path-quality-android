package de.florianschwanz.bikepathquality.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class LogEntry(

    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Calendar?,
    @ColumnInfo(name = "message") val message: String?,
)
