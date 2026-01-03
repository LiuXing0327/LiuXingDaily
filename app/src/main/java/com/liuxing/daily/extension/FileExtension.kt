/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import com.liuxing.daily.util.FileUtil
import java.io.File

/**
 * 检查文件是否存在
 *
 * @return true 存在，false 不存在。
 */
fun File.checkFileExistsToPath(): Boolean {
    return FileUtil().checkFileExists(absolutePath)
}

/**
 * 获取文件的 MD5 值
 *
 * @return 文件的 MD5 字符串
 */
fun File.getFileMD5(): String {
    return FileUtil().getFileMD5(absoluteFile)
}

/**
 * 文件重命名
 *
 * @param newName 新的名字
 *
 * @return true 成功，false 失败。
 */
fun File.rename(newName: String): Boolean {
    val newFile = File(parentFile, newName)
    return renameTo(newFile)
}