package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class BikeActivityRepository(private val bikeActivityDao: BikeActivityDao) {

    val bikeActivities: Flow<List<BikeActivity>> = bikeActivityDao.getAll()

    val bikeActivitiesWithSamples: Flow<List<BikeActivityWithSamples>> =
        bikeActivityDao.getAllWithSamples()

    val activeActivity: Flow<BikeActivity> = bikeActivityDao.getActive()

    val activeActivityWithSamples: Flow<BikeActivityWithSamples> = bikeActivityDao.getActiveWithSamples()

    fun singleBikeActivityWithSamples(uid: String): Flow<BikeActivityWithSamples> {
        return bikeActivityDao.getSingleWithSamples(uid)
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