package com.liuxing.daily.util

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever

/**
 * Author：流星
 * DateTime：2024/11/23 21:57
 * Description：视频工具类
 */
object VideoUtil {

    /**
     * 创建视频缩略图
     *
     * @param videoPath 视频路径
     * @return 缩略图 Bitmap
     */
    fun createVideoThumbnail(videoPath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoPath)
            return retriever.getFrameAtTime(
                0,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            retriever.release()
        }
    }
}