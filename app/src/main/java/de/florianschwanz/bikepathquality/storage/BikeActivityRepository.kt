package de.florianschwanz.bikepathquality.storage

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class BikeActivityRepository(private val bikeActivityDao: BikeActivityDao) {

    val bikeActivities: Flow<List<BikeActivity>> = bikeActivityDao.getAll()

    val activeActivities: Flow<List<BikeActivity>> = bikeActivityDao.getAllActive()

    @WorkerThread
    suspend fun insert(bikeActivity: BikeActivity) {
        bikeActivityDao.insert(bikeActivity)
    }

    @WorkerThread
    suspend fun update(bikeActivity: BikeActivity) {
        bikeActivityDao.update(bikeActivity)
    }

    @WorkerThread
    suspend fun deleteAll() {
        bikeActivityDao.deleteAll()
    }
}