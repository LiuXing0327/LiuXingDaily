/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File

object ZipUtil {

    fun addFileToZipSafely(
        zipFile: ZipFile, file: File, folderInZip: String?, baseParams: ZipParameters
    ) {
        if (!file.exists()) return
        val params = ZipParameters().apply {
            isEncryptFiles = baseParams.isEncryptFiles
            encryptionMethod = baseParams.encryptionMethod
            aesKeyStrength = baseParams.aesKeyStrength
            fileNameInZip =
                if (folderInZip.isNullOrEmpty()) file.name else "${folderInZip}/${file.name}"
        }
        zipFile.addFile(file, params)
    }
}