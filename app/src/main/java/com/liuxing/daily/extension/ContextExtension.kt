/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import android.content.Context
import com.liuxing.daily.R

fun Context.formatDateTimeWeek(dateString: String, time: String, week: String): String {
    return if (week.isBlank()) getString(
        R.string.date_time_format, dateString, time
    ) else getString(R.string.date_time_week_format, dateString, time, week)
}