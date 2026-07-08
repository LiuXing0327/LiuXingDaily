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
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.entity.DailyWithMedia
import com.liuxing.daily.util.BackupEncryptionUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SAF_BACKUP_KEY = "saf_backup_uri"
private const val BACKUPS_NAME = "backups"
private const val LOCAL_NAME = "local"
private const val AUTO_NAME = "auto"
private const val MANUAL_NAME = "manual"
private const val MAX_AUTO_BACKUPS = 10 // 自动备份保留 10 个

object SafBackupManager {

    fun putUri(context: Context, uri: Uri) {
        SharedPreferencesUtil.putString(context, SAF_BACKUP_KEY, uri.toString())
    }

    fun getUri(context: Context): String {
        return SharedPreferencesUtil.getString(context, SAF_BACKUP_KEY, "")
    }

    /**
     * 执行备份
     *
     * @param isAuto 是否为自动备份
     * @param password 可选密码
     */
    suspend fun backup(
        context: Context, 
        dailyViewModel: DailyViewModel, 
        isAuto: Boolean,
        password: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        
        val allDailies = dailyViewModel.queryAllDailyOnce()
        if (allDailies.isEmpty()) return@withContext false

        val uriString = getUri(context)
        if (!isSafUriValid(context, uriString)) return@withContext false

        val root = DocumentFile.fromTreeUri(context, uriString.toUri()) ?: return@withContext false
        
        // 结构：backups/local/(auto 或 manual)/
        val backupDir = getOrCreateDir(root, BACKUPS_NAME) ?: return@withContext false
        val localDir = getOrCreateDir(backupDir, LOCAL_NAME) ?: return@withContext false
        val targetDir = getOrCreateDir(localDir, if (isAuto) AUTO_NAME else MANUAL_NAME) ?: return@withContext false

        try {
            // 只有自动备份才执行 10 个文件的清理逻辑
            if (isAuto) {
                cleanupOldBackups(targetDir)
            }

            val isEncrypted = !password.isNullOrBlank()
            val backupFile = targetDir.createFile("application/json", buildBackupName(isAuto, isEncrypted))
                ?: return@withContext false
            
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val payload = buildBackupPayload(dailyViewModel, allDailies)
            val jsonContent = gson.toJson(payload)

            context.contentResolver.openOutputStream(backupFile.uri)?.use { output ->
                OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                    if (isEncrypted) {
                        val encryptedData = BackupEncryptionUtil.encrypt(jsonContent, password)
                        val container = JsonObject().apply {
                            addProperty("isEncrypted", true)
                            addProperty("payload", encryptedData)
                        }
                        writer.write(container.toString())
                    } else {
                        writer.write(jsonContent)
                    }
                }
            } ?: return@withContext false

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getOrCreateDir(parent: DocumentFile, name: String): DocumentFile? {
        val existingDir = parent.findFile(name)
        if (existingDir != null && existingDir.isDirectory) return existingDir
        parent.listFiles().forEach { file ->
            if (file.isDirectory && (file.name?.equals(name, ignoreCase = true) == true)) {
                return file
            }
        }
        return parent.createDirectory(name)
    }

    private fun cleanupOldBackups(directory: DocumentFile) {
        val files = directory.listFiles()
            .filter { it.isFile && it.name?.contains("backup_") == true && it.name?.endsWith(".json") == true }
            .sortedBy { it.lastModified() }

        if (files.size >= MAX_AUTO_BACKUPS) {
            val deleteCount = files.size - MAX_AUTO_BACKUPS + 1
            for (i in 0 until deleteCount) {
                files[i].delete()
            }
        }
    }

    private suspend fun buildBackupPayload(dailyViewModel: DailyViewModel, allDailies: List<DailyEntity>): LocalBackupPayload {
        val processedDailyUuids = mutableSetOf<String>()
        val dailyWithMediaList = mutableListOf<DailyWithMedia>()

        allDailies.forEach { dailyEntity ->
            val dailyUuid = dailyEntity.dailyUUID ?: return@forEach
            if (!processedDailyUuids.add(dailyUuid)) return@forEach

            dailyWithMediaList.add(
                DailyWithMedia(
                    dailyEntity = dailyEntity,
                    imageList = dailyViewModel.queryDailyImageByUuidToList(dailyUuid),
                    videoList = dailyViewModel.queryDailyVideoByUuidToList(dailyUuid),
                    audioList = dailyViewModel.queryDailyAudioByUuidToList(dailyUuid)
                )
            )
        }

        return LocalBackupPayload(
            version = 1,
            dailyList = dailyWithMediaList,
            labelList = dailyViewModel.queryDailyLabelToList()
        )
    }

    private fun buildBackupName(isAuto: Boolean, isEncrypted: Boolean): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val typePrefix = if (isAuto) "auto_" else "manual_"
        val lockPrefix = if (isEncrypted) "encrypted_" else ""
        return "backup_${typePrefix}${lockPrefix}$timestamp.json"
    }

    private fun isSafUriValid(context: Context, uriString: String): Boolean {
        if (uriString.isBlank()) return false
        return try {
            DocumentFile.fromTreeUri(context, uriString.toUri())?.canWrite() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

data class LocalBackupPayload(
    @Expose(deserialize = true, serialize = true)
    val version: Int,
    @Expose(deserialize = true, serialize = true)
    val dailyList: List<DailyWithMedia>,
    @Expose(deserialize = true, serialize = true)
    val labelList: List<DailyLabelEntity>
)
