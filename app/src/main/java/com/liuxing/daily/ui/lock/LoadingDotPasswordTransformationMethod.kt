/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.ui.lock

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.method.PasswordTransformationMethod
import android.text.style.ReplacementSpan
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import com.google.android.material.textfield.TextInputEditText
import com.liuxing.daily.ui.lock.LoadingDotPasswordTransformationMethod.Phase.DELETING
import com.liuxing.daily.ui.lock.LoadingDotPasswordTransformationMethod.Phase.DOT
import com.liuxing.daily.ui.lock.LoadingDotPasswordTransformationMethod.Phase.LOADING
import com.liuxing.daily.ui.lock.LoadingDotPasswordTransformationMethod.Phase.SHRINKING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自定义 [PasswordTransformationMethod]，用于实现
 * 「输入时 Loading → 圆点」以及「删除时圆点缩小消失」的密码显示效果。
 *
 * 该实现是为了尽量复刻原生的锁屏密码圆点。
 *
 * 输入动画流程：
 *      LOADING → SHRINKING → DOT
 *
 * 删除动画流程：
 *      DOT → 缩小 → 移除
 *
 * [refreshText] 配合 [PinFragment] 中的 onPinCodeChanged，
 * 通过 setText 的方式刷新，
 * 并始终保持光标位于末尾。
 *
 * @param loadingDrawable 输入时显示的加载指示器 Drawable
 * @param dotDrawable 输入完成后显示的圆点 Drawable
 * @param delay 每个字符从 loading 进入收缩动画的延迟时间
 * @param scaleDuration 缩放动画的时长
 */
