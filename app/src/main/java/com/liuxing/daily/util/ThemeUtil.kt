/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import com.liuxing.daily.R

object ThemeUtil {

    /**
     * 设置主题模式
     *
     * @param themeMode 主题模式
     */
    fun setThemeMode(themeMode: Int) {
        when (themeMode) {

            1 -> setDefaultNightMode(MODE_NIGHT_NO)

            2 -> setDefaultNightMode(MODE_NIGHT_YES)

            else -> setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * 应用主题
     *
     * @param context 上下文
     */
    fun applyTheme(context: Context) {
        val themeColorId = SharedPreferencesUtil.getInt(context, "theme_color_id", 0)
        when (themeColorId) {
            1 -> setThemeToRed(context)

            2 -> setThemeToGreen(context)

            3 -> setThemeToBlue(context)

            4 -> setThemeToYellow(context)

            5 -> setThemeToPink(context)

            else -> setThemeToPurple(context)
        }
    }

    /**
     * 设置主题
     *
     * @param context 上下文
     */

    fun setThemeToPurple(context: Context) {
        context.setTheme(R.style.Base_Theme_流星日记)
    }

    fun setThemeToRed(context: Context) {
        context.setTheme(R.style.RedTheme)
    }

    fun setThemeToGreen(context: Context) {
        context.setTheme(R.style.GreenTheme)
    }

    fun setThemeToBlue(context: Context) {
        context.setTheme(R.style.BlueTheme)
    }

    fun setThemeToYellow(context: Context) {
        context.setTheme(R.style.YellowTheme)
    }

    fun setThemeToPink(context: Context) {
        context.setTheme(R.style.PinkTheme)
    }
}