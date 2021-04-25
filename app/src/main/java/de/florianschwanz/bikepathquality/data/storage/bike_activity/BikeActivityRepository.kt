package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class BikeActivityRepository(private val bikeActivityDao: BikeActivityDao) {

    val bikeActivities: Flow<List<BikeActivity>> = bikeActivityDao.getAll()

    val bikeActivitiesWithDetails: Flow<List<BikeActivityWithDetails>> =
        bikeActivityDao.getAllWithDetails()

    val activeActivity: Flow<BikeActivity> = bikeActivityDao.getActive()

    fun singleBikeActivityWithDetails(uid: String): Flow<BikeActivityWithDetails> {
        return bikeActivityDao.getSingleWithDetails(uid)
    }

    @WorkerThread
    suspend fun insert(bikeActivity: BikeActivity) {
        bikeActivityDao.insert(bikeActivity)
    }

    @WorkerThread
    suspend fun update(bikeActivity: BikeActivity) {
        bikeActivityDao.update(bikeActivity)
    }

    @WorkerThread
    suspend fun delete(bikeActivity: BikeActivity) {
        bikeActivityDao.delete(bikeActivity)
    }

    @WorkerThread
    suspend fun deleteAll() {
        bikeActivityDao.deleteAll()
    }
}