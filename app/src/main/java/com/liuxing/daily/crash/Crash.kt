/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.crash

import android.content.Intent
import com.liuxing.daily.MyApplication
import com.liuxing.daily.util.AppUtil
import com.liuxing.daily.util.LogUtil

class Crash : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    companion object {
        private const val TAG = "Crash"
    }

    init {
        // 设置为默认未捕捉异常处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(p0: Thread, p1: Throwable) {
        val crashText = parseCrashData(p1)
        LogUtil.e(TAG, crashText)
        handleException(crashText)
        AppUtil.killApp()
        defaultHandler?.uncaughtException(p0, p1)
    }

    /**
     * 启动 CrashActivity 并传递信息。
     *
     * @param crashText 格式化后的崩溃信息
     */
    private fun handleException(crashText: String) {
        val intent = Intent(MyApplication.appContext, CrashActivity::class.java).apply {
            putExtra("crash_log",crashText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        MyApplication.appContext.startActivity(intent)
    }
}