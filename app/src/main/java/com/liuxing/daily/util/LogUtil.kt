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
    private const val LOG_D = "D"
    fun d(message: String) {
        Log.d(LOG_D, "logD: $message")
    }

    /**
     * 错误
     */
    private const val LOG_E = "E"
    fun e(message: String) {
        Log.e(LOG_E, "logG: $message")
    }
}