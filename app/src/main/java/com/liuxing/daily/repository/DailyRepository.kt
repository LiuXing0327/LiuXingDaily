package com.liuxing.daily.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.liuxing.daily.database.DailyDatabase
import com.liuxing.daily.entity.DailyAudioEntity
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.entity.DailyVideoEntity

class DailyRepository(application: Application) {

    private val dailyDao = DailyDatabase.getDatabase(application).getDailyDao()
    private val dailyLabelDao = DailyDatabase.getDatabase(application).getDailyLabelDao()

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

    fun queryImageCount(): LiveData<Int> = dailyDao.queryImageCount()

    suspend fun insertDailyLabel(dailyLabelEntity: DailyLabelEntity) {
        dailyLabelDao.insertDailyLabel(dailyLabelEntity)
    }

    suspend fun deleteDailyLabel(dailyLabelEntity: DailyLabelEntity) {
        dailyLabelDao.deleteDailyLabel(dailyLabelEntity)
    }

    suspend fun updateDailyLabel(dailyLabelEntity: DailyLabelEntity) {
        dailyLabelDao.updateDailyLabel(dailyLabelEntity)
    }

    fun queryAllDailyLabel(): LiveData<List<DailyLabelEntity>> = dailyLabelDao.queryAllDailyLabel()

    fun queryDailyLabelToList():List<DailyLabelEntity> = dailyLabelDao.queryDailyLabelToList()

    suspend fun insertDailyVideoPath(dailyUUID: String, videoPathList: List<String>) =
        videoPathList.forEach { path ->
            val dailyVideoEntity = DailyVideoEntity(dailyUuid = dailyUUID, videoPath = path)
            dailyDao.insertDailyVideoPath(dailyVideoEntity)
        }

    fun queryDailyVideoByUuid(dailyUuid: String): LiveData<List<DailyVideoEntity>> =
        dailyDao.queryDailyVideoByUuid(dailyUuid)

    suspend fun queryDailyVideoByUuidToList(dailyUuid: String): List<DailyVideoEntity> =
        dailyDao.queryDailyVideoByUuidToList(dailyUuid)

    suspend fun deleteSelectVideoPath(videoPath: String) =
        dailyDao.deleteDailySelectPathVideo(videoPath)

    suspend fun deletePathVideoByDailyUuid(dailyUuid: String) =
        dailyDao.deletePathImageByDailyUuid(dailyUuid)

    suspend fun insertDailyAudioPath(dailyUuid: String, audioPathList: List<String>) =
        audioPathList.forEach { path ->
            val dailyAudioEntity = DailyAudioEntity(dailyUuid = dailyUuid, audioPath = path)
            dailyDao.insertDailyAudioPath(dailyAudioEntity)
        }

    suspend fun deleteSelectAudioPath(audioPath: String) =
        dailyDao.deleteDailySelectPathAudio(audioPath)

    suspend fun deletePathAudioByDailyUuid(dailyUuid: String) =
        dailyDao.deleteDailyAudioPathByDailyUuid(dailyUuid)

    fun queryDailyAudioByUuid(dailyUuid: String): LiveData<List<DailyAudioEntity>> =
        dailyDao.queryDailyAudioByUuid(dailyUuid)

    suspend fun queryDailyAudioByUuidToList(dailyUuid: String): List<DailyAudioEntity> =
        dailyDao.queryDailyAudioByUuidToList(dailyUuid)

    suspend fun getDailyByUUID(dailyUUID: String): DailyEntity? {
        return dailyDao.getDailyByUUID(dailyUUID)
    }

    suspend fun queryAllDailyImageEntity(): List<DailyImageEntity> {
        return dailyDao.queryAllDailyImageEntity()
    }

    suspend fun toggleIsDelete(uuids: List<String>) {
        dailyDao.toggleIsDelete(uuids)
    }
}