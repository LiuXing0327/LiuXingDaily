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
    @Expose(deserialize = true, serialize = true) val singlePassword: String? = ""
)