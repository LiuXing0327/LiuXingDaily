/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import android.graphics.Bitmap

object BitmapUtil {

    /**
     * 判断 bitmap 是否有效
     *
     * @param bitmapProvider 返回要检查的 Bitmap
     * @return true：有效
     *         else：无效
     */
    fun check(bitmapProvider: () -> Bitmap): Boolean {
        return try {
            val bmp = bitmapProvider()
            bmp != null && !bmp.isRecycled
            true
        } catch (e: UninitializedPropertyAccessException) {
            false
        }
    }
}