package com.liuxing.daily.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {

    private val dateFormat =
        arrayOf("yyyy/MM/dd HH:mm")

    // 获取当前日期
    fun getCurrentDate(): Date = Date()

    // 获取日期格式
    private fun getSimpleDateFormat(pattern: String, aDefault: Locale): SimpleDateFormat =
        SimpleDateFormat(pattern, aDefault)

    // 日期格式
    private fun dateFormat(index: Int): SimpleDateFormat {
        return getSimpleDateFormat(dateFormat[index], Locale.getDefault())
    }

    // 获取日期格式
    fun getDateString(index: Int, date: Date): String = dateFormat(index).format(date)

    // 日期字符串转换日期
    fun dateStringToDate(dateString: String, index: Int): Long {
        val date: Date = getSimpleDateFormat(dateFormat[index], Locale.getDefault()).parse(dateString)!!
        return date.time
    }
}