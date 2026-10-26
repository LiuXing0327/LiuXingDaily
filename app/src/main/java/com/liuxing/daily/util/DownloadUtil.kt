/*
 * Copyright 2026 流星
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

package com.liuxing.daily.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.liuxing.daily.viewmodel.AppUpdateViewModel
import java.io.File

/**
 * APK 下载与安装工具类
 */
object DownloadUtil {

    private var downloadId: Long = -1
    private var downloadReceiver: BroadcastReceiver? = null
    private var appContext: Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    /**
     * 开始下载 APK
     */
    fun downloadApk(
        context: Context, url: String, fileName: String, viewModel: AppUpdateViewModel
    ) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(url.toUri()).apply {
            // 允许移动网络和 WiFi
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            // 明确允许使用计费网络（移动数据）
            setAllowedOverMetered(true)
            // 允许漫游时下载
            setAllowedOverRoaming(true)
            // 设置通知可见性：下载中和下载完成都可见
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setTitle(fileName)
            setDescription(context.getString(com.liuxing.daily.R.string.downloading))
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            // 某些设备需要设置这个才能在下载列表中看到
            setMimeType("application/vnd.android.package-archive")
        }

        // 使用 ApplicationContext 避免内存泄漏和 Unregister 时的 Context 不一致问题
        val applicationContext = context.applicationContext
        appContext = applicationContext

        // 开始下载前，先清理一次目录
        clearDownloadDirectory(applicationContext)

        // 如果之前有正在进行的任务，先移除并取消之前的广播接收器
        if (downloadId != -1L) {
            downloadManager.remove(downloadId)
            unregisterDownloadReceiver()
        }

        downloadId = downloadManager.enqueue(request)
        viewModel.updateDownloadProgress(0f)

        // 注册广播监听下载完成
        registerReceiver(applicationContext, viewModel, fileName)

        // 启动进度监听
        startProgressUpdate(applicationContext, viewModel)
    }

    /**
     * 注册广播监听下载完成
     *
     * @param context Context
     * @param viewModel AppUpdateViewModel
     * @param fileName APK Name
     */
    private fun registerReceiver(
        context: Context, viewModel: AppUpdateViewModel, fileName: String
    ) {
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    stopProgressUpdate()
                    viewModel.finishDownloading()
                    installApk(context, fileName)
                    unregisterDownloadReceiver()
                    downloadId = -1
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    /**
     * 取消注册
     */
    private fun unregisterDownloadReceiver() {
        if (downloadReceiver != null && appContext != null) {
            try {
                appContext?.unregisterReceiver(downloadReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                downloadReceiver = null
            }
        }
    }

    /**
     * 开始进度更新
     *
     * @param context Context
     * @param viewModel AppUpdateViewModel
     */
    private fun startProgressUpdate(context: Context, viewModel: AppUpdateViewModel) {
        stopProgressUpdate()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        progressRunnable = object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val bytesDownloaded =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val reason =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))

                    LogUtil.d(
                        "DownloadProgress",
                        "Downloaded: $bytesDownloaded, Total: $bytesTotal, Status: $status, Reason: $reason"
                    )

                    if (bytesTotal > 0) {
                        val progress = (bytesDownloaded.toFloat() / bytesTotal.toFloat()) * 100
                        viewModel.updateDownloadProgress(progress)
                    } else if (status == DownloadManager.STATUS_RUNNING && bytesDownloaded > 0) {
                        // 如果无法获取总大小，但已经产生下载字节，设为 1% 表示活动
                        viewModel.updateDownloadProgress(1f)
                    }

                    if (status == DownloadManager.STATUS_FAILED) {
                        stopProgressUpdate()
                        viewModel.finishDownloading()
                        // 失败时可以清理 ID 并提示
                        downloadId = -1
                    } else if (status != DownloadManager.STATUS_SUCCESSFUL) {
                        handler.postDelayed(this, 500)
                    }
                }
                cursor.close()
            }
        }
        handler.post(progressRunnable!!)
    }

    /**
     * 停止进度更新
     */
    private fun stopProgressUpdate() {
        progressRunnable?.let { handler.removeCallbacks(it) }
        progressRunnable = null
    }

    /**
     * 清理下载目录下的所有 APK 文件
     *
     * @param context Context
     */
    fun clearDownloadDirectory(context: Context) {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            downloadDir?.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".apk", ignoreCase = true)) {
                    file.delete()
                    LogUtil.d("DownloadUtil", "Deleted old APK: ${file.name}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 安装 APK
     *
     * @param context Context
     * @param fileName APK Name
     */
    private fun installApk(context: Context, fileName: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        if (!file.exists()) return

        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }
}
