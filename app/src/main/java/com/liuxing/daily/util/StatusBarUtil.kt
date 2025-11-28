/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import android.graphics.Bitmap
import android.view.Window
import android.widget.ImageView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.palette.graphics.Palette

/**
 * 状态栏工具类
 */
object StatusBarUtil {

    /**
     * 根据 Bitmap 的顶部颜色调整当前 Activity 的状态栏外观
     *
     * @param bitmap 用于分析的壁纸 Bitmap
     * @param wallpaper 对应的 ImageView
     * @param window 当前 Activity 的 window
     */
    fun setLightStausBarsFromBitmap(bitmap: Bitmap, wallpaper: ImageView, window: Window) {
        Palette.from(bitmap).maximumColorCount(7).setRegion(0, 0, bitmap.width, 100)
            .generate { palette ->
                val mostUsed = palette?.swatches?.maxByOrNull { it.population }
                mostUsed?.let { swatch ->
                    val isDark = ColorUtils.calculateLuminance(swatch.rgb) < 0.5
                    val wallpaperAlpha = wallpaper.alpha
                    val insetsController =
                        WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !isDark && wallpaperAlpha > 0.5f
                }
            }
    }
}