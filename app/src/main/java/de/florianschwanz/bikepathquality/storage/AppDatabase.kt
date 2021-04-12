package de.florianschwanz.bikepathquality.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivity
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivityDao
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivityDetail
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivityDetailDao
import de.florianschwanz.bikepathquality.storage.log_entry.LogEntry
import de.florianschwanz.bikepathquality.storage.log_entry.LogEntryDao

@Database(entities = [LogEntry::class, BikeActivity::class, BikeActivityDetail::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logEntryDao(): LogEntryDao
    abstract fun bikeActivityDao(): BikeActivityDao
    abstract fun bikeActivityDetailDao(): BikeActivityDetailDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
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
}