/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import android.util.Log


object LogUtil {

    /**
     * 全局开关
     */
    private const val DEBUG = false

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
     * 调试日志
     */
    fun d(message: String, tag: String? = null) {
        if (DEBUG) {
            Log.d(tag ?: getTag(), message)
        }
    }

    /**
     * 错误日志
     */
    fun e(message: String, throwable: Throwable, tag: String? = null) {
        if (DEBUG) {
            Log.e(tag ?: getTag(), message, throwable)
        }
    }

    /**
     * 警告日志
     */
    fun w(message: String, tag: String? = null) {
        if (DEBUG) {
            Log.w(tag ?: getTag(), message)
        }
    }
}