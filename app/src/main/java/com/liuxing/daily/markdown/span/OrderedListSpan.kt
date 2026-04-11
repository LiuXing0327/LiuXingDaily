/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.liuxing.daily.markdown.color.MarkdownColor

/** 有序列表的 Span. */
class OrderedListSpan(val index: Int) : LeadingMarginSpan {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MarkdownColor.bulletColor
        textSize = 34f
        textAlign = Paint.Align.RIGHT
    }

    override fun getLeadingMargin(first: Boolean): Int = 96

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
        if (!first || c == null) return
        val label = "$index."
        val drawX = x + dir * 66f
        c.drawText(label, drawX, baseline.toFloat(), paint)
    }
}
