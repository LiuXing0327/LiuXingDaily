package com.liuxing.daily.data

/**
 * Author：流星
 * DateTime：2025/1/26 13:28
 * Description：主题颜色数据类
 */
data class ThemeColorData(
    val id: Int,
    val topColor: Int,
    val leftColor: Int,
    val rightColor: Int,
    var isSelected: Boolean
)