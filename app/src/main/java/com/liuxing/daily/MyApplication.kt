package com.liuxing.daily

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager
import com.liuxing.daily.crash.Crash
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.ThemeUtil

/**
 *  Project：流星日记
 */
class MyApplication : Application() {

    private var sharedPreferences: SharedPreferences? = null

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        initSharePreferences()
       // DynamicColors.applyToActivitiesIfAvailable(this)
        ThemeUtil.setThemeMode(sharedPreferences!!.getInt("theme_mode_preference", 0))
        this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        deleteOldCrashLogs()

        // 初始化全局崩溃捕捉
        Crash()
    }

    /**
     * 初始化偏好
     */
    private fun initSharePreferences() =
        PreferenceManager.getDefaultSharedPreferences(this).also { sharedPreferences = it }

    /**
     * 删除存储在外部缓存目录的 crash 文件
     */
    private fun deleteOldCrashLogs() {
        val crashDir = externalCacheDir ?: return
        crashDir.listFiles()?.forEach { file ->
            FileUtil().getFilePaths(crashDir)
            if (file.name.startsWith("crash") && file.name.endsWith(".txt")) file.delete()
        }
    }
}