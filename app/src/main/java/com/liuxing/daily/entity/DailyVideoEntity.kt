/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

/**
 * 日记视频实体类
 */
@Entity(tableName = "DAILY_VIDEO")
data class DailyVideoEntity(

    @PrimaryKey(autoGenerate = true)
    @Expose(deserialize = false, serialize = false) val id: Long? = null,

    @ColumnInfo(name = "DAILY_UUID")
    @Expose(deserialize = true, serialize = true) var dailyUuid: String?,

    @ColumnInfo(name = "VIDEO_PATH")
    @Expose(deserialize = true, serialize = true) var videoPath: String?
)