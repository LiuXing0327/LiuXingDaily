/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.liuxing.daily.listener.UndoRedoListener

/**
 *  *  *  *//**
 * 画板
 */

class DrawView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var path = Path()

    private var drawPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private var eraserPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = drawPaint.strokeWidth * 2
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val indicatorPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var isEraserOn = false
    val isEraserOnP
        get() = isEraserOn

    private val paint: Paint
        get() = if (isEraserOn) eraserPaint else drawPaint

    private val paths = mutableListOf<Pair<Path, Paint>>()
    private val redoPaths = mutableListOf<Pair<Path, Paint>>()

    private var currentX = -1f
    private var currentY = -1f

    private var backgroundColor = Color.TRANSPARENT

    var undoRedoListener: UndoRedoListener? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)

        for ((path, paint) in paths) {
            canvas.drawPath(path, paint)
        }

        canvas.drawPath(path, paint)

        // 绘制指示圆点
        if (currentX >= 0 && currentY >= 0) {
            canvas.drawCircle(currentX, currentY, paint.strokeWidth / 2, indicatorPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        val x = event.x
        val y = event.y

        currentX = x
        currentY = y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path = Path().apply {
                    moveTo(x, y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
            }

            MotionEvent.ACTION_UP -> {
                val newPath = Path(path)
                val newPaint = Paint(paint)
                paths.add(Pair(newPath, newPaint))
                path.reset()
                redoPaths.clear()
                notifyUndoRedoChanged()

                // 清除圆点位置
                currentX = -1f
                currentY = -1f
            }
        }

        invalidate()
        return true
    }

    /**
     * 撤销
     */
    fun undo() {
        if (paths.isNotEmpty()) {
            val last = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                paths.removeLast()
            } else {
                paths.removeAt(paths.size - 1)
            }
            redoPaths.add(last)
            invalidate()
            notifyUndoRedoChanged()
        }
    }

    /**
     * 重做
     */
    fun redo() {
        if (redoPaths.isNotEmpty()) {
            val last = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                redoPaths.removeLast()
            } else {
                redoPaths.removeAt(redoPaths.size - 1)
            }
            paths.add(last)
            invalidate()
            notifyUndoRedoChanged()
        }
    }

    // 是否可以撤销和重做
    fun canUndo() = paths.isNotEmpty()
    fun canRedo() = redoPaths.isNotEmpty()

    /**
     * 通知撤消重做已更改
     */
    private fun notifyUndoRedoChanged() {
        undoRedoListener?.onUndoRedoChanged(canUndo(), canRedo())
    }

    /**
     * 设置画笔颜色
     *
     * @param color 画笔颜色
     */
    fun setStrokeColor(color: Int) {
        drawPaint.color = color
    }

    /**
     * 设置橡皮擦颜色
     *
     * @param color 橡皮擦颜色
     */
    fun setEraserStrokeColor(color: Int) {
        eraserPaint.color = color
    }

    /**
     * 设置线宽
     *
     * @param width 宽度
     */
    fun setStrokeWidth(width: Float) {
        drawPaint.strokeWidth = width
        eraserPaint.strokeWidth = width * 2
    }

    /**
     * 开启或关闭橡皮擦模式
     *
     * @param enable 是否启用
     */
    fun setEraserMode(enable: Boolean) {
        isEraserOn = enable
    }

    /**
     * 设置背景颜色
     *
     * @param color 背景颜色
     */
    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        super.setBackgroundColor(color)
    }
}