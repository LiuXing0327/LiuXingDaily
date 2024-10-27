package com.liuxing.daily.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.liuxing.daily.database.DailyDatabase
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity

class DailyRepository(application: Application) {

    private val dailyDao = DailyDatabase.getDatabase(application).getDailyDao()

    suspend fun insertDaily(dailyEntity: DailyEntity) =
        dailyDao.insertDaily(dailyEntity)

    suspend fun deleteDaily(dailyEntity: DailyEntity) =
        dailyDao.deleteDaily(dailyEntity)

    suspend fun updateDaily(dailyEntity: DailyEntity) =
        dailyDao.updateDaily(dailyEntity)

    fun queryAllDaily(): LiveData<List<DailyEntity>> = dailyDao.queryAllDaily()

    suspend fun clearDaily() = dailyDao.clearDaily()

    suspend fun insertDailyImagePath(dailyUUID: String, imagePathList: List<String>) =
        imagePathList.forEach { path ->
            val dailyImageEntity =
                DailyImageEntity(dailyUuid = dailyUUID, imagePath = path)
            dailyDao.insertDailyImagePath(dailyImageEntity)
        }

    fun queryDailyImageByUuid(dailyUuid: String): LiveData<List<DailyImageEntity>> =
        dailyDao.queryDailyImageByUuid(dailyUuid)

    suspend fun queryDailyImageByUuidToList(dailyUuid: String): List<DailyImageEntity> =
        dailyDao.queryDailyImageByUuidToList(dailyUuid)

    suspend fun deleteSelectPathImage(imagePath: String) = dailyDao.deleteSelectPathImage(imagePath)

    suspend fun deletePathImageByDailyUuid(dailyUuid: String) =
        dailyDao.deletePathImageByDailyUuid(dailyUuid)

    suspend fun clearImagePath() = dailyDao.clearDailyImage()
}