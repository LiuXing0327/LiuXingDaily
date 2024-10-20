package com.liuxing.daily.data

/**
 * Author：流星
 * DateTime：2024/10/18 下午5:15
 * Description：更新日志数据类
 */
data class VersionLogData(
    val version: String,
    val date: String,
    val versionList: List<String>
)