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

package com.liuxing.daily.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * 更新信息数据类
 */
data class AppUpdateInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String
)

/**
 * 全局管理应用更新状态的 ViewModel
 */
class AppUpdateViewModel : ViewModel() {

    /**
     * 当前待更新的信息，为 null 时不显示
     */
    var updateInfo by mutableStateOf<AppUpdateInfo?>(null)
        private set

    /**
     * 是否正在检查更新
     */
    var isChecking by mutableStateOf(false)
        private set

    /**
     * 待显示的状态信息
     */
    var statusMessage by mutableStateOf<String?>(null)
        private set

    /**
     * 是否正在下载
     */
    var isDownloading by mutableStateOf(false)
        private set

    /**
     * 下载进度 (0-100)
     */
    var downloadProgress by mutableFloatStateOf(0f)
        private set

    /**
     * 开始检查更新
     */
    fun startChecking() {
        isChecking = true
        updateInfo = null
        statusMessage = null
        isDownloading = false
        downloadProgress = 0f
    }

    /**
     * 结束检查更新
     */
    fun stopChecking() {
        isChecking = false
    }

    /**
     * 显示状态信息
     */
    fun showStatus(message: String) {
        statusMessage = message
        isChecking = false
        updateInfo = null
        isDownloading = false
        downloadProgress = 0f
    }

    /**
     * 触发显示更新弹窗
     */
    fun showUpdate(info: AppUpdateInfo) {
        updateInfo = info
        isChecking = false
        statusMessage = null
        isDownloading = false
        downloadProgress = 0f
    }

    /**
     * 更新下载进度
     */
    fun updateDownloadProgress(progress: Float) {
        isDownloading = true
        downloadProgress = progress
    }

    /**
     * 下载完成或取消
     */
    fun finishDownloading() {
        isDownloading = false
        downloadProgress = 0f
    }

    /**
     * 隐藏更新弹窗
     */
    fun dismissUpdate() {
        updateInfo = null
        isChecking = false
        statusMessage = null
        isDownloading = false
        downloadProgress = 0f
    }
}
