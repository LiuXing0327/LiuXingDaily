/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import com.liuxing.daily.MyApplication
import com.liuxing.daily.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val INPUT_DATE_FORMATTER_A = DateTimeFormatter.ofPattern("yyyy/M/d")
private val OUTPUT_DATE_FORMATTER_A = DateTimeFormatter.ofPattern("yyyy/MM/dd")

private val INPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-M-d")
private val OUTPUT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun String.formatDateString(): String {
    val isC = MyApplication.appContext.getString(R.string.daily) == "日记"
    val formatter =
        if (isC) INPUT_DATE_FORMATTER_A to OUTPUT_DATE_FORMATTER_A else INPUT_DATE_FORMATTER to OUTPUT_DATE_FORMATTER
    val inputFormatter = formatter.first
    val outputFormatter = formatter.second
    val date = LocalDate.parse(this, inputFormatter)
    return date.format(outputFormatter)
}