package de.florianschwanz.bikepathquality.data.storage

import androidx.room.TypeConverter
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityStatus
import java.time.Instant
import java.util.*


class Converters {

    @TypeConverter
    fun fromUUID(uuid: UUID): String = uuid.toString()

    @TypeConverter
    fun uuidFromString(string: String?): UUID = UUID.fromString(string)

    @TypeConverter
    fun calendarToDatestamp(calendar: Calendar): Long = calendar.timeInMillis

    @TypeConverter
    fun datestampToCalendar(value: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = value }

    @TypeConverter
    fun instantToDatestamp(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun datestampToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toBikeActivityStatus(value: String) = enumValueOf<BikeActivityStatus>(value)

    @TypeConverter
    fun fromBikeActivityStatus(value: BikeActivityStatus) = value.name
}