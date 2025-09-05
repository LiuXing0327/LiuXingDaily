/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.io.File
import java.security.MessageDigest

/**
 * 文件工具类
 */
class FileUtil {

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @param fileName 文件名
     * @return 文件是否存在
     */
    fun checkFileExists(filePath: String, fileName: String): Boolean {
        val file = File(filePath, fileName)
        return file.exists()
    }

    /**
     * 检查文件是否存在
     *
     * @param filePath 文件路径
     * @return 文件是否存在
     */
    fun checkFileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    /**
     * 检查文件是否存在
     *
     * @param file 文件
     * @return 文件是否存在
     */
    fun checkFileExists(file: File): Boolean = file.exists()

    /**
     * 检查文件是否存在
     *
     * @param sardine OkHttpSardine
     * @param url 链接
     * @return 文件是否存在
     */
    fun checkFileExists(sardine: OkHttpSardine, url: String): Boolean {
        val fileName = if (url.endsWith("/")) "醒悟/daily.zip" else "/醒悟/daily.zip"
        return sardine.exists("${url}${fileName}")
    }

    /**
     * 检查目录是否存在
     *
     * @param sardine OkHttpSardine
     * @param url 链接
     * @return 文件是否存在
     */
    fun checkDirExists(sardine: OkHttpSardine, url: String): Boolean {
        return sardine.list(url).any { it.name == "醒悟" }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件路径
     */
    fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    /**
     * 删除文件
     *
     * @param sardine OkHttpSardine
     * @param filePath 文件路径
     */
    fun deleteFile(sardine: OkHttpSardine, filePath: String) {
        val fileName = if (filePath.endsWith("/")) "醒悟/daily.zip" else "/醒悟/daily.zip"
        sardine.delete("${filePath}${fileName}")
    }

    /**
     * 是否是图片
     *
     * @param file 文件
     * @return 结果
     */
    fun isImageFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension == "jpg"
    }

    /**
     * 获取文件路径
     *
     * @param file 文件路径
     * @return 路径集合
     */
    fun getFilePaths(file: File): List<String> {
        val paths = mutableListOf<String>()
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (childFile in files) {
                    paths.addAll(getFilePaths(childFile))
                }
            }
        } else {
            paths.add(file.absolutePath)
        }
        return paths
    }

    /**
     * 获取文件的 MD5 值
     *
     * @param file 需要计算的文件
     * @return 文件的 MD5 字符串
     */
    fun getFileMD5(file: File): String {
        if (!checkFileExists(file)) return ""
        val messageDigest = MessageDigest.getInstance("MD5")
        file.inputStream().use { fis ->
            val buffer = ByteArray(1024)
            var len: Int
            while (fis.read(buffer).also { len = it } != -1) {
                messageDigest.update(buffer, 0, len)
            }
        }
        val bytes = messageDigest.digest()
        return bytes.joinToString("") { "%02x".format(it) }
    }
}