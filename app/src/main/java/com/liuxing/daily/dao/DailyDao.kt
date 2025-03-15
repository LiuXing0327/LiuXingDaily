package com.liuxing.daily.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.liuxing.daily.entity.DailyAudioEntity
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyImageEntity
import com.liuxing.daily.entity.DailyVideoEntity

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

    @Query("SELECT COUNT(*) FROM DAILY_IMAGE")
    fun queryImageCount(): LiveData<Int>

    @Insert
    suspend fun insertDailyVideoPath(vararg dailyVideoEntity: DailyVideoEntity)

    @Query("DELETE FROM DAILY_VIDEO WHERE VIDEO_PATH = :videoPath")
    suspend fun deleteDailySelectPathVideo(vararg videoPath: String)

    @Query("DELETE FROM DAILY_VIDEO WHERE DAILY_UUID = :dailyUuid")
    suspend fun deleteDailyVideoPathByDailyUuid(dailyUuid: String)

    @Query("SELECT * FROM DAILY_VIDEO WHERE DAILY_UUID = :dailyUuid")
    fun queryDailyVideoByUuid(dailyUuid: String): LiveData<List<DailyVideoEntity>>

    @Query("SELECT * FROM DAILY_VIDEO WHERE DAILY_UUID = :dailyUuid")
    suspend fun queryDailyVideoByUuidToList(dailyUuid: String): List<DailyVideoEntity>

    @Insert
    suspend fun insertDailyAudioPath(vararg dailyAudioEntity: DailyAudioEntity)

    @Query("DELETE FROM DAILY_AUDIO WHERE AUDIO_PATH = :audioPath")
    suspend fun deleteDailySelectPathAudio(vararg audioPath: String)

    @Query("DELETE FROM DAILY_AUDIO WHERE DAILY_UUID = :dailyUuid")
    suspend fun deleteDailyAudioPathByDailyUuid(vararg dailyUuid: String)

    @Query("SELECT * FROM DAILY_AUDIO WHERE DAILY_UUID =:dailyUuid")
    fun queryDailyAudioByUuid(dailyUuid: String): LiveData<List<DailyAudioEntity>>

    @Query("SELECT * FROM DAILY_AUDIO WHERE DAILY_UUID =:dailyUuid")
    suspend fun queryDailyAudioByUuidToList(dailyUuid: String): List<DailyAudioEntity>

    @Query("SELECT * FROM DAILY_INFO WHERE DAILY_UUID = :dailyUUID LIMIT 1")
    suspend fun getDailyByUUID(dailyUUID: String): DailyEntity?

}