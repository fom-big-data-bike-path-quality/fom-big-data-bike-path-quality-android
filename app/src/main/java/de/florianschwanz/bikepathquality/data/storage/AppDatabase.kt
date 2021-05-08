package de.florianschwanz.bikepathquality.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityDao
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurement
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurementDao
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryDao

@Database(entities = [LogEntry::class, BikeActivity::class, BikeActivityMeasurement::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logEntryDao(): LogEntryDao
    abstract fun bikeActivityDao(): BikeActivityDao
    abstract fun bikeActivityDetailDao(): BikeActivityMeasurementDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance

            instance

        }
    }
}