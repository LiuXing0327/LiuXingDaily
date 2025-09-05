/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.data

/**
 * 更新日志数据类
 */
data class VersionLogData(
    val version: String,
    val date: String,
    val versionList: List<String>
)