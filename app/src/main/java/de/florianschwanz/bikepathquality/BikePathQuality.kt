package de.florianschwanz.bikepathquality

import android.app.Application
import de.florianschwanz.bikepathquality.storage.AppDatabase
import de.florianschwanz.bikepathquality.storage.LogEntryRepository

class BikePathQuality : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { LogEntryRepository(database.logEntryDao()) }
}