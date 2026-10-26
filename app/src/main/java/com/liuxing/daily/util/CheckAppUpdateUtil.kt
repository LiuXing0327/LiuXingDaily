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

package com.liuxing.daily.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.liuxing.daily.R
import com.liuxing.daily.viewmodel.AppUpdateInfo
import com.liuxing.daily.viewmodel.AppUpdateViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object CheckAppUpdateUtil {

    /**
     * 检查更新
     *
     * @param context 上下文
     * @param appUpdateViewModel 用于显示更新弹窗的 ViewModel
     * @param isManual 是否是手动检查（手动检查会显示进度条和“已是最新版本”提示）
     */
    fun checkUpdate(
        context: Context,
        appUpdateViewModel: AppUpdateViewModel,
        isManual: Boolean = true
    ) {
        val activity = context as? Activity
        if (activity == null || activity.isFinishing || activity.isDestroyed) return

        if (isManual) {
            appUpdateViewModel.startChecking()
        }

        val client = OkHttpClient()
        val request = Request.Builder().url(ConstUtil.CHECK_APP_VERSION_URL).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    if (activity.isFinishing || activity.isDestroyed) return@post
                    appUpdateViewModel.stopChecking()
                    if (isManual) {
                        appUpdateViewModel.showStatus(context.getString(R.string.failed_to_check_for_updates))
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body.string()
                Handler(Looper.getMainLooper()).post {
                    if (activity.isFinishing || activity.isDestroyed) return@post
                    try {
                        val jsonObject = JSONObject(jsonString)
                        val latestVersionCode = jsonObject.getInt("versionCode")
                        val latestVersionName = jsonObject.getString("versionName")
                        val releaseNotes = jsonObject.getString("releaseNotes")
                        val downloadUrl = jsonObject.getString("downloadUrl")
                        val currentVersionCode = VersionUtil.getVersionCode(context)
                        if (latestVersionCode > currentVersionCode) {
                            appUpdateViewModel.showUpdate(
                                AppUpdateInfo(
                                    versionName = latestVersionName,
                                    releaseNotes = releaseNotes,
                                    downloadUrl = downloadUrl
                                )
                            )
                        } else if (isManual) {
                           appUpdateViewModel.stopChecking()
                           appUpdateViewModel.showStatus(context.getString(R.string.it_is_the_latest_version))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        if (isManual) {
                            appUpdateViewModel.stopChecking()
                            appUpdateViewModel.showStatus(context.getString(R.string.failed_to_check_for_updates))
                        }
                    }
                }
            }
        })
    }
}