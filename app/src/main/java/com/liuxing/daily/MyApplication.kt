/*
 * Copyright 2024-2026 流星
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liuxing.daily

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import com.liuxing.daily.backup.SafBackupManager
import com.liuxing.daily.crash.Crash
import com.liuxing.daily.ui.lock.UnlockActivity
import com.liuxing.daily.ui.datamanagement.DataManagementActivity
import com.liuxing.daily.util.BackupEncryptionUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *  Project：流星日记
 */
class MyApplication : Application(), DefaultLifecycleObserver {

    private var sharedPreferences: SharedPreferences? = null
    private var lastBackgroundTimestamp: Long = 0

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super<Application>.onCreate()
        appContext = applicationContext
        initSharePreferences()
       // DynamicColors.applyToActivitiesIfAvailable(this)
        ThemeUtil.setThemeMode(sharedPreferences!!.getInt("theme_mode_preference", 0))
        this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        deleteOldCrashLogs()

        // 初始化全局崩溃捕捉
        Crash()

        // 自动备份：解密本地存储的密码
        if (SharedPreferencesUtil.getBoolean(this, DataManagementActivity.AUTO_BACKUP_KEY, true)) {
            CoroutineScope(Dispatchers.IO).launch {
                val isEncryptionEnabled = SharedPreferencesUtil.getBoolean(this@MyApplication, DataManagementActivity.BACKUP_ENCRYPTION_KEY, false)
                
                // 从本地解密加载密码
                val encryptedPassword = SharedPreferencesUtil.getString(this@MyApplication, DataManagementActivity.BACKUP_PASSWORD_KEY, "")
                val savedPassword = BackupEncryptionUtil.decryptLocal(encryptedPassword)
                
                val finalPassword = if (isEncryptionEnabled && savedPassword.isNotBlank()) savedPassword else null

                SafBackupManager.backup(appContext, DailyViewModel(this@MyApplication), true, finalPassword)
            }
        }

        // 注册生命周期监听
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * 当应用进入前台时
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        val appPassword = sharedPreferences?.getString("app_password", "")
        if (!appPassword.isNullOrEmpty()) {
            val currentTime = System.currentTimeMillis()
            // 如果超过 30 秒，触发解锁界面
            if (lastBackgroundTimestamp != 0L) {
                val elapsedBackgroundTime = currentTime - lastBackgroundTimestamp
                if (elapsedBackgroundTime > 30000) {
                    IntentUtil.startActivity(
                        this,
                        UnlockActivity::class.java,
                        mapOf("lock" to true)
                    )
                }
            }
        }
        // 重置时间戳，防止重复触发
        lastBackgroundTimestamp = 0L
    }

    /**
     * 当应用进入后台时
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lastBackgroundTimestamp = System.currentTimeMillis()
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
            if (file.name.startsWith("crash") && file.name.endsWith(".txt")) file.delete()
        }
    }
}