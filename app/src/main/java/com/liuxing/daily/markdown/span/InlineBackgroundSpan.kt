/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan
import kotlin.math.max
import kotlin.math.min

/**
 * 仅按文字本身高度绘制背景的高亮 Span。
 *
 * 使用自绘背景，避免系统 BackgroundColorSpan 在设置额外行间距时，
 * 将背景一并扩展到整行高度。
 */
class InlineBackgroundSpan(
    val backgroundColor: Int
) : LineBackgroundSpan {

    override fun drawBackground(
        c: Canvas,
        p: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val drawEnd = if (end > start && text[end - 1] == '\n') end - 1 else end
        if (drawEnd <= start) return

        val lineStart = findLineStart(text, start)
        val leadingWidth = if (start > lineStart) {
            p.measureText(text, lineStart, start)
        } else {
            0f
        }
        val segmentWidth = p.measureText(text, start, drawEnd)
        val drawLeft = left + leadingWidth
        val metrics = p.fontMetrics

        val oldColor = p.color
        val oldStyle = p.style
        p.color = backgroundColor
        p.style = Paint.Style.FILL
        c.drawRect(
            drawLeft,
            baseline + metrics.ascent,
            drawLeft + segmentWidth,
            baseline + metrics.descent,
            p
        )
        p.color = oldColor
        p.style = oldStyle
    }

    private fun findLineStart(text: CharSequence, index: Int): Int {
        var cursor = min(index, text.length)
        while (cursor > 0 && text[cursor - 1] != '\n') {
            cursor--
        }
        return max(cursor, 0)
    }
}
