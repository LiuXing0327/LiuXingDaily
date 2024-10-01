package com.liuxing.daily.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object VersionUtil {

    /**
     * 获取版本号
     *
     * @param context 上下文
     * @return 版本号
     */
    fun getVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            -1
        }
    }

    /**
     * 获取版本名
     *
     * @param context 上下文
     * @return 版本名
     */
    fun getVersionName(context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName!!
    }
}