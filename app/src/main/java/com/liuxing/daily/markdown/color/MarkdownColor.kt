/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.markdown.color

import android.content.Context
import androidx.core.content.ContextCompat
import com.liuxing.daily.R

/**
 * MarkdownColor
 *
 * 用于存储和管理 Markdown 的颜色，例如 [bulletColor] 的圆点颜色。
 * 通过 [init] 函数在 Activity 或 Fragment 中初始化颜色。
 *
 * 使用示例：
 * ```
 * // 在 Activity 或 Fragment 中初始化
 * MarkdownColor.init(this)
 *
 * // 在 BulletSpan 中使用
 * paint.color = MarkdownColor.bulletColor
 * ```
 */

object MarkdownColor {

    var bulletColor = 0

    /**
     * 初始化 MarkdownColor
     *
     * @param context 上下文
     */
    fun init(context: Context) {
        bulletColor = ContextCompat.getColor(context, R.color.bullet_color)
    }
}