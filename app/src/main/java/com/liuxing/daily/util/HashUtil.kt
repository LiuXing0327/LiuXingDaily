package com.liuxing.daily.util

import java.security.MessageDigest
import java.util.Objects

/**
 * Author：流星
 * DateTime：2024/10/13 下午4:55
 * Description：哈希工具类
 */
object HashUtil {

    fun hashSHA256(input: String): String {
        if (input != "") {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val bytes = messageDigest.digest(input.toByteArray())
            return bytes.joinToString("") { String.format("%02x", it) }
        }else{
            return ""
        }
    }
}