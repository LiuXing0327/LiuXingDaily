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

package com.liuxing.daily.ui.datamanagement

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.backup.SafBackupManager
import com.liuxing.daily.backup.SafRestoreManager
import com.liuxing.daily.ui.compose.theme.DailyTheme
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.BackupEncryptionUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.launch

class DataManagementActivity : QRXActivity() {

    private var backupPath by mutableStateOf("")
    private var autoBackupEnabled by mutableStateOf(true)
    private var backupEncryptionEnabled by mutableStateOf(false)
    private var backupPassword by mutableStateOf("")
    private var isProcessing by mutableStateOf(false)
    private val snackbarHostState = SnackbarHostState()

    private val dailyViewModel by lazy {
        ViewModelProvider(this)[DailyViewModel::class.java]
    }

    private val pickBackupDirectoryLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri ?: return@registerForActivityResult
            handleBackupDirectoryPicked(uri)
        }

    private val pickBackupJsonLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@registerForActivityResult
            restoreBackupWithDetection(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DailyThemeManager.syncFromPreferences(this)

        backupPath = SafBackupManager.getUri(this)
        autoBackupEnabled = SharedPreferencesUtil.getBoolean(this, AUTO_BACKUP_KEY, true)
        backupEncryptionEnabled =
            SharedPreferencesUtil.getBoolean(this, BACKUP_ENCRYPTION_KEY, false)
        
        // 从本地解密加载密码
        val encryptedPassword = SharedPreferencesUtil.getString(this, BACKUP_PASSWORD_KEY, "")
        backupPassword = BackupEncryptionUtil.decryptLocal(encryptedPassword)

        setContent {
            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                DataManagementScreen(
                    backupPath = backupPath,
                    autoBackupEnabled = autoBackupEnabled,
                    backupEncryptionEnabled = backupEncryptionEnabled,
                    backupPassword = backupPassword,
                    isProcessing = isProcessing,
                    snackbarHostState = snackbarHostState,
                    onBack = ::finish,
                    onPickBackupPath = { pickBackupDirectoryLauncher.launch(null) },
                    onBackup = ::executeManualBackup,
                    onRestore = {
                        pickBackupJsonLauncher.launch(
                            arrayOf(
                                "application/json", "text/*"
                            )
                        )
                    },
                    onAutoBackupChanged = ::changeAutoBackupSetting,
                    onBackupEncryptionChanged = ::changeBackupEncryptionSetting,
                    onPasswordChanged = ::updateBackupPassword
                )
            }
        }
    }

    private fun handleBackupDirectoryPicked(uri: Uri) {
        val takeFlags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
        SafBackupManager.putUri(this, uri)
        backupPath = uri.toString()
    }

    private fun executeManualBackup() {
        if (backupPath.isBlank()) {
            SnackbarUtil.showSnackbarShort(
                snackbarHostState,
                lifecycleScope,
                getString(R.string.local_backup_path_not_selected)
            )
            return
        }

        lifecycleScope.launch {
            isProcessing = true
            val pwd =
                if (backupEncryptionEnabled && backupPassword.isNotBlank()) backupPassword else null
            val success =
                SafBackupManager.backup(this@DataManagementActivity, dailyViewModel, false, pwd)
            isProcessing = false
            SnackbarUtil.showSnackbarShort(
                snackbarHostState,
                lifecycleScope,
                getString(if (success) R.string.backup_success else R.string.failed_to_backup_data)
            )
        }
    }

    /**
     * 自动检测加密并优先尝试本地密码
     */
    private fun restoreBackupWithDetection(uri: Uri, manualPassword: String? = null) {
        lifecycleScope.launch {
            isProcessing = true

            // 尝试本地保存的备份密码（如果没有提供手动密码）
            val passwordToTry =
                manualPassword ?: backupPassword.ifBlank { null }

            val status = SafRestoreManager.restore(
                this@DataManagementActivity, uri, dailyViewModel, passwordToTry
            )
            isProcessing = false

            when (status) {
                SafRestoreManager.RestoreStatus.SUCCESS -> {
                    SnackbarUtil.showSnackbarShort(
                        snackbarHostState, lifecycleScope, getString(R.string.recovery_successful)
                    )
                }

                SafRestoreManager.RestoreStatus.NEED_PASSWORD -> {
                    // 如果文件加密但没设密码，或者本地密码不匹配（SafRestoreManager 对于错密码返回 WRONG_PASSWORD）
                    showRestorePasswordDialog(uri)
                }

                SafRestoreManager.RestoreStatus.WRONG_PASSWORD -> {
                    // 本地密码错误，提示手动输入
                    SnackbarUtil.showSnackbarShort(
                        snackbarHostState, lifecycleScope, getString(R.string.the_key_is_incorrect)
                    )
                    showRestorePasswordDialog(uri)
                }

                SafRestoreManager.RestoreStatus.FAILED -> {
                    SnackbarUtil.showSnackbarShort(
                        snackbarHostState, lifecycleScope, getString(R.string.recovery_failed)
                    )
                }
            }
        }
    }

    private fun showRestorePasswordDialog(uri: Uri) {
        var inputPassword: TextInputEditText? = null
        MaterialAlertDialogUtil.showDialog(
            this,
            title = getString(R.string.require_password_to_import),
            layoutRes = R.layout.dialog_input_password_layout,
            positiveText = getString(R.string.sure),
            onPositive = {
                val pwd = inputPassword?.text?.toString()
                if (!pwd.isNullOrBlank()) {
                    restoreBackupWithDetection(uri, pwd)
                }
            },
            neutralText = getString(R.string.cancel),
            onViewCreated = { view, _ ->
                inputPassword = view.findViewById(R.id.input_password)
                val layout = view.findViewById<TextInputLayout>(R.id.input_password_layout)
                layout.hint = getString(R.string.password)
            })
    }

    private fun changeAutoBackupSetting(enabled: Boolean) {
        autoBackupEnabled = enabled
        SharedPreferencesUtil.putBoolean(this, AUTO_BACKUP_KEY, enabled)
    }

    private fun changeBackupEncryptionSetting(enabled: Boolean) {
        backupEncryptionEnabled = enabled
        SharedPreferencesUtil.putBoolean(this, BACKUP_ENCRYPTION_KEY, enabled)
    }

    private fun updateBackupPassword(newPassword: String) {
        backupPassword = newPassword
        // 将密码加密后存入本地
        val encryptedPassword = BackupEncryptionUtil.encryptLocal(newPassword)
        SharedPreferencesUtil.putString(this, BACKUP_PASSWORD_KEY, encryptedPassword)
    }

    enum class ItemKey {
        LOCAL_BACKUP_PATH,
        IMPORT,
        EXPORT,
        BACKUP,
        RESTORE
    }

    companion object {
        const val AUTO_BACKUP_KEY = "auto_backup"
        const val BACKUP_ENCRYPTION_KEY = "backup_encryption_enabled"
        const val BACKUP_PASSWORD_KEY = "backup_password_value"
    }
}
