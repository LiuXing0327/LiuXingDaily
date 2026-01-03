/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toMonthDay(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MM-dd"))
