/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import com.liuxing.daily.markdown.color.MarkdownColor

/** 引用快的 Span. */
class QuoteBlockSpan : LeadingMarginSpan {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MarkdownColor.bulletColor
        style = Paint.Style.FILL
    }

    override fun getLeadingMargin(first: Boolean): Int = 56

    override fun drawLeadingMargin(
        c: Canvas?,
        p: Paint?,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence?,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        if (c == null || text == null || layout == null) return
        val barLeft = x + dir * 18f
        val barRight = barLeft + dir * 6f
        val currentLine = layout.getLineForOffset(start)
        val spanStart = (text as? Spanned)
            ?.getSpans(start, start, QuoteBlockSpan::class.java)
            ?.firstOrNull()
            ?.let { spanned ->
                (text as Spanned).getSpanStart(spanned)
            }
            ?: start
        val spanEndExclusive = (text as? Spanned)
            ?.getSpans(start, start, QuoteBlockSpan::class.java)
            ?.firstOrNull()
            ?.let { spanned ->
                (text as Spanned).getSpanEnd(spanned)
            }
            ?: end
        val firstLine = layout.getLineForOffset(spanStart.coerceAtLeast(0))
        val lastLine = layout.getLineForOffset((spanEndExclusive - 1).coerceAtLeast(spanStart))
        val isFirstLine = currentLine == firstLine
        val isLastLine = currentLine == lastLine
        val drawTop = if (isFirstLine) top.toFloat() else top.toFloat() - 2f
        val drawBottom = if (isLastLine) bottom.toFloat() else bottom.toFloat() + 2f
        val radius = if (isFirstLine || isLastLine) 4f else 0f
        c.drawRoundRect(
            minOf(barLeft, barRight),
            drawTop,
            maxOf(barLeft, barRight),
            drawBottom,
            radius,
            radius,
            paint
        )
    }
}
