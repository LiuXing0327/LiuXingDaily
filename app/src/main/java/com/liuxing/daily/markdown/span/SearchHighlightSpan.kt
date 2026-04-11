/*
 * Copyright (c) 2026 流星
 */


package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

/**
 * 搜索关键词高亮。
 *
 * 使用 ReplacementSpan 仅绘制当前命中的字符范围，
 * 避免 LineBackgroundSpan 被按整行回调时放大高亮区域。
 */
class SearchHighlightSpan(
    val backgroundColor: Int
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (fm != null) {
            val metrics = paint.fontMetricsInt
            fm.ascent = metrics.ascent
            fm.descent = metrics.descent
            fm.top = metrics.top
            fm.bottom = metrics.bottom
        }
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(
        c: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        if (start >= end) return

        val width = paint.measureText(text, start, end)
        val metrics = paint.fontMetrics
        val oldColor = paint.color
        val oldStyle = paint.style

        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        c.drawRect(
            x,
            y + metrics.ascent,
            x + width,
            y + metrics.descent,
            paint
        )

        paint.color = oldColor
        paint.style = oldStyle
        c.drawText(text, start, end, x, y.toFloat(), paint)
    }
}
