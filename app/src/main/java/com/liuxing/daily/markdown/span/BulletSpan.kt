package com.liuxing.daily.markdown.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.core.content.ContextCompat
import com.liuxing.daily.MyApplication
import com.liuxing.daily.R

/**
 * Author：流星
 * DateTime：2025/5/1 10:25
 * Description：BulletSpan
 */
class BulletSpan : LeadingMarginSpan {

    private val paint = Paint()

    init {
        paint.color = ContextCompat.getColor(MyApplication.context!!, R.color.bullet_color)
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