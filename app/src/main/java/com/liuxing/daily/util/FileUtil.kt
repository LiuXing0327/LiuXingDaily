package com.liuxing.daily.util

import java.io.File

/**
 * Author：流星
 * DateTime：2024/10/26 11:18
 * Description：文件工具类
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
}