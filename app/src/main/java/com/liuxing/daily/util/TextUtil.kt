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
        val textWithoutVideo = textWithoutImages.replace(Regex("<\\s*video[^>]*>"), "")
        val textWithoutAudio = textWithoutVideo.replace(Regex("<\\s*audio[^>]*>"), "")
        val spannableText = SpannableString(textWithoutAudio)
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
     * @return 替换结果
     */
    fun replaceTag(text: String): String {
        val imageRegex = Regex("<img\\s+src=\"(.*?)\"\\s*/?>", RegexOption.IGNORE_CASE)
        val videoRegex = Regex("<video\\s+src=\"(.*?)\"\\s*/?>", RegexOption.IGNORE_CASE)
        val audioRegex = Regex("<audio\\s+src=\"(.*?)\"\\s*/?>", RegexOption.IGNORE_CASE)
        var replacedText = text
        replacedText = replacedText.replace(imageRegex, "...")
        replacedText = replacedText.replace(videoRegex, "...")
        replacedText = replacedText.replace(audioRegex, "...")

        return replacedText
    }

}