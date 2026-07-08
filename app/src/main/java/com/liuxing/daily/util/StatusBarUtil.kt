/*
 * Copyright 2025-2026 流星
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @param wallpaperAlpha 壁纸透明度
     * @param window 当前 Activity 的 window
     */
    fun setLightStausBarsFromBitmap(bitmap: Bitmap, wallpaperAlpha: Float, window: Window) {
        Palette.from(bitmap).maximumColorCount(7).setRegion(0, 0, bitmap.width, 100)
            .generate { palette ->
                val mostUsed = palette?.swatches?.maxByOrNull { it.population }
                mostUsed?.let { swatch ->
                    val isDark = ColorUtils.calculateLuminance(swatch.rgb) < 0.5
                    val wallpaperAlpha = wallpaperAlpha
                    val insetsController =
                        WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !isDark && wallpaperAlpha > 0.5f
                }
            }
    }
}