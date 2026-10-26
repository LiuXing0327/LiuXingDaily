/*
 * Copyright 2024-2026 流星
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
        } catch (_: Exception) {
            null
        }
    }

    const val DRAW_IMAGE = "draw_image_name"
}