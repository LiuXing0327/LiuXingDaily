/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.view

import android.graphics.Typeface
import android.text.Annotation
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.core.graphics.toColorInt
import com.liuxing.daily.markdown.span.BulletSpan
import com.liuxing.daily.markdown.span.HorizontalRuleSpan
import com.liuxing.daily.markdown.span.InlineBackgroundSpan
import com.liuxing.daily.markdown.span.OrderedListSpan
import com.liuxing.daily.markdown.span.QuoteBlockSpan
import com.liuxing.daily.markdown.span.TodoSpan
import kotlin.math.max
import kotlin.math.min

/**
 * 日记富文本。
 */
object DailyRichText {

    const val ANNOTATION_KEY = "daily_rich_text"
    const val HR_MARKER = "\u200B"
    const val HR_PLACEHOLDER = "\n$HR_MARKER\n"
    const val EMPTY_BLOCK_MARKER = "\u2060"
    const val PLAIN_EXIT_MARKER = "\u2063"

    /**
     * 预设字号映射。
     *
     * `token` 用于导出标签值，`factor` 用于编辑时的相对字号缩放。
     */
    enum class FontPreset(val token: String, val factor: Float) {
        SMALL("small", 0.85f),
        BODY("body", 1.0f),
        TITLE("title", 1.3f)
    }

    /**
     * 解析导入的标签文本，并为 builder 挂上对应的可视 Span 和 Annotation。
     */
    fun applyMarkup(builder: SpannableStringBuilder) {
        applyListTags(builder)
        applyTodoTags(builder)
        applySimpleTag(builder, "b") { listOf(StyleSpan(Typeface.BOLD)) }
        applySimpleTag(builder, "i") { listOf(StyleSpan(Typeface.ITALIC)) }
        applySimpleTag(builder, "u") { listOf(UnderlineSpan()) }
        applySimpleTag(builder, "s") { listOf(StrikethroughSpan()) }
        applyHeadings(builder)
        applyColor(builder)
        applyBackground(builder)
        applySize(builder)
        applyLink(builder)
        applyQuote(builder)
        applyAlign(builder)
        applyHorizontalRule(builder)
    }

    /**
     * 将当前编辑态文本导出为带标签的持久化文本。
     *
     * 段落类 Span 会借助 Annotation 还原为对应标签；
     * 内部使用的占位锚点和分割线标记会在这里统一剥离。
     */
    fun export(editable: Editable): String {
        if (editable.isEmpty()) return ""

        val events = mutableListOf<Triple<Int, Boolean, String>>()
        editable.getSpans(0, editable.length, Annotation::class.java)
            .filter { it.key == ANNOTATION_KEY }
            .forEach { annotation ->
                val start = editable.getSpanStart(annotation)
                val end = editable.getSpanEnd(annotation)
                if (start !in 0..<end) return@forEach
                val tags = annotationToTags(annotation.value) ?: return@forEach
                val effectiveEnd = if (
                    isBlockAnnotation(annotation.value) && editable[end - 1] == '\n'
                ) {
                    end - 1
                } else {
                    end
                }
                events.add(Triple(start, true, tags.first))
                events.add(Triple(effectiveEnd, false, tags.second))
            }

        events.sortWith(
            compareBy<Triple<Int, Boolean, String>> { it.first }
                .thenBy { if (it.second) 1 else 0 }
                .thenByDescending { it.third.length }
        )

        val rawText = editable.toString()
        val builder = StringBuilder()
        var currentIndex = 0
        var eventIndex = 0
        while (eventIndex < events.size) {
            val position = events[eventIndex].first
            if (position > currentIndex) {
                builder.append(rawText.substring(currentIndex, position))
            }
            while (eventIndex < events.size && events[eventIndex].first == position) {
                builder.append(events[eventIndex].third)
                eventIndex++
            }
            currentIndex = position
        }
        if (currentIndex < rawText.length) {
            builder.append(rawText.substring(currentIndex))
        }

        val exportedText = builder.toString()
            .replace(HR_PLACEHOLDER, "<hr/>")
            .let { text ->
                if (text.startsWith("$HR_MARKER\n")) {
                    "<hr/>" + text.removePrefix("$HR_MARKER\n")
                } else {
                    text
                }
            }
            .replace(HR_MARKER, "<hr/>")
            .replace(EMPTY_BLOCK_MARKER, "")
            .replace(PLAIN_EXIT_MARKER, "")
        return exportedText
    }

