/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.liuxing.daily.extension.centerYFromBaseLine
import com.liuxing.daily.markdown.color.MarkdownColor

class TodoSpan(val checked: Boolean) : LeadingMarginSpan {

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MarkdownColor.bulletColor
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MarkdownColor.bulletColor
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    override fun getLeadingMargin(first: Boolean): Int = 108

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
        if (!first || c == null || p == null) return

        val size = 34f
        val left = x + dir * 22f
        val cy = p.centerYFromBaseLine(baseline)
        val topEdge = cy - size / 2
        val right = left + dir * size
        val bottomEdge = cy + size / 2

        val rectLeft = minOf(left, right)
        val rectRight = maxOf(left, right)
        c.drawRoundRect(rectLeft, topEdge, rectRight, bottomEdge, 7f, 7f, boxPaint)

        if (checked) {
            val startX = rectLeft + size * 0.22f
            val startY = cy + size * 0.02f
            val midX = rectLeft + size * 0.45f
            val midY = cy + size * 0.26f
            val endX = rectLeft + size * 0.8f
            val endY = cy - size * 0.22f
            c.drawLine(startX, startY, midX, midY, checkPaint)
            c.drawLine(midX, midY, endX, endY, checkPaint)
        }
    }
}
