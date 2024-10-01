package com.liuxing.daily.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.liuxing.daily.dao.DailyDao
import com.liuxing.daily.database.DailyDatabase
import com.liuxing.daily.entity.DailyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyRepository(application: Application) {
    private val dailyDao: DailyDao

    init {
        val database = DailyDatabase.getDatabase(application)
        dailyDao = database.getDailyDao()
    }

    fun insertDaily(dailyEntity: DailyEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            dailyDao.insertDaily(dailyEntity)
        }

    fun deleteDaily(dailyEntity: DailyEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            dailyDao.deleteDaily(dailyEntity)
        }

    fun updateDaily(dailyEntity: DailyEntity) =
        CoroutineScope(Dispatchers.IO).launch {
            dailyDao.updateDaily(dailyEntity)
        }

    fun queryAllDaily(): LiveData<List<DailyEntity>> = dailyDao.queryAllDaily()

    fun queryDaily(searchQuery: String): LiveData<List<DailyEntity>> =
        dailyDao.queryDaily(searchQuery)
}