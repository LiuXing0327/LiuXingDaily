/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import android.content.Context
import android.os.Environment
import com.liuxing.daily.R
import java.io.File

fun Context.formatDateTimeWeek(dateString: String, time: String, week: String): String {
    return if (week.isBlank()) getString(
        R.string.date_time_format, dateString, time
    ) else getString(R.string.date_time_week_format, dateString, time, week)
}

fun Context.getExternalPicturesFilesDir(): File? {
    return getExternalFilesDir(Environment.DIRECTORY_PICTURES)
}