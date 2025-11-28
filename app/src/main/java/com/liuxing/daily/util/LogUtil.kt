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
        return stackTrace.getOrNull(4)?.className?.substringAfterLast(".") ?: "com.liuxing.daily"
    }

    /**
     * 调试日志
     *
     * @param tag 标签。
     * @param message 消息。
     */
    fun d(tag: String? = null, message: String) {
        if (DEBUG) {
            Log.d(tag ?: getTag(), message)
        }
    }


    /**
     * 错误日志
     *
     * @param tag 标签。
     * @param message 消息。
     * @param throwable 异常。
     */
    fun e(tag: String? = null, message: String, throwable: Throwable? = null) {
        if (DEBUG) {
            Log.e(tag ?: getTag(), message, throwable)
        }
    }

    /**
     * 警告日志
     *
     * @param tag 标签。
     * @param message 消息。
     */
    fun w(tag: String? = null, message: String) {
        if (DEBUG) {
            Log.w(tag ?: getTag(), message)
        }
    }

    /**
     * 从 Throwable 对象中获取可记录的堆栈跟踪
     *
     * @param throwable 异常。
     */
    fun getStackTraceString(throwable: Throwable): String {
        if (DEBUG) {
            return Log.getStackTraceString(throwable)
        }

        return ""
    }
}