package com.liuxing.daily.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object CopyUtil {

    /**
     * 复制文本到剪贴板
     *
     * @param context 上下文
     * @param text 要复制的文本
     */
    fun copyTextToClipboard(context: Context, text: String) {
        val clipboardManager: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    /**
     * 复制图片到我的私有目录
     *
     * @param context 上下文
     * @param imageUri 图片路径
     * @return 返回图片路径
     */
    fun copyImageToMyAppDir(context: Context, imageUri: Uri): String {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (externalFilesDir != null) {
            val copyImageName = UUID.randomUUID().toString() + ".jpg"
            val file = File(externalFilesDir, copyImageName)
            context.contentResolver.openInputStream(imageUri).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }
            return file.absolutePath
        }
        return ""
    }
}