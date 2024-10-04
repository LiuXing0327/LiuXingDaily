package com.liuxing.daily.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * Author：流星
 * DateTime：2024/10/4 下午9:00
 * Description：根据垂直滚动来收缩或扩展按钮。
 */
open class CustomExtendedFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : ExtendedFloatingActionButton(context, attributeSet) {

    private val customFabBehavior = CustomExtendedFABBehavior(context, attributeSet)

    /**
     * 覆盖默认行为
     */
    override fun getBehavior(): CoordinatorLayout.Behavior<ExtendedFloatingActionButton> {
        return customFabBehavior
    }

    /**
     * 定义按钮响应滚动事件行为
     *
     * @param context 上下文
     * @param attributeSet XML属性
     */
    protected class CustomExtendedFABBehavior(
        context: Context,
        attributeSet: AttributeSet?
    ) : ExtendedFloatingActionButtonBehavior<ExtendedFloatingActionButton>(context, attributeSet) {

        /**
         * 监听垂直滚动事件
         *
         * @param axes 监听滚动轴
         * @return 如果滚动轴是垂直的，返回 true，否则返回 false.
         */
        override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: ExtendedFloatingActionButton,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int
        ): Boolean = axes == ViewCompat.SCROLL_AXIS_VERTICAL

        /**
         * 处理滚动事件
         */
        override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: ExtendedFloatingActionButton,
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int,
            consumed: IntArray
        ) {
            // 判断是否是列表类型视图
            if (target is RecyclerView) {
                // 检查是否到达底部
                val isAtBottom = !target.canScrollVertically(1)
                // 到达扩展，否则收缩
                if (!isAtBottom) handleFABState(dyConsumed, child)
            } else {
                // 如果不是列表，直接根据滚动方向处理
                handleFABState(dyConsumed, child)
            }
        }

        /**
         * 根据滚动方向处理FAB按钮的缩放
         *
         * @param dyConsumed 滚动量
         * @param child 需要处理的FAB按钮
         */
        private fun handleFABState(dyConsumed: Int, child: ExtendedFloatingActionButton) =
            if (dyConsumed > 0) shrinkButton(child) else extendButton(child)

        /**
         * 扩展按钮
         */
        private fun extendButton(child: ExtendedFloatingActionButton) = child.extend()

        /**
         * 收缩按钮
         */
        private fun shrinkButton(child: ExtendedFloatingActionButton) = child.shrink()
    }
}