package de.florianschwanz.bikepathquality

import android.app.Application
import de.florianschwanz.bikepathquality.storage.AppDatabase
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivityDetailRepository
import de.florianschwanz.bikepathquality.storage.bike_activity.BikeActivityRepository
import de.florianschwanz.bikepathquality.storage.log_entry.LogEntryRepository

class BikePathQualityApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val logEntryRepository by lazy { LogEntryRepository(database.logEntryDao()) }
    val bikeActivitiesRepository by lazy { BikeActivityRepository(database.bikeActivityDao()) }
    val bikeActivityDetailsRepository by lazy { BikeActivityDetailRepository(database.bikeActivityDetailDao()) }
}