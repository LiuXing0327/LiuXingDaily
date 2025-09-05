/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.liuxing.daily.markdown.color.MarkdownColor

/**
 * 自定义 BulletSpan
 */
class BulletSpan : LeadingMarginSpan {

    private val paint = Paint()

    init {
        paint.color = MarkdownColor.bulletColor
        paint.style = Paint.Style.FILL
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return 70
    }

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
        // 只在段落第一行绘制
        if (!first) return

        // x偏移40像素
        val cx = x + dir + 40f
        // 垂直居中
        val cy = (top + bottom) / 2f
        // 以cx、cy，绘制圆
        c?.drawCircle(cx, cy, 8f, paint)
    }
}