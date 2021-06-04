package de.florianschwanz.bikepathquality

import android.app.Application
import de.florianschwanz.bikepathquality.data.storage.AppDatabase
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityMeasurementRepository
import de.florianschwanz.bikepathquality.data.storage.bike_activity.BikeActivityRepository
import de.florianschwanz.bikepathquality.data.storage.bike_activity_sample.BikeActivitySampleRepository
import de.florianschwanz.bikepathquality.data.storage.log_entry.LogEntryRepository
import de.florianschwanz.bikepathquality.data.storage.user_data.UserDataRepository

class BikePathQualityApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val logEntryRepository by lazy { LogEntryRepository(database.logEntryDao()) }
    val bikeActivityRepository by lazy { BikeActivityRepository(database.bikeActivityDao()) }
    val bikeActivitySampleRepository by lazy { BikeActivitySampleRepository(database.bikeActivitySampleDao()) }
    val bikeActivityMeasurementRepository by lazy { BikeActivityMeasurementRepository(database.bikeActivityMeasurementDao()) }
    val userDataRepository by lazy { UserDataRepository(database.userDataDao()) }
}