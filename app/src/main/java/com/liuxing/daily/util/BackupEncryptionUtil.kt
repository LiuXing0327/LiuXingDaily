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

package com.liuxing.daily.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 备份加密工具类 (AES-256 PBKDF2 用于文件, AES/GCM 用于本地存储)
 */
object BackupEncryptionUtil {

    // 文件加密常量 (PBKDF2)
    private const val FILE_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 16

    // 本地存储加密常量 (Keystore)
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val LOCAL_KEY_ALIAS = "wake_backup_local_key"
    private const val LOCAL_ALGORITHM = "AES/GCM/NoPadding"

    /**
     * 加密文件内容 (使用用户密码)
     */
    fun encrypt(data: String, password: String): String {
        val salt = generateRandomBytes(SALT_LENGTH)
        val iv = generateRandomBytes(IV_LENGTH)
        
        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance(FILE_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        val result = salt + iv + encryptedBytes
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    /**
     * 解密文件内容 (使用用户密码)
     */
    fun decrypt(encryptedData: String, password: String): String? {
        return try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            
            val salt = combined.sliceArray(0 until SALT_LENGTH)
            val iv = combined.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH)
            val encryptedBytes = combined.sliceArray(SALT_LENGTH + IV_LENGTH until combined.size)
            
            val secretKey = deriveKey(password, salt)
            val cipher = Cipher.getInstance(FILE_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 本地保存备份密码 (使用 Android Keystore 加密)
     */
    fun encryptLocal(text: String): String {
        if (text.isBlank()) return ""
        return try {
            val cipher = Cipher.getInstance(LOCAL_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateLocalKey())
            
            val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv
            
            // 存储格式: IV + EncryptedData
            val result = iv + encryptedBytes
            Base64.encodeToString(result, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 从本地存储解密备份密码
     */
    fun decryptLocal(encryptedText: String): String {
        if (encryptedText.isBlank()) return ""
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            val iv = combined.sliceArray(0 until 12) // GCM 默认 IV 长度 12
            val encryptedBytes = combined.sliceArray(12 until combined.size)
            
            val cipher = Cipher.getInstance(LOCAL_ALGORITHM)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateLocalKey(), spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            // 解密失败可能是因为 Key 变了，返回空
            ""
        }
    }

    private fun getOrCreateLocalKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val entry = keyStore.getEntry(LOCAL_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (entry != null) return entry.secretKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            LOCAL_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_LENGTH)
            .build()
        
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    private fun generateRandomBytes(length: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes
    }
}
