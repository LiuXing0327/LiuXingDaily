package com.liuxing.daily.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.liuxing.daily.entity.DailyAudioEntity
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.entity.DailyVideoEntity
import com.liuxing.daily.repository.DailyRepository
import com.liuxing.daily.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DailyViewModel(application: Application) : AndroidViewModel(application) {

    private val dailyRepository: DailyRepository = DailyRepository(application)

    fun insertDaily(dailyEntity: DailyEntity) {
        viewModelScope.launch {
            dailyRepository.insertDaily(dailyEntity)
        }
    }

    fun deleteDaily(dailyEntity: DailyEntity) {
        viewModelScope.launch {
            dailyRepository.deleteDaily(dailyEntity)
        }
    }

    fun updateDaily(dailyEntity: DailyEntity) {
        viewModelScope.launch {
            dailyRepository.updateDaily(dailyEntity)
        }
    }

    fun queryAllDaily(): LiveData<List<DailyEntity>> = dailyRepository.queryAllDaily()

    fun clearDaily() = viewModelScope.launch {
        dailyRepository.clearDaily()
    }

    fun insertDailyImagePath(dailyUUID: String, imagePathList: List<String>) =
        viewModelScope.launch {
            dailyRepository.insertDailyImagePath(
                dailyUUID = dailyUUID,
                imagePathList = imagePathList
            )
        }

    fun queryDailyImageByUuid(dailyUuid: String): LiveData<List<DailyImageEntity>> =
        dailyRepository.queryDailyImageByUuid(dailyUuid)


    fun deleteSelectPathImage(imagePath: String) = viewModelScope.launch {
        dailyRepository.deleteSelectPathImage(imagePath)
    }

    fun deletePathImageByDailyUuid(dailyUuid: String) = viewModelScope.launch(Dispatchers.IO) {
        dailyRepository.deletePathImageByDailyUuid(dailyUuid)
    }

    fun clearClearImagePath() = viewModelScope.launch {
        dailyRepository.clearImagePath()
    }

    suspend fun queryDailyImageByUuidToList(dailyUuid: String): List<DailyImageEntity> =
        dailyRepository.queryDailyImageByUuidToList(dailyUuid)

    fun queryImageCount(): LiveData<Int> = dailyRepository.queryImageCount()

    fun insertDailyLabel(dailyLabelEntity: DailyLabelEntity) = viewModelScope.launch {
        dailyRepository.insertDailyLabel(dailyLabelEntity)
    }

    fun deleteDailyLabel(dailyLabelEntity: DailyLabelEntity) = viewModelScope.launch {
        dailyRepository.deleteDailyLabel(dailyLabelEntity)
    }

    fun updateDailyLabel(dailyLabelEntity: DailyLabelEntity) = viewModelScope.launch {
        dailyRepository.updateDailyLabel(dailyLabelEntity)
    }

    fun queryAllDailyLabel(): LiveData<List<DailyLabelEntity>> =
        dailyRepository.queryAllDailyLabel()

    fun queryDailyLabelToList():List<DailyLabelEntity> = dailyRepository.queryDailyLabelToList()

    fun insertDailyVideoPath(dailyUUID: String, videoPathList: List<String>) =
        viewModelScope.launch {
            dailyRepository.insertDailyVideoPath(dailyUUID, videoPathList)
        }

    fun queryDailyVideoByUuid(dailyUuid: String): LiveData<List<DailyVideoEntity>> =
        dailyRepository.queryDailyVideoByUuid(dailyUuid)

    fun deleteSelectPathVideo(videoPath: String) = viewModelScope.launch {
        dailyRepository.deleteSelectVideoPath(videoPath)
    }

    fun deletePathVideoByDailyUuid(videoPath: String) = viewModelScope.launch(Dispatchers.IO) {
        dailyRepository.deletePathVideoByDailyUuid(videoPath)
    }

    suspend fun queryDailyVideoByUuidToList(dailyUuid: String): List<DailyVideoEntity> =
        dailyRepository.queryDailyVideoByUuidToList(dailyUuid)

    fun insertDailyAudioPath(dailyUuid: String, audioPathList: List<String>) =
        viewModelScope.launch {
            dailyRepository.insertDailyAudioPath(dailyUuid, audioPathList)
        }

    fun queryDailyAudioByUuid(dailyUuid: String): LiveData<List<DailyAudioEntity>> =
        dailyRepository.queryDailyAudioByUuid(dailyUuid)

    suspend fun queryDailyAudioByUuidToList(dailyUuid: String): List<DailyAudioEntity> =
        dailyRepository.queryDailyAudioByUuidToList(dailyUuid)

    fun deleteSelectPathAudio(audioPath: String) = viewModelScope.launch {
        dailyRepository.deleteSelectAudioPath(audioPath)
    }

    fun deletePathAudioByDailyUuid(dailyUuid: String) = viewModelScope.launch(Dispatchers.IO) {
        dailyRepository.deletePathAudioByDailyUuid(dailyUuid)
    }

    suspend fun checkDailyExists(dailyUUID: String): Boolean {
        return dailyRepository.getDailyByUUID(dailyUUID) != null
    }

    suspend fun queryAllDailyImageEntity(): List<DailyImageEntity> = dailyRepository.queryAllDailyImageEntity()

    suspend fun getImagePathForUuids(uuids: List<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        uuids.forEach { uuid ->
            val images = dailyRepository.queryDailyImageByUuidToList(uuid)
            val image = images.firstOrNull()
            if (image != null && FileUtil().checkFileExists(image.imagePath!!)) {
                result[uuid] = image.imagePath!!
            }
        }

        return result
    }

    suspend fun toggleIsDelete(uuids: List<String>) = withContext(Dispatchers.IO) {
        dailyRepository.toggleIsDelete(uuids)
    }

    suspend fun deleteSelected(selectedList: List<DailyEntity>) = withContext(Dispatchers.IO) {
        val fileUtil = FileUtil()
            selectedList.forEach { dailyEntity ->
                val uuid = dailyEntity.dailyUUID ?: return@forEach
                queryDailyImageByUuidToList(uuid).mapNotNull { it.imagePath }
                    .forEach { path ->
                        if (fileUtil.checkFileExists(path)) fileUtil.deleteFile(path)
                    }
                deletePathImageByDailyUuid(uuid)

                queryDailyVideoByUuidToList(uuid).mapNotNull { it.videoPath }
                    .forEach { path ->
                        if (fileUtil.checkFileExists(path)) fileUtil.deleteFile(path)
                    }
                deletePathVideoByDailyUuid(uuid)

                queryDailyAudioByUuidToList(uuid).mapNotNull { it.audioPath }.forEach { path ->
                    if (fileUtil.checkFileExists(path)) fileUtil.deleteFile(path)
                }
                deletePathAudioByDailyUuid(uuid)

                deleteDaily(dailyEntity)
            }
    }

    fun getByDateRange(dateString: String): LiveData<List<DailyEntity>> {
        return dailyRepository.getByDateRange(dateString).asLiveData()
    }
}