package com.liuxing.daily.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
/*
    fun copyImageToMyAppDir(context: Context, imageUri: Uri): String {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (externalFilesDir != null) {
            val copyImageName = UUID.randomUUID().toString() + ".jpg"
            val file = File(externalFilesDir, copyImageName)
            MediaStore.Images.Media.getBitmap(context.contentResolver,imageUri)
            context.contentResolver.openInputStream(imageUri).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }
            return file.absolutePath
        }
        return ""
    }
*/

    fun copyImageToMyAppDir(context: Context, imageUri: Uri): String {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (externalFilesDir != null) {
            val copyImageName = UUID.randomUUID().toString() + ".jpg"
            val file = File(externalFilesDir, copyImageName)
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }
            inputStream?.close()

            return file.absolutePath
        }
        return ""
    }

    fun copyVideoToMyAppDir(context: Context, videoUri: Uri): String {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (externalFilesDir != null) {
            val copyVideoName = UUID.randomUUID().toString() + ".mp4"
            val file = File(externalFilesDir, copyVideoName)

            try {
                val inputStream = context.contentResolver.openInputStream(videoUri)
                    ?: return ""
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(1024 * 4)
                var length: Int
                while (inputStream.read(buffer).also { length = it } != -1) {
                    outputStream.write(buffer, 0, length)
                }
                // 关流
                inputStream.close()
                outputStream.close()

                return file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return ""
    }

    /**
     * 复制音频文件到应用私有目录
     *
     * @param context 上下文
     * @param audioUri 音频文件的 URI
     * @return 复制后的音频文件路径
     */
    fun copyAudioToMyAppDir(context: Context, audioUri: Uri): String {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (externalFilesDir != null) {
            val copyAudioName = UUID.randomUUID().toString() + ".mp3"
            val file = File(externalFilesDir, copyAudioName)
            val inputStream: InputStream? = context.contentResolver.openInputStream(audioUri)
            if (inputStream != null) {
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
                // 关流
                inputStream.close()
                return file.absolutePath
            }
        }
        return ""
    }
}