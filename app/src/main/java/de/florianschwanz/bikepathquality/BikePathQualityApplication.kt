package de.florianschwanz.bikepathquality

import android.app.Application
import de.florianschwanz.bikepathquality.storage.AppDatabase
import de.florianschwanz.bikepathquality.storage.BikeActivityRepository
import de.florianschwanz.bikepathquality.storage.LogEntryRepository

class BikePathQualityApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val logEntryRepository by lazy { LogEntryRepository(database.logEntryDao()) }
    val bikeActivitiesRepository by lazy { BikeActivityRepository(database.bikeActivityDao()) }
}