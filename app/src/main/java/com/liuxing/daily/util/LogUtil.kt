/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import android.util.Log


object LogUtil {

    private const val DEBUG = true

    /**
     * 获取调用者的类名作为 tag
     *
     * @return 类名
     */
    private fun getTag(): String {
        val stackTrace = Throwable().stackTrace
        return stackTrace.getOrNull(2)?.className?.substringAfterLast(".") ?: "com.liuxing.daily"
    }

    /**
     * 调试
     */
    fun d(message: String) {
        if (DEBUG) {
            Log.d(getTag(), message)
        }
    }

    /**
     * 错误
     */
    fun e(message: String) {
        if (DEBUG) {
            Log.e(getTag(), message)
        }
    }

    fun w(message: String) {
        if (DEBUG) {
            Log.w(getTag(), message)
        }
    }
}