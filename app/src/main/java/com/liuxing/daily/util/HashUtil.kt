/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import java.security.MessageDigest

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