package com.liuxing.daily.util

import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode

/**
 * Author：流星
 * DateTime：2024/10/25 14:27
 * Description：主题模式工具类
 */
object ThemeModeUtil {

    fun setThemeMode(themeMode: Int) {
        when (themeMode) {

            1 -> setDefaultNightMode(MODE_NIGHT_NO)

            2 -> setDefaultNightMode(MODE_NIGHT_YES)

            else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}