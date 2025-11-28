package com.liuxing.daily.util

import android.content.Context
import com.liuxing.daily.MyApplication
import com.liuxing.daily.R
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

object DateUtil {

    private val dateFormat =
        arrayOf(
            if (MyApplication.appContext.getString(R.string.daily) == "日记") "yyyy/MM/dd HH:mm" else "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "HH:mm",
            "yyyy-MM-dd HH:mm"
        )

    // 获取当前日期
    fun getCurrentDate(): Date = Date()

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    fun getCurrentDateTime(): Long = getCurrentDate().time

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

    /**
     * 计算两个日期之间的天数差
     */
    fun getDaysBetween(startDate: String, endDate: String = LocalDate.now().toString()): Long {
        val startLocalDate = LocalDate.parse(startDate)
        val endLocalDate = LocalDate.parse(endDate)
        return ChronoUnit.DAYS.between(startLocalDate, endLocalDate)
    }

    /**
     * 获取星期
     *
     * @param context 上下文
     * @return 星期
     */
    fun getWeek(context: Context, dateString: String, dateFormatIndex: Int = 0): String {
        val (year, month, dayOfMonth) = parserDateString(dateString, dateFormatIndex)
        val today = LocalDate.of(year, month, dayOfMonth)

        return when (today.dayOfWeek) {
            DayOfWeek.MONDAY -> context.getString(R.string.monday)
            DayOfWeek.TUESDAY -> context.getString(R.string.tuesday)
            DayOfWeek.WEDNESDAY -> context.getString(R.string.wednesday)
            DayOfWeek.THURSDAY -> context.getString(R.string.thursday)
            DayOfWeek.FRIDAY -> context.getString(R.string.friday)
            DayOfWeek.SATURDAY -> context.getString(R.string.saturday)
            DayOfWeek.SUNDAY -> context.getString(R.string.sunday)
        }
    }

    /**
     * 日期字符串解析
     *
     * @param dateString 日期字符串
     * @param dateFormatIndex 日期格式索引
     */
    fun parserDateString(dateString: String, dateFormatIndex: Int): Triple<Int, Int, Int> {
        val dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat[dateFormatIndex])
        val dateTime = LocalDateTime.parse(dateString, dateTimeFormatter)

        val year = dateTime.year
        val month = dateTime.monthValue
        val dayOfMonth = dateTime.dayOfMonth
        return Triple(year, month, dayOfMonth)
    }
}