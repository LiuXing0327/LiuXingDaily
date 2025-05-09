package com.liuxing.daily.markdown

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.liuxing.daily.markdown.span.HeadingSpan

/**
 * Author：流星
 * DateTime：2025/4/6 12:53
 * Description：Markdown 解析
 */
object MarkdownParser {

    /**
     * 解析 Markdown
     *
     * @param text 要解析的 Markdown 文本
     * @return 解析后的结果
     */
    fun parseMarkdown(text: String): SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        // 按行拆分文本
        val lines = text.split("\n")

        lines.forEach { line ->
            when (line.checkLineType()) {
                LineType.HEADING -> {
                    parseHeading(line, builder)
                }

                LineType.LIST_ITEM -> {
                    parseListItem(line, builder)
                }

                LineType.NORMAL -> {
                    builder.append(line).append("\n")
                }
            }
        }

        return builder
    }

    /**
     * 解析 Markdown 标题
     *
     * @param line 标题行
     * @param builder 追加解析结果
     */
    private fun parseHeading(line: String, builder: SpannableStringBuilder) {
        val content = line.replaceFirst(Regex("^#\\s+"), "")
        val start = builder.length
        builder.append(content).append("\n")
        builder.setSpan(
            HeadingSpan(1.5f, 1f),
            start,
            builder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    /**
     * 解析 Markdown 列表项
     *
     * @param line 列表项行
     * @param builder 追加解析结果
     */
    private fun parseListItem(line: String, builder: SpannableStringBuilder) {
        val content = line.replaceFirst(Regex("^\\s*-\\s+"), "")
        val start = builder.length
        builder.append(content).append("\n")
        builder.setSpan(
            com.liuxing.daily.markdown.span.BulletSpan(),
            start,
            builder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}

/**
 * Markdown 格式文本行的类型
 *
 * - HEADING 标题
 * - LIST_ITEM 列表项
 * - NORMAL 普通文本
 */
private enum class LineType {
    HEADING, LIST_ITEM, NORMAL
}

/**
 * 检查 Markdown 格式文本行的类型
 *
 * @return LineType 文本行类型
 */
private fun String.checkLineType(): LineType {
    return when {
        this.matches(Regex("^#\\s+.*")) -> LineType.HEADING

        this.matches(Regex("^\\s*-\\s+.*")) -> LineType.LIST_ITEM

        else -> LineType.NORMAL
    }
}