/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.data

/**
 * 主题颜色数据类
 */
data class ThemeColorData(
    val id: Int,
    val topColor: Int,
    val leftColor: Int,
    val rightColor: Int,
    var isSelected: Boolean
)