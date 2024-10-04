package com.liuxing.daily.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackbarUtil {

    /**
     * 显示 Snackbar 短时间
     *
     * @param view    显示 Snackbar 的 View
     * @param message 要显示的文本
     */
    fun showSnackbarShort(view: View?, message: String?) {
        Snackbar.make(view!!, message!!, Snackbar.LENGTH_SHORT).show()
    }
}