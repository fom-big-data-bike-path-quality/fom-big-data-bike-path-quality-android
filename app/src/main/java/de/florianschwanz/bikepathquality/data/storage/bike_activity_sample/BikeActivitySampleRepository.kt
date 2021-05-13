package de.florianschwanz.bikepathquality.data.storage.bike_activity_sample

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class BikeActivitySampleRepository(private val bikeActivitySampleDao: BikeActivitySampleDao) {

    val bikeActivitySamples: Flow<List<BikeActivitySample>> = bikeActivitySampleDao.getAll()

    val bikeActivitySamplesWithMeasurements: Flow<List<BikeActivitySampleWithMeasurements>> =
        bikeActivitySampleDao.getAllWithMeasurements()

    fun singleBikeActivitySampleWithMeasurements(uid: String): Flow<BikeActivitySampleWithMeasurements> {
        return bikeActivitySampleDao.getSingleWithMeasurements(uid)
    }

    @WorkerThread
    suspend fun insert(bikeActivitySample: BikeActivitySample) {
        bikeActivitySampleDao.insert(bikeActivitySample)
    }

    @WorkerThread
    suspend fun update(bikeActivitySample: BikeActivitySample) {
        bikeActivitySampleDao.update(bikeActivitySample)
    }

    @WorkerThread
    suspend fun deleteAll() {
        bikeActivitySampleDao.deleteAll()
    }
}