    /**
     * 为一段文本附加内部 Annotation，后续导出和归一化都依赖它做语义判断。
     */
    fun addAnnotation(
        editable: Editable,
        start: Int,
        end: Int,
        value: String,
        flags: Int = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    ) {
        editable.setSpan(Annotation(ANNOTATION_KEY, value), start, end, flags)
    }

    /**
     * 删除满足条件的 Annotation，并在必要时拆分被部分覆盖的范围。
     */
    fun removeAnnotations(
        editable: Editable,
        start: Int,
        end: Int,
        predicate: (String) -> Boolean
    ) {
        editable.getSpans(start, end, Annotation::class.java)
            .filter { it.key == ANNOTATION_KEY && predicate(it.value) }
            .forEach { annotation ->
                splitSpan(editable, annotation, start, end) {
                    Annotation(
                        annotation.key,
                        annotation.value
                    )
                }
            }
    }

    /** 移除指定粗体或斜体样式。 */
    fun removeStyleSpans(editable: Editable, start: Int, end: Int, style: Int) {
        editable.getSpans(start, end, StyleSpan::class.java)
            .filter { it.style == style }
            .forEach { span -> splitSpan(editable, span, start, end) { StyleSpan(span.style) } }
    }

    /** 移除下划线样式。 */
    fun removeUnderlineSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, UnderlineSpan::class.java)
            .forEach { span -> splitSpan(editable, span, start, end) { UnderlineSpan() } }
    }

    /** 移除删除线样式。 */
    fun removeStrikeSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, StrikethroughSpan::class.java)
            .forEach { span -> splitSpan(editable, span, start, end) { StrikethroughSpan() } }
    }

    /** 移除前景色样式。 */
    fun removeForegroundSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, ForegroundColorSpan::class.java)
            .forEach { span ->
                splitSpan(editable, span, start, end) { ForegroundColorSpan(span.foregroundColor) }
            }
    }

    /** 移除背景高亮样式。 */
    fun removeBackgroundSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, InlineBackgroundSpan::class.java)
            .forEach { span ->
                splitSpan(editable, span, start, end) { InlineBackgroundSpan(span.backgroundColor) }
            }
        editable.getSpans(start, end, BackgroundColorSpan::class.java)
            .forEach { span ->
                splitSpan(editable, span, start, end) { BackgroundColorSpan(span.backgroundColor) }
            }
    }

    /** 移除字号缩放样式。 */
    fun removeRelativeSizeSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, RelativeSizeSpan::class.java)
            .forEach { span ->
                splitSpan(
                    editable,
                    span,
                    start,
                    end
                ) { RelativeSizeSpan(span.sizeChange) }
            }
    }

    /** 移除链接样式。 */
    fun removeUrlSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, URLSpan::class.java)
            .forEach { span -> splitSpan(editable, span, start, end) { URLSpan(span.url) } }
    }

    /** 移除引用块样式。 */
    fun removeQuoteSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, QuoteBlockSpan::class.java)
            .forEach { span -> splitSpan(editable, span, start, end) { QuoteBlockSpan() } }
    }

    /** 移除段落对齐样式。 */
    fun removeAlignmentSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, AlignmentSpan.Standard::class.java)
            .forEach { span ->
                splitSpan(editable, span, start, end) { AlignmentSpan.Standard(span.alignment) }
            }
    }

    /** 移除无序列表样式。 */
    fun removeBulletSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, BulletSpan::class.java)
            .forEach { span -> splitSpan(editable, span, start, end) { BulletSpan() } }
    }

    /** 移除有序列表样式。 */
    fun removeOrderedListSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, OrderedListSpan::class.java)
            .forEach { span ->
                splitSpan(editable, span, start, end) { OrderedListSpan(span.index) }
            }
    }

    /** 移除待办列表样式。 */
    fun removeTodoSpans(editable: Editable, start: Int, end: Int) {
        editable.getSpans(start, end, TodoSpan::class.java)
            .forEach { span ->
                splitSpan(editable, span, start, end) { TodoSpan(span.checked) }
            }
    }

    /**
     * 规范化标题范围。
     *
     * 标题只允许作用于单个段落，超出换行的部分会被裁回当前段。
     */
    fun normalizeHeadingSpans(editable: Editable) {
        editable.getSpans(0, editable.length, Annotation::class.java)
            .filter { it.key == ANNOTATION_KEY && it.value.matches(Regex("h[123]")) }
            .forEach { annotation ->
                val level = annotation.value.removePrefix("h").toIntOrNull() ?: return@forEach
                val start = editable.getSpanStart(annotation)
                val end = editable.getSpanEnd(annotation)
                if (start !in 0..<end) return@forEach

                val paragraphBreak = editable.toString().indexOf('\n', start)
                val normalizedEnd = when {
                    paragraphBreak == -1 || paragraphBreak >= end -> end
                    else -> paragraphBreak + 1
                }

                if (normalizedEnd == end) return@forEach

                removeHeadingSpans(editable, start, end)
                applyHeadingSpans(editable, start, normalizedEnd, level)
            }
    }

    /**
     * 合并连续引用段，避免多次编辑后出现重叠引用线。
     */
    fun normalizeQuoteSpans(editable: Editable) {
        val quoteRanges = lineRanges(editable)
            .filter { (start, end) ->
                findLatestAnnotation(editable, start, end) { it == "blockquote" } != null
            }
            .sortedWith(compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second })

        if (quoteRanges.isEmpty()) return

        val mergedRanges = mutableListOf<Pair<Int, Int>>()
        quoteRanges.forEach { range ->
            val last = mergedRanges.lastOrNull()
            if (last == null || range.first > last.second) {
                mergedRanges.add(range)
            } else {
                mergedRanges[mergedRanges.lastIndex] = last.first to max(last.second, range.second)
            }
        }

        removeQuoteSpans(editable, 0, editable.length)
        removeAnnotations(editable, 0, editable.length) { it == "blockquote" }
        mergedRanges.forEach { (start, end) ->
            applyQuoteSpans(editable, start, end)
        }
    }

    /**
     * 逐行重建列表样式，确保每一行只有一层有效列表 Span。
     */
    fun normalizeListSpans(editable: Editable) {
        val lineStates = lineRanges(editable).mapNotNull { (start, end) ->
            val latest = findLatestAnnotation(editable, start, end) { value ->
                value == "ul" || value.startsWith("ol:") || value.startsWith("todo:")
            } ?: return@mapNotNull null
            start to (end to latest.value)
        }

        removeBulletSpans(editable, 0, editable.length)
        removeOrderedListSpans(editable, 0, editable.length)
        removeTodoSpans(editable, 0, editable.length)
        removeAnnotations(editable, 0, editable.length) {
            it == "ul" || it.startsWith("ol:") || it.startsWith("todo:")
        }

        lineStates.forEach { (start, endAndValue) ->
            val end = endAndValue.first
            val value = endAndValue.second
            when {
                value == "ul" -> applyBulletListSpans(editable, start, end)
                value.startsWith("ol:") -> {
                    value.removePrefix("ol:").toIntOrNull()?.let { index ->
                        applyOrderedListSpans(editable, start, end, index)
                    }
                }

                value.startsWith("todo:") -> {
                    val checked = value.removePrefix("todo:").toBooleanStrictOrNull() ?: false
                    applyTodoSpans(editable, start, end, checked)
                }
            }
        }
    }

    /**
     * 归并连续对齐段，消除多次切换后叠加的对齐 Span。
     */
    fun normalizeAlignmentSpans(editable: Editable) {
        val alignmentLines = lineRanges(editable).mapNotNull { (start, end) ->
            val latest = findLatestAnnotation(editable, start, end) { it.startsWith("align:") }
                ?: return@mapNotNull null
            Triple(start, end, latest.value.removePrefix("align:"))
        }

        removeAlignmentSpans(editable, 0, editable.length)
        removeAnnotations(editable, 0, editable.length) { it.startsWith("align:") }

        if (alignmentLines.isEmpty()) return

        var blockStart = alignmentLines.first().first
        var blockEnd = alignmentLines.first().second
        var blockToken = alignmentLines.first().third

        alignmentLines.drop(1).forEach { (start, end, token) ->
            if (start == blockEnd && token == blockToken) {
                blockEnd = end
            } else {
                applyAlignmentSpans(editable, blockStart, blockEnd, tokenToAlignment(blockToken))
                blockStart = start
                blockEnd = end
                blockToken = token
            }
        }
        applyAlignmentSpans(editable, blockStart, blockEnd, tokenToAlignment(blockToken))
    }

    /** 应用标题段落样式。 */
    fun applyHeadingSpans(editable: Editable, start: Int, end: Int, level: Int) {
        val (safeStart, safeEnd) = normalizeParagraphRange(editable, start, end)
        if (safeEnd <= safeStart) return
        val factor = when (level) {
            1 -> 1.6f
            2 -> 1.4f
            else -> 1.2f
        }
        editable.setSpan(StyleSpan(Typeface.BOLD), safeStart, safeEnd, Spannable.SPAN_PARAGRAPH)
        editable.setSpan(RelativeSizeSpan(factor), safeStart, safeEnd, Spannable.SPAN_PARAGRAPH)
        addAnnotation(editable, safeStart, safeEnd, "h$level", Spannable.SPAN_PARAGRAPH)
    }

    /** 应用预设字号样式。 */
    fun applyFontPresetSpans(editable: Editable, start: Int, end: Int, preset: FontPreset) {
        editable.setSpan(
            RelativeSizeSpan(preset.factor),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        addAnnotation(editable, start, end, "size:${preset.token}")
    }

    /** 应用文字颜色样式。 */
    fun applyColorSpans(editable: Editable, start: Int, end: Int, color: Int) {
        val hex = colorToHex(color)
        editable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        addAnnotation(editable, start, end, "color:$hex")
    }

    /** 应用背景高亮样式。 */
    fun applyHighlightSpans(editable: Editable, start: Int, end: Int, color: Int) {
        val hex = colorToHex(color)
        splitInlineRangeByNewline(editable, start, end).forEach { (segmentStart, segmentEnd) ->
            editable.setSpan(
                InlineBackgroundSpan(color),
                segmentStart,
                segmentEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        addAnnotation(editable, start, end, "bg:$hex")
    }

    /** 应用链接样式。 */
    fun applyLinkSpans(editable: Editable, start: Int, end: Int, url: String) {
        editable.setSpan(URLSpan(url), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        addAnnotation(editable, start, end, "a:$url")
    }

    /** 应用引用块样式。 */
    fun applyQuoteSpans(editable: Editable, start: Int, end: Int) {
        val (safeStart, safeEnd) = normalizeParagraphRange(editable, start, end)
        if (safeEnd <= safeStart) return
        editable.setSpan(QuoteBlockSpan(), safeStart, safeEnd, Spannable.SPAN_PARAGRAPH)
        addAnnotation(editable, safeStart, safeEnd, "blockquote", Spannable.SPAN_PARAGRAPH)
    }

    /** 应用段落对齐样式。 */
    fun applyAlignmentSpans(editable: Editable, start: Int, end: Int, alignment: Layout.Alignment) {
        val (safeStart, safeEnd) = normalizeParagraphRange(editable, start, end)
        if (safeEnd <= safeStart) return
        editable.setSpan(
            AlignmentSpan.Standard(alignment),
            safeStart,
            safeEnd,
            Spannable.SPAN_PARAGRAPH
        )
        addAnnotation(
            editable,
            safeStart,
            safeEnd,
            "align:${alignmentToToken(alignment)}",
            Spannable.SPAN_PARAGRAPH
        )
    }

    /** 应用无序列表样式。 */
    fun applyBulletListSpans(editable: Editable, start: Int, end: Int) {
        val (safeStart, safeEnd) = normalizeParagraphRange(editable, start, end)
        if (safeEnd <= safeStart) return
        editable.setSpan(BulletSpan(), safeStart, safeEnd, Spannable.SPAN_PARAGRAPH)
        addAnnotation(editable, safeStart, safeEnd, "ul", Spannable.SPAN_PARAGRAPH)
    }

    /** 应用有序列表样式。 */
    fun applyOrderedListSpans(editable: Editable, start: Int, end: Int, index: Int) {
        val (safeStart, safeEnd) = normalizeParagraphRange(editable, start, end)
        if (safeEnd <= safeStart) return
        editable.setSpan(OrderedListSpan(index), safeStart, safeEnd, Spannable.SPAN_PARAGRAPH)
        addAnnotation(editable, safeStart, safeEnd, "ol:$index", Spannable.SPAN_PARAGRAPH)
    }

    /** 应用待办列表样式。 */
    fun applyTodoSpans(editable: Editable, start: Int, end: Int, checked: Boolean) {
        val (safeStart, safeEnd) = normalizeParagraphRange(editable, start, end)
        if (safeEnd <= safeStart) return
        editable.setSpan(TodoSpan(checked), safeStart, safeEnd, Spannable.SPAN_PARAGRAPH)
        addAnnotation(editable, safeStart, safeEnd, "todo:$checked", Spannable.SPAN_PARAGRAPH)
    }

    /** 为分割线占位字符挂上可视分割线 Span。 */
    fun applyHorizontalRuleSpan(editable: Editable, markerIndex: Int) {
        val start = markerIndex.coerceAtLeast(0)
        val end = (markerIndex + HR_MARKER.length).coerceAtMost(editable.length)
        if (start >= end) return
        editable.setSpan(HorizontalRuleSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /** 将颜色值转换成统一的十六进制字符串，便于导出。 */
    fun colorToHex(color: Int): String = String.format("#%08X", color)

    /**
     * 解析成对的简单行内标签，例如粗体、斜体、下划线、删除线。
     */
    private fun applySimpleTag(
        builder: SpannableStringBuilder,
        tag: String,
        visualFactory: () -> List<Any>
    ) {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.IGNORE_CASE)
        regex.findAll(builder.toString()).toList().reversed().forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            val inner = SpannableStringBuilder(
                builder.subSequence(
                    start + tag.length + 2,
                    end - tag.length - 3
                )
            )
            builder.replace(start, end, inner)
            val newEnd = start + inner.length
            visualFactory().forEach { span ->
                builder.setSpan(span, start, newEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            addAnnotation(builder, start, newEnd, tag)
        }
    }

    /** 解析标题标签。 */
    private fun applyHeadings(builder: SpannableStringBuilder) {
        Regex(
            "<h([123])\\s*>([\\s\\S]*?)</h\\1\\s*>",
            RegexOption.IGNORE_CASE
        )
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val level = match.groupValues[1].toInt()
                val innerRange = match.groups[2]?.range ?: return@forEach
                val inner = SpannableStringBuilder(
                    builder.subSequence(innerRange.first, innerRange.last + 1)
                )
                builder.replace(start, end, inner)
                val headingEnd = inner.indexOf('\n').let { if (it == -1) inner.length else it }
                if (headingEnd > 0) {
                    applyHeadingSpans(builder, start, start + headingEnd, level)
                }
            }
    }

    /** 解析文字颜色标签。 */
    private fun applyColor(builder: SpannableStringBuilder) {
        Regex("<color=(#[0-9A-Fa-f]{6,8})>(.*?)</color>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val colorValue = match.groupValues[1]
                val inner = SpannableStringBuilder(
                    builder.subSequence(
                        start + "<color=$colorValue>".length,
                        end - "</color>".length
                    )
                )
                builder.replace(start, end, inner)
                applyColorSpans(builder, start, start + inner.length, colorValue.toColorInt())
            }
    }

    /** 解析背景高亮标签。 */
    private fun applyBackground(builder: SpannableStringBuilder) {
        Regex("<bg=(#[0-9A-Fa-f]{6,8})>(.*?)</bg>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val colorValue = match.groupValues[1]
                val inner = SpannableStringBuilder(
                    builder.subSequence(start + "<bg=$colorValue>".length, end - "</bg>".length)
                )
                builder.replace(start, end, inner)
                applyHighlightSpans(builder, start, start + inner.length, colorValue.toColorInt())
            }
    }

    /** 解析字号标签。 */
    private fun applySize(builder: SpannableStringBuilder) {
        Regex("<size=([^>]+)>(.*?)</size>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val rawValue = match.groupValues[1]
                val inner = SpannableStringBuilder(
                    builder.subSequence(start + "<size=$rawValue>".length, end - "</size>".length)
                )
                builder.replace(start, end, inner)
                val newEnd = start + inner.length
                val preset = FontPreset.entries.firstOrNull { it.token.equals(rawValue, true) }
                if (preset != null) {
                    applyFontPresetSpans(builder, start, newEnd, preset)
                } else {
                    val factor = rawValue.toFloatOrNull() ?: 1f
                    builder.setSpan(
                        RelativeSizeSpan(factor),
                        start,
                        newEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    addAnnotation(builder, start, newEnd, "size:$rawValue")
                }
            }
    }

    /** 解析链接标签。 */
    private fun applyLink(builder: SpannableStringBuilder) {
        Regex("<a href=\"(.*?)\">(.*?)</a>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val url = match.groupValues[1]
                val inner = SpannableStringBuilder(
                    builder.subSequence(start + "<a href=\"$url\">".length, end - "</a>".length)
                )
                builder.replace(start, end, inner)
                applyLinkSpans(builder, start, start + inner.length, url)
            }
    }

    /** 解析引用块标签。 */
    private fun applyQuote(builder: SpannableStringBuilder) {
        Regex(
            "<blockquote>(.*?)</blockquote>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val inner = SpannableStringBuilder(
                    builder.subSequence(start + "<blockquote>".length, end - "</blockquote>".length)
                )
                builder.replace(start, end, inner)
                applyQuoteSpans(builder, start, start + inner.length)
            }
    }

    /** 解析对齐标签。 */
    private fun applyAlign(builder: SpannableStringBuilder) {
        Regex(
            "<align=(left|center|right)\\s*>([\\s\\S]*?)</align\\s*>",
            RegexOption.IGNORE_CASE
        )
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val alignToken = match.groupValues[1].lowercase()
                val innerRange = match.groups[2]?.range ?: return@forEach
                val inner = SpannableStringBuilder(
                    builder.subSequence(
                        innerRange.first,
                        innerRange.last + 1
                    )
                )
                builder.replace(start, end, inner)
                applyAlignmentSpans(
                    builder,
                    start,
                    start + inner.length,
                    tokenToAlignment(alignToken)
                )
            }
    }

    /** 解析分割线标签。 */
    private fun applyHorizontalRule(builder: SpannableStringBuilder) {
        Regex("<hr\\s*/?>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val replacement = if (match.range.first == 0) {
                    "$HR_MARKER\n"
                } else {
                    HR_PLACEHOLDER
                }
                builder.replace(match.range.first, match.range.last + 1, replacement)
                val markerOffset = replacement.indexOf(HR_MARKER)
                if (markerOffset != -1) {
                    applyHorizontalRuleSpan(builder, match.range.first + markerOffset)
                }
            }
    }

    /** 移除标题相关的视觉 Span 与 Annotation。 */
    private fun removeHeadingSpans(editable: Editable, start: Int, end: Int) {
        removeRelativeSizeSpans(editable, start, end)
        removeStyleSpans(editable, start, end, Typeface.BOLD)
        removeAnnotations(editable, start, end) { it.matches(Regex("h[123]")) }
    }

    /** 解析无序和有序列表标签。 */
    private fun applyListTags(builder: SpannableStringBuilder) {
        Regex("<li type=\"ul\">([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val inner = SpannableStringBuilder(match.groupValues[1])
                builder.replace(start, end, inner)
                applyBulletListSpans(builder, start, start + inner.length)
            }

        Regex("<li type=\"ol\" index=\"(\\d+)\">([\\s\\S]*?)</li>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val inner = SpannableStringBuilder(match.groupValues[2])
                builder.replace(start, end, inner)
                applyOrderedListSpans(
                    builder,
                    start,
                    start + inner.length,
                    match.groupValues[1].toInt()
                )
            }
    }

    /** 解析待办列表标签。 */
    private fun applyTodoTags(builder: SpannableStringBuilder) {
        Regex("<todo checked=\"(true|false)\">([\\s\\S]*?)</todo>", RegexOption.IGNORE_CASE)
            .findAll(builder.toString())
            .toList()
            .reversed()
            .forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                val inner = SpannableStringBuilder(match.groupValues[2])
                builder.replace(start, end, inner)
                applyTodoSpans(
                    builder,
                    start,
                    start + inner.length,
                    match.groupValues[1].equals("true", true)
                )
            }
    }

    /** 将内部 Annotation 值映射回导出标签。 */
    private fun annotationToTags(value: String): Pair<String, String>? {
        return when {
            value == "b" -> "<b>" to "</b>"
            value == "i" -> "<i>" to "</i>"
            value == "u" -> "<u>" to "</u>"
            value == "s" -> "<s>" to "</s>"
            value == "ul" -> "<li type=\"ul\">" to "</li>"
            value.startsWith("ol:") -> "<li type=\"ol\" index=\"${value.removePrefix("ol:")}\">" to "</li>"
            value.startsWith("todo:") -> "<todo checked=\"${value.removePrefix("todo:")}\">" to "</todo>"
            value == "blockquote" -> "<blockquote>" to "</blockquote>"
            value.startsWith("color:") -> "<color=${value.removePrefix("color:")}>" to "</color>"
            value.startsWith("bg:") -> "<bg=${value.removePrefix("bg:")}>" to "</bg>"
            value.startsWith("size:") -> "<size=${value.removePrefix("size:")}>" to "</size>"
            value.startsWith("a:") -> "<a href=\"${value.removePrefix("a:")}\">" to "</a>"
            value.startsWith("align:") -> "<align=${value.removePrefix("align:")}>" to "</align>"
            value.matches(Regex("h[123]")) -> "<$value>" to "</$value>"
            else -> null
        }
    }

    /** 判断某个 Annotation 是否属于段落类语义。 */
    private fun isBlockAnnotation(value: String): Boolean {
        return value == "ul" ||
                value.startsWith("ol:") ||
                value.startsWith("todo:") ||
                value == "blockquote" ||
                value.startsWith("align:") ||
                value.matches(Regex("h[123]"))
    }

    /** 将 Android 对齐枚举转成导出 token。 */
    private fun alignmentToToken(alignment: Layout.Alignment): String {
        return when (alignment) {
            Layout.Alignment.ALIGN_CENTER -> "center"
            Layout.Alignment.ALIGN_OPPOSITE -> "right"
            else -> "left"
        }
    }

    /** 将导出 token 还原为 Android 对齐枚举。 */
    private fun tokenToAlignment(token: String): Layout.Alignment {
        return when (token) {
            "center" -> Layout.Alignment.ALIGN_CENTER
            "right" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_NORMAL
        }
    }

    /**
     * 从一个已有 Span 中裁掉指定区间。
     *
     * 如果只删除中间一段，会把左右两侧重新建成新的 Span。
     */
    private fun <T> splitSpan(
        editable: Editable,
        span: T,
        start: Int,
        end: Int,
        recreate: () -> T
    ) {
        val spanStart = editable.getSpanStart(span)
        val spanEnd = editable.getSpanEnd(span)
        val flags = editable.getSpanFlags(span)
        editable.removeSpan(span)
        val isParagraphSpan = flags == Spannable.SPAN_PARAGRAPH
        if (spanStart < start) {
            val leftEnd = min(start, spanEnd)
            if (!isParagraphSpan || isValidParagraphSpanRange(editable, spanStart, leftEnd)) {
                editable.setSpan(recreate(), spanStart, leftEnd, flags)
            }
        }
        if (spanEnd > end) {
            val rightStart = max(end, spanStart)
            if (!isParagraphSpan || isValidParagraphSpanRange(editable, rightStart, spanEnd)) {
                editable.setSpan(recreate(), rightStart, spanEnd, flags)
            }
        }
    }

    /** 检查一段范围是否满足段落 Span 的边界要求。 */
    private fun isValidParagraphSpanRange(text: CharSequence, start: Int, end: Int): Boolean {
        if (start >= end) return false
        val validStart = start == 0 || (start <= text.length && text[start - 1] == '\n')
        val validEnd = end == text.length || (end > 0 && text[end - 1] == '\n')
        return validStart && validEnd
    }

    /** 按段落切分整段文本，返回每一行对应的起止范围。 */
    private fun lineRanges(text: CharSequence): List<Pair<Int, Int>> {
        if (text.isEmpty()) return emptyList()
        val ranges = mutableListOf<Pair<Int, Int>>()
        var lineStart = 0
        while (lineStart < text.length) {
            val lineEnd =
                text.indexOf('\n', lineStart).let { if (it == -1) text.length else it + 1 }
            ranges.add(lineStart to lineEnd)
            lineStart = lineEnd
        }
        return ranges
    }

    /** 找出某一行里最后生效的指定 Annotation。 */
    private fun findLatestAnnotation(
        editable: Editable,
        lineStart: Int,
        lineEnd: Int,
        predicate: (String) -> Boolean
    ): Annotation? {
        return editable.getSpans(lineStart, lineEnd, Annotation::class.java)
            .filter { annotation ->
                annotation.key == ANNOTATION_KEY &&
                        predicate(annotation.value) &&
                        editable.getSpanStart(annotation) < lineEnd &&
                        editable.getSpanEnd(annotation) > lineStart
            }
            .maxWithOrNull(
                compareBy<Annotation> { editable.getSpanStart(it) }
                    .thenBy { editable.getSpanEnd(it) }
            )
    }

    /**
     * 将任意范围扩展到合法的整段边界。
     *
     * 段落 Span 必须从段首开始，到换行或文本结尾结束。
     */
    private fun normalizeParagraphRange(text: CharSequence, start: Int, end: Int): Pair<Int, Int> {
        var safeStart = start.coerceIn(0, text.length)
        var safeEnd = end.coerceIn(safeStart, text.length)

        if (safeStart > 0 && text[safeStart - 1] != '\n') {
            val previousBreak = text.lastIndexOf('\n', safeStart - 1)
            safeStart = if (previousBreak == -1) 0 else previousBreak + 1
        }

        if (safeEnd < text.length && (safeEnd == 0 || text[safeEnd - 1] != '\n')) {
            val nextBreak = text.indexOf('\n', safeEnd)
            safeEnd = if (nextBreak == -1) text.length else nextBreak + 1
        }

        return safeStart to safeEnd
    }

    /**
     * 将行内范围按换行拆分，避免背景色把换行符一起染色。
     */
    private fun splitInlineRangeByNewline(
        text: CharSequence,
        start: Int,
        end: Int
    ): List<Pair<Int, Int>> {
        if (start >= end) return emptyList()

        val ranges = mutableListOf<Pair<Int, Int>>()
        var segmentStart = start
        for (index in start until end) {
            if (text[index] == '\n') {
                if (segmentStart < index) {
                    ranges += segmentStart to index
                }
                segmentStart = index + 1
            }
        }
        if (segmentStart < end) {
            ranges += segmentStart to end
        }
        return ranges
    }

    /** 从指定位置开始向后查找字符。 */
    private fun CharSequence.indexOf(char: Char, startIndex: Int): Int {
        for (index in startIndex.coerceAtLeast(0) until length) {
            if (this[index] == char) return index
        }
        return -1
    }

    /** 从指定位置开始向前查找字符。 */
    private fun CharSequence.lastIndexOf(char: Char, startIndex: Int): Int {
        for (index in startIndex.coerceAtMost(length - 1) downTo 0) {
            if (this[index] == char) return index
        }
        return -1
    }
}
