/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.material.appbar.AppBarLayout
import com.liuxing.daily.view.ExtendedFloatingActionButton

/**
 * AppBarLayout 平移动画显示或隐藏函数
 *
 * @param show 是否显示 AppBarLayout
 */
fun AppBarLayout.slide(show: Boolean) {
    val targetY = if (show) 0f else -this.height.toFloat()
    animate().translationY(targetY).setDuration(300).start()
}

/**
 * View 旋转一次动画
 *
 * @param duration 动画时长（默认 1000ms）
 * @param endAction 动画结束时执行的回调
 */
fun View.rotateOnce(duration: Long = 1000L, endAction: (() -> Unit)? = null) {
    this.rotation = 0f
    this.animate()
        .rotation(360f)
        .withLayer()
        .setDuration(duration)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .withEndAction { endAction?.invoke() }
        .start()
}


fun ExtendedFloatingActionButton.rotateOnce(
    duration: Long = 1000L,
    endAction: (() -> Unit)? = null
) {
    (this as View).rotateOnce(duration, endAction)
}

/**
 * 根据布尔值设置视图可视性
 *
 * @param show true：可见，
 *             false：隐藏。
 */
fun View.setVisibility(show: Boolean) {
    this.visibility = if(show) View.VISIBLE else View.GONE
}