/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.liuxing.daily.extension.centerYFromBaseLine

/**
 * HorizontalRuleSpan
 *
 * @param lineHeight 线条高度
 * @param color 线条颜色
 */
class HorizontalRuleSpan(
    private val lineHeight: Float = 4f,
    private val color: Int = Color.GRAY,
) : LeadingMarginSpan {

    override fun getLeadingMargin(first: Boolean): Int = 0

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
        if (c == null || p == null || layout == null) return

        val paint = Paint(p)
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = lineHeight
        paint.isAntiAlias = true

        val left = 0f
        val right = layout.width.toFloat()
        val lineY = p.centerYFromBaseLine(baseline)
        c.drawLine(left, lineY, right, lineY, paint)
    }
}
