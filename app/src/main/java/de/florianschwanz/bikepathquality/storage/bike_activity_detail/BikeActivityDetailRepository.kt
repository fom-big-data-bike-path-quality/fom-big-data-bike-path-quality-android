package de.florianschwanz.bikepathquality.storage.bike_activity

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class BikeActivityDetailRepository(private val bikeActivityDetailDao: BikeActivityDetailDao) {

    val bikeActivities: Flow<List<BikeActivityDetail>> = bikeActivityDetailDao.getAll()

    @WorkerThread
    suspend fun insert(bikeActivityDetail: BikeActivityDetail) {
        bikeActivityDetailDao.insert(bikeActivityDetail)
    }

    @WorkerThread
    suspend fun update(bikeActivityDetail: BikeActivityDetail) {
        bikeActivityDetailDao.update(bikeActivityDetail)
    }

    @WorkerThread
    suspend fun deleteAll() {
        bikeActivityDetailDao.deleteAll()
    }
}