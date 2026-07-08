/*
 * Copyright 2026 流星
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

package com.liuxing.daily.ui.qrx

import android.graphics.Bitmap
import android.graphics.Color
import android.view.Window
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable
import com.liuxing.daily.util.BitmapUtil
import com.liuxing.daily.util.StatusBarUtil

/**
 * 检查状态栏颜色
 */
fun QRXActivity.checkStatusBarColor(
    appBarLayout: AppBarLayout, window: Window, bitmap: Bitmap, wallpaperAlpha: Float
) {
    val background = appBarLayout.background
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    if (background is MaterialShapeDrawable) {
        val fillColor = background.fillColor
        val color = fillColor?.defaultColor ?: Color.TRANSPARENT
        if (color == Color.TRANSPARENT) {
            val bitmapValid = BitmapUtil.check({ bitmap })
            if (bitmapValid) {
                setLightStausBarsFromBitmap(bitmap, wallpaperAlpha, window)
            }
        } else {
            val isDark = ColorUtils.calculateLuminance(color) < 0.5
            insetsController.isAppearanceLightStatusBars = !isDark
        }
    }
}

fun QRXActivity.checkStatusBarColor(
    topAppBarColor: Int, window: Window, bitmap: Bitmap, wallpaperAlpha: Float
) {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    if (topAppBarColor == Color.TRANSPARENT) {
        val bitmapValid = BitmapUtil.check({ bitmap })
        if (bitmapValid) setLightStausBarsFromBitmap(bitmap, wallpaperAlpha, window)
    } else {
        val isDark = ColorUtils.calculateLuminance(topAppBarColor) < 0.5
        insetsController.isAppearanceLightStatusBars = !isDark
    }
}

/**
 * 根据 Bitmap 的顶部颜色调整状态栏外观
 *
 * @param bitmap 用于分析的壁纸 Bitmap
 */
fun QRXActivity.setLightStausBarsFromBitmap(bitmap: Bitmap, wallpaperAlpha: Float, window: Window) =
    StatusBarUtil.setLightStausBarsFromBitmap(bitmap, wallpaperAlpha, window)

/**
 * 检查 [bitmap] 是否有效。
 */
fun QRXActivity.isBitmapValid(bitmap: Bitmap): Boolean = BitmapUtil.check {
    bitmap
}

/**
 * 获取 [bitmap]
 *
 * 如果有效返回 [bitmap]，无效返回 null.
 */
fun QRXActivity.getBitmap(bitmap: Bitmap): Bitmap? {
    isBitmapValid(bitmap).let {
        return if (it) {
            bitmap
        } else {
            null
        }
    }
}