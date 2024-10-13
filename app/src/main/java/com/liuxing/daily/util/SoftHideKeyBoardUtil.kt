package com.liuxing.daily.util

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout


/**
 * Author：流星
 * DateTime：2024/10/04 10:54:02
 * Description：处理键盘遮挡输入框
 */
class SoftHideKeyBoardUtil(activity: Activity) {
    private val childOfContent: View =
        (activity.findViewById<FrameLayout>(android.R.id.content)!!).getChildAt(0)
    private var usableHeightPrevious = 0
    private val frameLayoutParams: FrameLayout.LayoutParams =
        childOfContent.layoutParams as FrameLayout.LayoutParams
    private var contentHeight = 0
    private var isfirst = true
    private val statusBarHeight = 0

    init {
        childOfContent.viewTreeObserver.addOnGlobalLayoutListener {
            if (isfirst) {
                contentHeight = childOfContent.height
                isfirst = false
            }
            resizeChildOfContent()
        }

    }

    /**
     * 计算当前可用高度
     */
    private fun resizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        // 如果当前可用高度和原高度不同
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard =
                childOfContent.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            // 高度差大于屏幕1/4时，说明键盘弹出
            frameLayoutParams.height = if (heightDifference > usableHeightSansKeyboard / 4) {
                usableHeightSansKeyboard - heightDifference +
                        statusBarHeight
            } else {
                contentHeight
            }
            childOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    /**
     * 计算可用高度
     *
     * @return 可用高度
     */
    private fun computeUsableHeight(): Int {
        val rect = Rect()
        childOfContent.getWindowVisibleDisplayFrame(rect)
        return (rect.bottom - rect.top) + 200
    }
}