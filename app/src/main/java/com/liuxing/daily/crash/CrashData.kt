/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.crash

import android.os.Build
import com.liuxing.daily.MyApplication
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.VersionUtil

/**
 * 崩溃数据，包括应用版本、设备信息和错误堆栈。
 */
data class CrashData(
    val versionCode: String = VersionUtil.getVersionCode(MyApplication.appContext).toString(),
    val versionName: String = VersionUtil.getVersionName(MyApplication.appContext),
    val deviceData: DeviceData = DeviceData(),
    val errorMessage: String? = null,
    val stackTrace: String? = null
)

/**
 * 设备信息，包括型号和系统版本。
 */
data class DeviceData(
    val model: String = Build.MODEL,
    val versionRelease: String = Build.VERSION.RELEASE,
    val versionSdk: String = Build.VERSION.SDK_INT.toString()
)

/**
 * 解析崩溃数据
 *
 * @param cause 导致崩溃的 Throwable
 * @return 解析后的崩溃信息
 */
fun parseCrashData(cause: Throwable): String {
    val crashData =
        CrashData(errorMessage = cause.message, stackTrace = LogUtil.getStackTraceString(cause))

    return """ 
VERSION_CODE: ${crashData.versionCode}
VERSION_NAME: ${crashData.versionName}
MODEL: ${crashData.deviceData.model}
VERSION_RELEASE: ${crashData.deviceData.versionRelease}
VERSION_SDK: ${crashData.deviceData.versionSdk}
ERROR_MESSAGE: ${crashData.errorMessage}
STACK_TRACE:
${crashData.stackTrace}
    """.trimIndent()
}