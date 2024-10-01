package com.liuxing.daily.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.repository.DailyRepository

class DailyViewModel(application: Application) {

    private val dailyRepository: DailyRepository = DailyRepository(application)

    fun insertDaily(dailyEntity: DailyEntity) {
        dailyRepository.insertDaily(dailyEntity)
    }

    fun deleteDaily(dailyEntity: DailyEntity) {
        dailyRepository.deleteDaily(dailyEntity)
    }

    fun updateDaily(dailyEntity: DailyEntity) {
        dailyRepository.updateDaily(dailyEntity)
    }

    fun queryAllDaily(): LiveData<List<DailyEntity>> = dailyRepository.queryAllDaily()

    fun queryDaily(searchQuery: String): LiveData<List<DailyEntity>> =
        dailyRepository.queryDaily(searchQuery)
}