/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import com.google.android.material.color.DynamicColors
import com.liuxing.daily.R
import com.liuxing.daily.ui.appearance.AppearanceConst

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
        val dynamicColor = SharedPreferencesUtil.getBoolean(
            context,
            AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY,
            false
        )
        if (dynamicColor) {
            DynamicColors.applyToActivityIfAvailable(context as Activity)
            return
        }

        val themeColorId = SharedPreferencesUtil.getInt(context, "theme_color_id", 0)
        when (themeColorId) {
            ThemeColor.RED.id -> setThemeToRed(context)

            ThemeColor.LIGHT_CYAN.id  -> setThemeToLightCyan(context)

            ThemeColor.GREEN.id -> setThemeToGreen(context)

            ThemeColor.BLUE.id -> setThemeToBlue(context)

            ThemeColor.YELLOW.id  -> setThemeToYellow(context)

            ThemeColor.PINK.id  -> setThemeToPink(context)

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

    fun setThemeToLightCyan(context: Context) {
        context.setTheme(R.style.lightCyanTheme)
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

/**
 * 主题颜色信息
 *
 * - PURPLE 紫色（默认）
 * - RED 红色
 * - GREEN 绿色
 * - BLUE 蓝色
 * - YELLOW 黄色
 * - PINK 粉色
 * - LIGHT_CYAN 浅青色
 *
 * @param id 颜色 id
 * @param themeLabel 主题标签
 */
enum class ThemeColor(val id: Int, val themeLabel: Int) {
    PURPLE(0, R.string.color_label_purple),
    RED(1,R.string.color_label_red),
    GREEN(2, R.string.color_label_green),
    BLUE(3, R.string.color_label_blue),
    YELLOW(4, R.string.color_label_yellow),
    PINK(5, R.string.color_label_pink),
    LIGHT_CYAN(6, R.string.color_label_light_cyan)
}