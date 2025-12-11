/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import com.google.android.material.color.MaterialColors

/**
 * 高亮工具类
 */
object HighlightUtil {

    /**
     * 对文本中的关键字进行背景高亮
     *
     * @param context 上下文
     * @param fullText 完整文本
     * @param keyword 搜索关键字
     */
    fun highlightKeyword(context: Context, fullText: String, keyword: String): SpannableString {
        val spannableString = SpannableString(fullText)
        if (fullText.isEmpty() || keyword.isEmpty()) return spannableString

        val lowerText = fullText.lowercase()
        val lowerKey = keyword.lowercase()

        var beginIndex = lowerText.indexOf(lowerKey)
        if (beginIndex == -1) return spannableString

        val highlightBgColor =
            MaterialColors.getColor(context, android.R.attr.textColorHighlight, 0)

        while (beginIndex != -1) {
            val endIndex = beginIndex + keyword.length
            spannableString.setSpan(
                BackgroundColorSpan(
                    highlightBgColor
                ), beginIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            beginIndex = lowerText.indexOf(lowerKey, endIndex)
        }

        return spannableString
    }
}