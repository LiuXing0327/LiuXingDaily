package com.liuxing.daily.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "DAILY_INFO")
data class DailyEntity(

    /**
     * deserialize 参与是否反序列化
     * serialize 参与是否序列化
     *
     * 因为不导出ID所以除了ID所有的都参与序列化和反序列化
     */

    @PrimaryKey(autoGenerate = true)
    @Expose(deserialize = false, serialize = false) val id: Long? = null,

    @ColumnInfo(name = "TITLE")
    @Expose(deserialize = true, serialize = true) val title: String?,

    @ColumnInfo(name = "CONTENT")
    @Expose(deserialize = true, serialize = true) val content: String?,

    @ColumnInfo(name = "DATE_TIME")
    @Expose(deserialize = true, serialize = true) val dateTime: Long?,

    @ColumnInfo(name = "BACKGROUND_COLOR_INDEX")
    @Expose(deserialize = true, serialize = true) val backgroundColorIndex: Int?,

    @ColumnInfo(name = "SINGLE_PASSWORD")
    @Expose(deserialize = true, serialize = true) val singlePassword: String? = "",

    @ColumnInfo(name = "MOOD")
    @Expose(deserialize = true, serialize = true) val moodIndex: Int? = 0,

    @ColumnInfo(name = "WEATHER")
    @Expose(deserialize = true, serialize = true) val weatherIndex: Int? = 0,

    @ColumnInfo(name = "DAILY_UUID")
    @Expose(deserialize = true, serialize = true) val dailyUUID: String? = null,

    @ColumnInfo(name = "IS_DELETED")
    @Expose(deserialize = true, serialize = true) val isDeleted: Boolean = false,

    @ColumnInfo(name = "DAILY_LABEL")
    @Expose(deserialize = true, serialize = true) val dailyLabel: String? = null,

    @ColumnInfo(name = "DAILY_RECYCLER_DATE_TIME")
    @Expose(deserialize = true, serialize = true) val dailyRecyclerDateTime: Long? = null,

    @ColumnInfo(name = "IS_PINNED")
    @Expose(deserialize = true, serialize = true) val isPinned: Boolean = false,

    @ColumnInfo(name = "MONTH_DAY")
    @Expose(deserialize = true, serialize = true) val monthDay: String? = null
)