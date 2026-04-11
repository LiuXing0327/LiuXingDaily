/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import com.google.android.material.color.MaterialColors
import com.liuxing.daily.markdown.span.InlineBackgroundSpan
import com.liuxing.daily.markdown.span.SearchHighlightSpan

/**
 * 搜索高亮工具。
 */
object HighlightUtil {

    /**
     * 在纯文本上创建搜索高亮结果。
     *
     * @param context 上下文
     * @param fullText 完整文本
     * @param keyword 搜索关键字
     */
    fun highlightKeyword(context: Context, fullText: String, keyword: String): SpannableString {
        val spannableString = SpannableString(fullText)
        highlightKeyword(context, spannableString, keyword)
        return spannableString
    }

    /**
     * 在现有 Spannable 上直接应用搜索高亮。
     *
     * 这样不会丢失已有的富文本样式，并且搜索高亮会覆盖正文背景色。
     */
    fun highlightKeyword(context: Context, spannable: Spannable, keyword: String) {
        spannable.getSpans(0, spannable.length, SearchHighlightSpan::class.java)
            .forEach { spannable.removeSpan(it) }

        if (spannable.isEmpty() || keyword.isEmpty()) return

        val fullText = spannable.toString()
        val lowerText = fullText.lowercase()
        val lowerKey = keyword.lowercase()
        val highlightBgColor =
            MaterialColors.getColor(context, android.R.attr.textColorHighlight, 0)
        val ranges = mutableListOf<IntRange>()
        val highlightFlags =
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE or
                (0xFF shl Spanned.SPAN_PRIORITY_SHIFT)

        var beginIndex = lowerText.indexOf(lowerKey)
        while (beginIndex != -1) {
            val endIndex = beginIndex + keyword.length
            ranges += beginIndex until endIndex
            beginIndex = lowerText.indexOf(lowerKey, endIndex)
        }

        if (ranges.isEmpty()) return

        overrideInlineBackgrounds(spannable, ranges)

        ranges.forEach { range ->
            val start = range.first
            val end = range.last + 1
            spannable.setSpan(
                SearchHighlightSpan(highlightBgColor),
                start,
                end,
                highlightFlags
            )
        }
    }

    private fun overrideInlineBackgrounds(spannable: Spannable, ranges: List<IntRange>) {
        spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
            .forEach { span ->
                val spanStart = spannable.getSpanStart(span)
                val spanEnd = spannable.getSpanEnd(span)
                if (spanStart >= spanEnd) return@forEach
                val remainingRanges = subtractRanges(spanStart, spanEnd, ranges)
                if (remainingRanges.size == 1 &&
                    remainingRanges[0].first == spanStart &&
                    remainingRanges[0].last + 1 == spanEnd
                ) {
                    return@forEach
                }
                spannable.removeSpan(span)
                remainingRanges.forEach { range ->
                    spannable.setSpan(
                        BackgroundColorSpan(span.backgroundColor),
                        range.first,
                        range.last + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

        spannable.getSpans(0, spannable.length, InlineBackgroundSpan::class.java)
            .forEach { span ->
                val spanStart = spannable.getSpanStart(span)
                val spanEnd = spannable.getSpanEnd(span)
                if (spanStart >= spanEnd) return@forEach
                val remainingRanges = subtractRanges(spanStart, spanEnd, ranges)
                if (remainingRanges.size == 1 &&
                    remainingRanges[0].first == spanStart &&
                    remainingRanges[0].last + 1 == spanEnd
                ) {
                    return@forEach
                }
                spannable.removeSpan(span)
                remainingRanges.forEach { range ->
                    spannable.setSpan(
                        InlineBackgroundSpan(span.backgroundColor),
                        range.first,
                        range.last + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
    }

    private fun subtractRanges(
        spanStart: Int,
        spanEnd: Int,
        ranges: List<IntRange>
    ): List<IntRange> {
        val overlaps = ranges
            .mapNotNull { range ->
                val overlapStart = maxOf(spanStart, range.first)
                val overlapEnd = minOf(spanEnd, range.last + 1)
                if (overlapStart < overlapEnd) overlapStart until overlapEnd else null
            }
            .sortedBy { it.first }

        if (overlaps.isEmpty()) return listOf(spanStart until spanEnd)

        val result = mutableListOf<IntRange>()
        var cursor = spanStart
        overlaps.forEach { overlap ->
            if (cursor < overlap.first) {
                result += cursor until overlap.first
            }
            cursor = maxOf(cursor, overlap.last + 1)
        }
        if (cursor < spanEnd) {
            result += cursor until spanEnd
        }
        return result
    }
}