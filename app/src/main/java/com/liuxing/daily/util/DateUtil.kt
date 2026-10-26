/*
 * Copyright 2024-2026 流星
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liuxing.daily.util

import android.content.Context
import com.liuxing.daily.MyApplication
import com.liuxing.daily.R
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import com.liuxing.daily.extension.formatDateTimeWeek

object DateUtil {

    private val dateFormat =
        arrayOf(
            if (MyApplication.appContext.getString(R.string.daily) == "日记") "yyyy/MM/dd HH:mm" else "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "HH:mm",
            "yyyy-MM-dd HH:mm"
        )

    /**
     * 年月日的起始索引和结束索引
     */
    val YMD_INDEX = 0 to 10

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

    /**
     * yyyy-MM-dd 格式日期字符串转换为当前的起止时间戳（毫秒）。
     *
     * @param dateString 日期字符串。
     * @return Pair(startMillis,endMillis)
     */
    fun dateStringToDayRangeMillis(dateString: String): Pair<Long, Long> {
        val localDate = LocalDate.parse(dateString)
        val zoneId = ZoneId.systemDefault()

        // 当天 00:00 的 ZonedDateTime
        val startOfDay = localDate.atStartOfDay(zoneId)
        // 第二天 00:00 的 ZonedDateTime
        val startOfNextDay = localDate.plusDays(1).atStartOfDay(zoneId)

        // 转成 EpochMilli
        val startMillis = startOfDay.toInstant().toEpochMilli()
        val startNextMillis = startOfNextDay.toInstant().toEpochMilli()

        // 第二天 00:00 - 1 毫秒，得到当天最后一毫秒
        val endMillis = startNextMillis - 1L

        return startMillis to endMillis
    }

    /**
     * 选择日期时间
     */
    fun selectDateTime(
        activity: androidx.appcompat.app.AppCompatActivity,
        initialDate: Date,
        onDateTimeSelected: (String) -> Unit
    ) {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText(activity.getString(R.string.select_date))
            .setSelection(initialDate.time)
            .build()

        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val initialTime = timeFormatter.format(initialDate)
        val hour = initialTime.split(":")[0].toInt()
        val minute = initialTime.split(":")[1].toInt()

        val timePicker = com.google.android.material.timepicker.MaterialTimePicker.Builder()
            .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText(activity.getString(R.string.select_time))
            .build()

        datePicker.show(activity.supportFragmentManager, datePicker.tag)

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = selection
            
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            
            val dateSep = if (activity.getString(R.string.daily) == "日记") "/" else "-"
            val newSelectedDateString = "%d%s%02d%s%02d".format(year, dateSep, month, dateSep, day)

            timePicker.show(activity.supportFragmentManager, timePicker.tag)

            timePicker.addOnPositiveButtonClickListener {
                val selectedHour = timePicker.hour
                val selectedMinute = timePicker.minute
                val newSelectedTime = "%02d:%02d".format(selectedHour, selectedMinute)

                val week = getWeek(activity, "$newSelectedDateString $newSelectedTime")

                onDateTimeSelected(
                    activity.formatDateTimeWeek(
                        newSelectedDateString,
                        newSelectedTime,
                        week
                    )
                )
            }
        }
    }
}