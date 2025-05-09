package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan

/**
 * Author：流星
 * DateTime：2025/5/1 8:44
 * Description：HeadingSpan
 */
class HeadingSpan(private val proportion: Float, private val lineHeight: Float) :
    MetricAffectingSpan(),
    LeadingMarginSpan {

    override fun updateDrawState(tp: TextPaint?) {
        tp?.let { applyStyle(it) }
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        applyStyle(textPaint)
    }

    /**
     * 应用样式
     *
     * @param textPaint 绘制的textPaint对象
     */
    private fun applyStyle(textPaint: TextPaint) {
        textPaint.textSize = (textPaint.textSize * proportion)
        textPaint.typeface = Typeface.create(textPaint.typeface, Typeface.BOLD)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return 0
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

        val paint = Paint(p)
        paint.style = Paint.Style.FILL
        paint.color = Color.GRAY
        paint.strokeWidth = lineHeight

        val lineTop = bottom - lineHeight / 2

        c?.drawLine(
            x.toFloat(), lineTop, (c.width).toFloat(), lineTop, paint
        )
    }
}