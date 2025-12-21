/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.data

/**
 * 日记设置数据
 *
 * @param key 用于存储的键
 * @param text 设置项显示的文字
 * @param checked 设置项当前是否开启
 * @param iconResource 设置项左侧图标导入资源 ID
 * @param supportingString 设置项显示的副文本
 */
data class DailySettingsData(
    val key: String,
    val text: String,
    var checked: Boolean,
    val iconResource: Int,
    val supportingString: String = ""
)