package de.florianschwanz.bikepathquality.data.storage.user_data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class UserDataRepository(private val userDataDao: UserDataDao) {

    fun exists(): Flow<Boolean> {
        return userDataDao.exists()
    }

    fun singleUserData(): Flow<UserData> {
        return userDataDao.getSingle()
    }

    @WorkerThread
    suspend fun insert(userData: UserData) {
        userDataDao.insert(userData)
    }

    @WorkerThread
    suspend fun update(userData: UserData) {
        userDataDao.update(userData)
    }

    @WorkerThread
    suspend fun delete(userData: UserData) {
        userDataDao.delete(userData)
    }
}