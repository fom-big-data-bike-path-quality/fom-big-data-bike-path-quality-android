package de.florianschwanz.bikepathquality.data.storage

import android.content.Context
import androidx.room.*
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityDao
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurement
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurementDao
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySample
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleDao
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryDao
import de.florianschwanz.bikepathquality.data.storage.user_data.UserData
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataDao

@Database(
    version = 3,
    entities = [LogEntry::class, BikeActivity::class, BikeActivitySample::class, BikeActivityMeasurement::class, UserData::class]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logEntryDao(): LogEntryDao
    abstract fun bikeActivityDao(): BikeActivityDao
    abstract fun bikeActivitySampleDao(): BikeActivitySampleDao
    abstract fun bikeActivityMeasurementDao(): BikeActivityMeasurementDao
    abstract fun userDataDao(): UserDataDao

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