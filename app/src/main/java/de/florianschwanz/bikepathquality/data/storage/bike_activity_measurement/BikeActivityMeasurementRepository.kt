package de.florianschwanz.bikepathquality.data.storage.bike_activity

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class BikeActivityMeasurementRepository(private val bikeActivityMeasurementDao: BikeActivityMeasurementDao) {

    val bikeActivities: Flow<List<BikeActivityMeasurement>> = bikeActivityMeasurementDao.getAll()

    @WorkerThread
    suspend fun insert(bikeActivityMeasurement: BikeActivityMeasurement) {
        bikeActivityMeasurementDao.insert(bikeActivityMeasurement)
    }

    @WorkerThread
    suspend fun update(bikeActivityMeasurement: BikeActivityMeasurement) {
        bikeActivityMeasurementDao.update(bikeActivityMeasurement)
    }

    @WorkerThread
    suspend fun deleteAll() {
        bikeActivityMeasurementDao.deleteAll()
    }
}