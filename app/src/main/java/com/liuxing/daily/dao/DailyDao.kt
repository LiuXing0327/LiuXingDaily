package com.liuxing.daily.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity

@Dao
interface DailyDao {

    @Insert
    suspend fun insertDaily(vararg dailyEntity: DailyEntity)

    @Delete
    suspend fun deleteDaily(vararg dailyEntity: DailyEntity)

    @Update
    suspend fun updateDaily(vararg dailyEntity: DailyEntity)

    @Query("SELECT * FROM DAILY_INFO ORDER BY ID DESC")
    fun queryAllDaily(): LiveData<List<DailyEntity>>

    @Query("SELECT * FROM DAILY_INFO ORDER BY ID DESC")
    suspend fun queryAllDailyToList(): List<DailyEntity>

    @Query("DELETE FROM DAILY_INFO")
    suspend fun clearDaily()

    @Insert
    suspend fun insertDailyImagePath(vararg dailyImageEntity: DailyImageEntity)

    @Query("SELECT * FROM DAILY_IMAGE WHERE DAILY_UUID = :dailyUuid")
    fun queryDailyImageByUuid(dailyUuid: String): LiveData<List<DailyImageEntity>>

    @Query("SELECT * FROM DAILY_IMAGE WHERE DAILY_UUID = :dailyUuid")
    suspend fun queryDailyImageByUuidToList(dailyUuid: String): List<DailyImageEntity>

    @Query("DELETE FROM DAILY_IMAGE WHERE IMAGE_PATH = :imagePath")
    suspend fun deleteSelectPathImage(vararg imagePath: String)

    @Query("DELETE FROM DAILY_IMAGE WHERE DAILY_UUID =:dailyUuid")
    suspend fun deletePathImageByDailyUuid(dailyUuid: String)

    @Query("DELETE FROM DAILY_IMAGE")
    suspend fun clearDailyImage()
}