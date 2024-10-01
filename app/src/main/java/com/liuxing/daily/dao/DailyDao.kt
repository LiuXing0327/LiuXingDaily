package com.liuxing.daily.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.liuxing.daily.entity.DailyEntity

@Dao
interface DailyDao {

    @Insert
    fun insertDaily(vararg dailyEntity: DailyEntity)

    @Delete
    fun deleteDaily(vararg dailyEntity: DailyEntity)

    @Update
    fun updateDaily(vararg dailyEntity: DailyEntity)

    @Query("SELECT * FROM DAILY_INFO ORDER BY ID DESC")
    fun queryAllDaily(): LiveData<List<DailyEntity>>

    @Query("SELECT * FROM DAILY_INFO WHERE title LIKE :searchQuery OR content LIKE :searchQuery ORDER BY ID DESC")
    fun queryDaily(searchQuery:String): LiveData<List<DailyEntity>>
}