class LoadingDotPasswordTransformationMethod(
    private val loadingDrawable: Drawable,
    private val dotDrawable: Drawable,
    private val delay: Long,
    private val scaleDuration: Long
) : PasswordTransformationMethod() {

    /**
     * 是否已完成 reveal（进入 DOT 阶段）。
     */
    private val revealedMap = mutableMapOf<Int, Boolean>()

    /**
     * 当前缩放比例
     */
    private val scaleMap = mutableMapOf<Int, Float>()

    /**
     * 目标输入框，强制刷新文本，并始终保持光标位于末尾。
     */
    private var targetView: TextInputEditText? = null

    /**
     * 每个字符当前所处的动画阶段。
     */
    private val phaseMap = mutableMapOf<Int, Phase>()

    /**
     * 动画阶段
     *
     * - [LOADING] 初始输入，显示加载指示器 drawable
     * - [SHRINKING] LOADING 缩小阶段
     * - [DOT] 最终圆点阶段
     * - [DELETING] 删除时的缩小消失阶段
     */
    private enum class Phase {
        LOADING, SHRINKING, DOT, DELETING
    }

    /**
     * 实际显示 DOT 数量。
     */
    private var visualLength = 0

    /**
     * 实际文本长度
     */
    private var realLength = 0

    /**
     * 上次文本长度，判断是否发生删除。
     */
    private var lastLength = 0

    override fun getTransformation(source: CharSequence, view: View): CharSequence {
        targetView = view as TextInputEditText

        realLength = source.length
        if (realLength < visualLength) {
            // 删除发生：保持 visualLength 不变
            // 等动画结束再减
        } else {
            visualLength = realLength
        }
        // 同步状态
        syncRevealedState(realLength)

        return SpannableString(" ".repeat(visualLength)).apply {
            for (i in 0 until visualLength) {
                setSpan(
                    DotSpan(i), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    /**
     * 同步字符状态
     */
    private fun syncRevealedState(length: Int) {
        // 发生删除
        if (length < lastLength) {
            for (i in length until lastLength) {
                startDeleteForIndex(i)
            }
        }

        // 新增字符
        for (i in 0 until length) {
            if (!revealedMap.containsKey(i)) {
                revealedMap[i] = false
                scaleMap[i] = 1f
                phaseMap[i] = LOADING
                startRevealForIndex(i)
            }
        }

        lastLength = length
    }

    /**
     * 启动删除动画
     */
    private fun startDeleteForIndex(index: Int) {
        if (phaseMap[index] == DELETING) return

        phaseMap[index] = DELETING

        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = 150L
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener {
            scaleMap[index] = it.animatedValue as Float
            refreshText()
        }

        animator.doOnEnd {
            revealedMap.remove(index)
            scaleMap.remove(index)
            phaseMap.remove(index)

            visualLength-- // 动画结束后减少长度
            refreshText()
        }


        animator.start()
    }

    /**
     * 启动 Reveal
     */
    private fun startRevealForIndex(index: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(delay)
            animateShrink(index)
        }
    }

    /**
     * loading 缩小动画
     */
    private fun animateShrink(index: Int) {
        val animator = ValueAnimator.ofFloat(0.8f, 0.2f)
        animator.duration = scaleDuration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener {
            scaleMap[index] = it.animatedValue as Float
            refreshText()
        }

        animator.doOnEnd {
            revealedMap[index] = true
            phaseMap[index] = DOT
            animateDotPop(index)
        }

        animator.start()
    }

    /**
     * 圆点弹出动画
     */
    private fun animateDotPop(index: Int) {
        val animator = ValueAnimator.ofFloat(0.2f, 1f)
        animator.duration = scaleDuration
        animator.interpolator = OvershootInterpolator(1.6f)
        animator.addUpdateListener {
            scaleMap[index] = it.animatedValue as Float
            refreshText()
        }

        animator.start()
    }

    /**
     * 强制刷新 TextInputEditText。
     */
    private fun refreshText() {
        targetView?.let { inputEditText ->
            val text = inputEditText.text
            inputEditText.text = text
            inputEditText.setSelection(inputEditText.text.toString().length)
        }
    }

    inner class DotSpan(private val index: Int) : ReplacementSpan() {

        /**
         * 独立 Drawable，避免状态污染
         */
        private val dot = dotDrawable.constantState?.newDrawable()?.mutate() ?: dotDrawable.mutate()
        private val loading =
            loadingDrawable.constantState?.newDrawable()?.mutate() ?: loadingDrawable.mutate()

        /**
         * slot 缩放系数（用于控制字符间距）
         */
        private val slotScale = 0.6f

        /**
         * slot 固定宽度，避免动画期间输入框跳动
         */
        private val baseSize = maxOf(dot.intrinsicWidth, loading.intrinsicWidth)
        private val slotWidth = (baseSize * slotScale).toInt()

        /**
         *     /**
         *      * Returns the width of the span. Extending classes can set the height of the span by updating
         *      * attributes of {@link android.graphics.Paint.FontMetricsInt}. If the span covers the whole
         *      * text, and the height is not set,
         *      * {@link #draw(Canvas, CharSequence, int, int, float, int, int, int, Paint)} will not be
         *      * called for the span.
         *      *
         *      * @param paint Paint instance.
         *      * @param text Current text.
         *      * @param start Start character index for span.
         *      * @param end End character index for span.
         *      * @param fm Font metrics, can be null.
         *      * @return Width of the span.
         *      */
         *     public abstract int getSize(@NonNull Paint paint, CharSequence text,
         *                         @IntRange(from = 0) int start, @IntRange(from = 0) int end,
         *                         @Nullable Paint.FontMetricsInt fm);
         */
        override fun getSize(
            p0: Paint, p1: CharSequence?, p2: Int, p3: Int, p4: Paint.FontMetricsInt?
        ): Int {
            return slotWidth
        }

        /**
         *    /**
         *      * Draws the span into the canvas.
         *      *
         *      * @param canvas Canvas into which the span should be rendered.
         *      * @param text Current text.
         *      * @param start Start character index for span.
         *      * @param end End character index for span.
         *      * @param x Edge of the replacement closest to the leading margin.
         *      * @param top Top of the line.
         *      * @param y Baseline.
         *      * @param bottom Bottom of the line.
         *      * @param paint Paint instance.
         *      */
         *     public abstract void draw(@NonNull Canvas canvas, CharSequence text,
         *                               @IntRange(from = 0) int start, @IntRange(from = 0) int end, float x,
         *                               int top, int y, int bottom, @NonNull Paint paint);
         */
        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            val phase = phaseMap[index] ?: LOADING

            // 根据阶段选择 drawable
            val drawable = when (phase) {
                LOADING, SHRINKING -> loading
                DOT, DELETING -> dot
            }

            // 根据阶段选择缩放比例
            val scale = when (phase) {
                DELETING -> scaleMap[index] ?: 0f
                DOT -> scaleMap[index] ?: 1f
                SHRINKING -> scaleMap[index] ?: 0.8f
                LOADING -> 0.8f
            }


            val drawableWidth = (drawable.intrinsicWidth * scale).toInt()
            val drawableHeight = (drawable.intrinsicHeight * scale).toInt()

            val fontMetrics = paint.fontMetrics
            val textCenterY = y + (fontMetrics.ascent + fontMetrics.descent) / 2f
            val drawX = x + (slotWidth - drawableWidth) / 2f - 8
            val drawY = (textCenterY - drawableHeight / 2f).toInt()
            drawable.apply {
                setBounds(
                    drawX.toInt(), drawY, drawX.toInt() + drawableWidth, drawY + drawableHeight
                )

                draw(canvas)
            }
        }
    }
}