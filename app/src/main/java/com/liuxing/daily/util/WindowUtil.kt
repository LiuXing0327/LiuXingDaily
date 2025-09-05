/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import android.content.Context
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.liuxing.daily.R

object WindowUtil {

    /**
     * 跟随主题模式设置颜色
     *
     *                  浅色模式：-1
     *                  深色模式：-16777216
     *
     * @param window Window
     * @param context 上下文
     */
    fun followPatternSetColor(window: Window, context: Context) {
        val color = ContextCompat.getColor(context, R.color.mode)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = color == -1
    }
}