package com.liuxing.daily.util

import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import androidx.core.content.ContextCompat
import com.liuxing.daily.R


/**
 * Author：流星
 * DateTime：2024/10/17 下午2:26
 * Description：窗户工具类
 */
object WindowUtil {

    /**
     * 跟随主题模式设置颜色
     *
     *                  浅色模式：-1
     *                  深色模式：-16777216
     *
     * @param window Window
     * @param context 上下文
     */
    fun followPatternSetColor(window: Window, context: Context) {
        val color = ContextCompat.getColor(context, R.color.mode)
        if (color == -1) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility = 0
        }
    }
}