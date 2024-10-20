package com.liuxing.daily.util

import android.view.View
import android.view.Window

/**
 * Author：流星
 * DateTime：2024/10/17 下午2:26
 * Description：窗户工具类
 */
object WindowUtil {

    /**
     * 浅色模式：-1120012
     * 深色墨色：-13685706
     */
    fun FollowPatternSetColor(window: Window, ColorValue: Int) =
        if (ColorValue == -1120012) {
            window.decorView
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        } else {
            window.decorView.setSystemUiVisibility(0)
        }
}