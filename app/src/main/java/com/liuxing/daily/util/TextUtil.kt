/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import android.text.SpannableString
import android.text.style.ImageSpan

object TextUtil {

    private val mediaTagRegexes = listOf(
        Regex("<\\s*img\\b[^>]*>", RegexOption.IGNORE_CASE),
        Regex("<\\s*video\\b[^>]*>", RegexOption.IGNORE_CASE),
        Regex("<\\s*audio\\b[^>]*>", RegexOption.IGNORE_CASE),
    )

    private val unwrapTagRegexes = listOf(
        Regex("<b>([\\s\\S]*?)</b>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<i>([\\s\\S]*?)</i>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<u>([\\s\\S]*?)</u>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<s>([\\s\\S]*?)</s>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<color=[^>]+>([\\s\\S]*?)</color>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<bg=[^>]+>([\\s\\S]*?)</bg>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<size=[^>]+>([\\s\\S]*?)</size>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<a\\s+href=\"[^\"]*\"\\s*>([\\s\\S]*?)</a>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<blockquote>([\\s\\S]*?)</blockquote>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<align=(left|center|right)\\s*>([\\s\\S]*?)</align\\s*>", RegexOption.IGNORE_CASE) to "$2",
        Regex("<h([123])\\s*>([\\s\\S]*?)</h\\1\\s*>", RegexOption.IGNORE_CASE) to "$2",
        Regex("<li\\s+type=\"ul\"\\s*>([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<li\\s+type=\"ol\"(?:\\s+index=\"[^\"]*\")?\\s*>([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE) to "$1",
        Regex("<todo\\s+checked=\"(true|false)\"\\s*>([\\s\\S]*?)</todo>", RegexOption.IGNORE_CASE) to "$2",
    )

    private val separatorTagRegexes = listOf(
        Regex("<hr\\s*/?>", RegexOption.IGNORE_CASE),
    )

    /**
     * 获取字数
     *
     * @param text 要计算的文本
     * @return 字数
     */
    fun getWordCount(text: String): Int {
        val spannableText = SpannableString(stripRichTextTags(text, mediaReplacement = "", separatorReplacement = ""))
        val imageSpans = spannableText.getSpans(0, spannableText.length, ImageSpan::class.java)
        var totalLength = spannableText.length
        for (imageSpan in imageSpans) {
            val start = spannableText.getSpanStart(imageSpan)
            val end = spannableText.getSpanEnd(imageSpan)
            totalLength -= (end - start)
        }

        return maxOf(totalLength, 0)
    }

    /**
     * 替换标签
     *
     * @param text 文本
     * @param replacement 替代的文本
     * @return 替换结果
     */
    fun replaceTag(text: String, replacement: String = "..."): String {
        return stripRichTextTags(text, mediaReplacement = replacement, separatorReplacement = replacement)
    }

    private fun stripRichTextTags(
        text: String,
        mediaReplacement: String,
        separatorReplacement: String
    ): String {
        var strippedText = text
        mediaTagRegexes.forEach { regex ->
            strippedText = strippedText.replace(regex, mediaReplacement)
        }
        separatorTagRegexes.forEach { regex ->
            strippedText = strippedText.replace(regex, separatorReplacement)
        }
        unwrapTagRegexes.forEach { (regex, replacement) ->
            strippedText = strippedText.replace(regex, replacement)
        }
        return strippedText
    }

}