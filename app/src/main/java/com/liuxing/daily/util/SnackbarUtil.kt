package com.liuxing.daily.util

import android.view.View
import com.google.android.material.floatingtoolbar.FloatingToolbarLayout
import com.google.android.material.snackbar.Snackbar

/**
 * Snackbar 工具类
 */
object SnackbarUtil {

    /**
     * 显示 Snackbar 短时间
     *
     * @param view    显示 Snackbar 的 View
     * @param message 要显示的文本
     */
    fun showSnackbarShort(view: View?, message: String?) {
        val snackbar = Snackbar.make(view!!, message!!, Snackbar.LENGTH_SHORT)

        // 如果传入的 View 是 FloatingToolbarLayout，则设置锚点。
        if (view is FloatingToolbarLayout) {
            snackbar.setAnchorView(view)
        }

        snackbar.show()
    }
}