/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.liuxing.daily.R
import kotlin.math.min

class RoundTricolorView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val topPaint = Paint()
    private val leftPaint = Paint()
    private val rightPaint = Paint()

    init {
        // 初始化颜色
        topPaint.color = ContextCompat.getColor(context!!, R.color.md_theme_primaryFixed)
        topPaint.style = Paint.Style.FILL

        leftPaint.color = ContextCompat.getColor(context, R.color.md_theme_primary)
        leftPaint.style = Paint.Style.FILL

        rightPaint.color = ContextCompat.getColor(context, R.color.md_theme_inversePrimary)
        rightPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height
        val halfHeight = height / 2f
        val radius = (min(width.toDouble(), height.toDouble()) / 2f).toFloat()

        // 上半部分（整块颜色）
        canvas.save()
        canvas.clipRect(0f, 0f, width.toFloat(), halfHeight)
        canvas.drawCircle(width / 2f, height / 2f, radius, topPaint)
        canvas.restore()

        // 下半部分
        canvas.save()
        canvas.clipRect(0f, halfHeight, width.toFloat(), height.toFloat())

        // 左下部分
        canvas.save()
        canvas.clipRect(0f, halfHeight, width / 2f, height.toFloat())
        canvas.drawCircle(width / 2f, height / 2f, radius, leftPaint)
        canvas.restore()

        // 右下部分
        canvas.save()
        canvas.clipRect(width / 2f, halfHeight, width.toFloat(), height.toFloat())
        canvas.drawCircle(width / 2f, height / 2f, radius, rightPaint)
        canvas.restore()

        canvas.restore()
    }

    /**
     * 设置颜色
     *
     * @param topColor 上半部分的颜色
     * @param leftColor 左半部分的颜色
     * @param rightColor 右半部分的颜色
     */
    fun setColors(topColor: Int, leftColor: Int, rightColor: Int) {
        topPaint.color = topColor
        leftPaint.color = leftColor
        rightPaint.color = rightColor
        invalidate()
    }
}

