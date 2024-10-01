package com.liuxing.daily.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

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
}