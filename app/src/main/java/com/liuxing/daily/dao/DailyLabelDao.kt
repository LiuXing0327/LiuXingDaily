/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.liuxing.daily.entity.DailyLabelEntity

@Dao
interface DailyLabelDao {

    @Insert
    suspend fun insertDailyLabel(vararg dailyLabelEntity: DailyLabelEntity)

    @Delete
    suspend fun deleteDailyLabel(vararg dailyLabelEntity: DailyLabelEntity)

    @Update
    suspend fun updateDailyLabel(vararg dailyLabelEntity: DailyLabelEntity)

    @Query("SELECT * FROM DAILY_LABEL ORDER BY ID DESC")
    fun queryAllDailyLabel(): LiveData<List<DailyLabelEntity>>

    @Query("SELECT * FROM DAILY_LABEL")
    fun queryDailyLabelToList(): List<DailyLabelEntity>

}