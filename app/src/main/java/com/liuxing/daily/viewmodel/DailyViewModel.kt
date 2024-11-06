package com.liuxing.daily.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity
import com.liuxing.daily.repository.DailyRepository
import kotlinx.coroutines.launch

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

    fun deletePathImageByDailyUuid(dailyUuid: String) = viewModelScope.launch {
        dailyRepository.deletePathImageByDailyUuid(dailyUuid)
    }

    fun clearClearImagePath() = viewModelScope.launch {
        dailyRepository.clearImagePath()
    }

    suspend fun queryDailyImageByUuidToList(dailyUuid: String): List<DailyImageEntity> =
        dailyRepository.queryDailyImageByUuidToList(dailyUuid)

    fun queryImageCount(): LiveData<Int> = dailyRepository.queryImageCount()
}