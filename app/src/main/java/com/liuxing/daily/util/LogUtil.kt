package com.liuxing.daily.util

import android.util.Log

/**
 * Author：流星
 * DateTime：2024/10/12 上午8:03
 * Description：日志工具类
 */
object LogUtil {

    /**
     * 调试
     */
    fun d(tag: String, message: String) {
        Log.d(tag, "logD: $message")
    }

    /**
     * 错误
     */
    fun e(tag: String, message: String) {
        Log.e(tag, "logE: $message")
    }
}