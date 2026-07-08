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


package com.liuxing.daily.backup

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.liuxing.daily.database.DailyDatabase
import com.liuxing.daily.util.BackupEncryptionUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object SafRestoreManager {

    /**
     * 恢复结果状态
     */
    enum class RestoreStatus {
        SUCCESS, FAILED, NEED_PASSWORD, WRONG_PASSWORD
    }

    suspend fun restore(
        context: Context,
        uri: Uri,
        dailyViewModel: DailyViewModel,
        password: String? = null
    ): RestoreStatus = withContext(Dispatchers.IO) {
        try {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            
            // 读取原始数据并解析为 JsonObject 以判断是否加密
            val rawJson = context.contentResolver.openInputStream(uri)?.use { input ->
                InputStreamReader(input, Charsets.UTF_8).use { it.readText() }
            } ?: return@withContext RestoreStatus.FAILED

            val rootElement = JsonParser.parseString(rawJson)
            
            val payload: LocalBackupPayload = if (rootElement is JsonObject && rootElement.has("isEncrypted")) {
                if (password == null) return@withContext RestoreStatus.NEED_PASSWORD
                
                val encryptedData = rootElement.get("payload").asString
                val decryptedJson = BackupEncryptionUtil.decrypt(encryptedData, password)
                    ?: return@withContext RestoreStatus.WRONG_PASSWORD
                
                gson.fromJson(decryptedJson, LocalBackupPayload::class.java)
            } else {
                // 普通明文流程
                gson.fromJson(rawJson, LocalBackupPayload::class.java)
            }

            // 执行数据库写入
            performRestore(context, payload, dailyViewModel)
            RestoreStatus.SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            RestoreStatus.FAILED
        }
    }

    private suspend fun performRestore(
        context: Context,
        payload: LocalBackupPayload,
        dailyViewModel: DailyViewModel
    ) {
        val database = DailyDatabase.getDatabase(context)
        val dailyDao = database.getDailyDao()
        val labelDao = database.getDailyLabelDao()
        val existingLabels = dailyViewModel.queryDailyLabelToList().map { it.label }.toSet()

        payload.labelList
            .filter { it.label !in existingLabels }
            .forEach { labelDao.insertDailyLabel(it.copy(id = null)) }

        payload.dailyList.forEach { dailyWithMedia ->
            val daily = dailyWithMedia.dailyEntity
            val dailyUuid = daily.dailyUUID ?: return@forEach
            if (dailyViewModel.checkDailyExists(dailyUuid)) return@forEach

            dailyDao.insertDaily(daily.copy(id = null))
            dailyWithMedia.imageList.forEach {
                dailyDao.insertDailyImagePath(it.copy(id = null, dailyUuid = dailyUuid))
            }
            dailyWithMedia.videoList.forEach {
                dailyDao.insertDailyVideoPath(it.copy(id = null, dailyUuid = dailyUuid))
            }
            dailyWithMedia.audioList.forEach {
                dailyDao.insertDailyAudioPath(it.copy(id = null, dailyUuid = dailyUuid))
            }
        }
    }
}
