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

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.liuxing.daily.R
import com.liuxing.daily.ui.compose.list.InputListItemCard
import com.liuxing.daily.ui.compose.list.ListItemCard
import com.liuxing.daily.ui.compose.list.ListItemData
import com.liuxing.daily.ui.compose.list.ListItemTrailing
import com.liuxing.daily.ui.compose.list.Subheader
import com.liuxing.daily.ui.compose.theme.DailyTheme
import androidx.core.net.toUri

private const val LOCAL_BACKUP_ITEM_COUNT = 1
private const val BACKUP_RESTORE_ITEM_COUNT = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    backupPath: String,
    autoBackupEnabled: Boolean,
    backupEncryptionEnabled: Boolean,
    backupPassword: String,
    isProcessing: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onPickBackupPath: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAutoBackupChanged: (Boolean) -> Unit,
    onBackupEncryptionChanged: (Boolean) -> Unit,
    onPasswordChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val displayPath = remember(backupPath) {
        getDisplayPath(context, backupPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.data_management)) },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(8.dp).size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Subheader(stringResource(R.string.local_backup_path))
            ListItemCard(
                data = ListItemData(
                    icon = Icons.Filled.Folder,
                    title = stringResource(R.string.local_backup_path),
                    supportingText = displayPath.ifBlank { stringResource(R.string.local_backup_path_not_selected) },
                    index = 0, count = LOCAL_BACKUP_ITEM_COUNT
                ),
                onAction = onPickBackupPath
            )

            Subheader(stringResource(R.string.backup_and_restore))
            ListItemCard(
                data = ListItemData(
                    icon = Icons.Filled.CloudUpload,
                    title = stringResource(R.string.backup),
                    supportingText = if (isProcessing) stringResource(R.string.processing) else stringResource(R.string.backup_supporting_text),
                    index = 0, count = BACKUP_RESTORE_ITEM_COUNT
                ),
                onAction = { if (!isProcessing) onBackup() }
            )
            ListItemCard(
                data = ListItemData(
                    icon = Icons.Filled.Restore,
                    title = stringResource(R.string.restore_2),
                    supportingText = if (isProcessing) stringResource(R.string.processing) else stringResource(R.string.restore_supporting_text),
                    index = 1, count = BACKUP_RESTORE_ITEM_COUNT
                ),
                onAction = { if (!isProcessing) onRestore() }
            )
            ListItemCard(
                data = ListItemData(
                    icon = Icons.Filled.Autorenew,
                    title = stringResource(R.string.auto_backup),
                    supportingText = stringResource(R.string.auto_backup_supporting_text),
                    index = 2, count = BACKUP_RESTORE_ITEM_COUNT
                ),
                trailing = ListItemTrailing.Switch(autoBackupEnabled),
                onAction = { onAutoBackupChanged(!autoBackupEnabled) }
            )

            InputListItemCard(
                data = ListItemData(
                    icon = Icons.Filled.Lock,
                    title = stringResource(R.string.backup_encryption),
                    supportingText = stringResource(R.string.backup_encryption_supporting_text),
                    index = 3, count = BACKUP_RESTORE_ITEM_COUNT
                ),
                enabled = backupEncryptionEnabled,
                value = backupPassword,
                placeholder = stringResource(R.string.leave_blank_for_no_encryption),
                onValueChange = onPasswordChanged,
                onEnabledChange = onBackupEncryptionChanged
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun getDisplayPath(context: android.content.Context, uriString: String): String {
    if (uriString.isBlank()) return ""
    return try {
        val uri = uriString.toUri()
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        if (documentFile != null && documentFile.exists()) {
            val path = uri.path ?: ""
            val decodedPath = Uri.decode(path)
            if (decodedPath.contains(":")) decodedPath.substringAfterLast(":") else documentFile.name ?: uriString
        } else ""
    } catch (_: Exception) { "" }
}

@Preview(showBackground = true)
@Composable
fun DataManagementScreenPreview() {
    DailyTheme {
        DataManagementScreen(
            backupPath = "", autoBackupEnabled = true, backupEncryptionEnabled = true,
            backupPassword = "password123", isProcessing = false, snackbarHostState = SnackbarHostState(),
            onBack = {}, onPickBackupPath = {}, onBackup = {}, onRestore = {},
            onAutoBackupChanged = {}, onBackupEncryptionChanged = {}, onPasswordChanged = {}
        )
    }
}
