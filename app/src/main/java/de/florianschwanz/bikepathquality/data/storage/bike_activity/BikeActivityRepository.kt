package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class BikeActivityRepository(private val bikeActivityDao: BikeActivityDao) {

    val bikeActivities: Flow<List<BikeActivity>> = bikeActivityDao.getAll()

    val bikeActivitiesWithDetails: Flow<List<BikeActivityWithDetails>> =
        bikeActivityDao.getAllWithDetails()

    val activeActivities: Flow<List<BikeActivity>> = bikeActivityDao.getAllActive()

    val activeActivity: Flow<BikeActivity> = bikeActivityDao.getActive()

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