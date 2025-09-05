/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory


object ImageUtil {

    /**
     * 创建图片缩略图
     *
     * @param filePath 图片文件路径
     * @return 缩略图 Bitmap
     */
    fun createImageThumbnail(filePath: String?): Bitmap? {
        return try {
            BitmapFactory.Options().apply {
                inTempStorage = ByteArray(100 * 1024)
                inPreferredConfig = Bitmap.Config.RGB_565
                inSampleSize = 2
            }.let { opts ->
                BitmapFactory.decodeFile(filePath, opts)
            }
        } catch (e: Exception) {
            null
        }
    }
}