package com.liuxing.daily.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

/**
 * Author：流星
 * DateTime：2024/12/8 12:06
 * Description：
 */
@Entity(tableName = "DAILY_AUDIO")
data class DailyAudioEntity(

    @PrimaryKey(autoGenerate = true)
    @Expose(deserialize = false, serialize = false) val id: Long? = null,

    @ColumnInfo(name = "DAILY_UUID")
    @Expose(deserialize = true, serialize = true) var dailyUuid:String?,

    @ColumnInfo(name = "AUDIO_PATH")
    @Expose(deserialize = true, serialize = true) var audioPath:String?

)