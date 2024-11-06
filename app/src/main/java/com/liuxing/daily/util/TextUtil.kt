package com.liuxing.daily.util

import android.text.SpannableString
import android.text.style.ImageSpan

/**
 * Author：流星
 * DateTime：2024/11/3 9:04
 * Description：文本工具类
 */
object TextUtil {

    /**
     * 获取字数
     *
     * @param text 要计算的文本
     * @return 字数
     */
    fun getWordCount(text: String): Int {
        val originalText = text
        val textWithoutImages = originalText.replace(Regex("<\\s*img[^>]*>"), "")
        val spannableText = SpannableString(textWithoutImages)
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
     * 替换图像标签
     *
     * @param text 文本
     * @return 替换结果
     */
    fun replaceImageTag(text: String): String {
        val regex = Regex("<img src=\"(.*?)\"/>")
        return text.replace(regex, "...")
    }
}