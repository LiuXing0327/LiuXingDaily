/*
 * Copyright 2024-2026 流星
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liuxing.daily.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import com.google.android.material.color.DynamicColors
import com.liuxing.daily.R
import com.liuxing.daily.ui.appearance.AppearanceConst
import com.liuxing.daily.ui.compose.theme.DailyThemeType

object ThemeUtil {

    private const val THEME_COLOR_ID_KEY = "theme_color_id"

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
        val dynamicColor = isDynamicColorEnabled(context)
        if (dynamicColor) {
            DynamicColors.applyToActivityIfAvailable(context as Activity)
            return
        }

        val themeColorId = getThemeColorId(context)
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

    fun isDynamicColorEnabled(context: Context): Boolean {
        return SharedPreferencesUtil.getBoolean(
            context,
            AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY,
            false
        )
    }

    fun getThemeColorId(context: Context): Int {
        return SharedPreferencesUtil.getInt(context, THEME_COLOR_ID_KEY, ThemeColor.PURPLE.id)
    }

    fun getThemeColor(context: Context): ThemeColor {
        return ThemeColor.fromId(getThemeColorId(context))
    }

    fun getComposeThemeType(context: Context): DailyThemeType {
        return getThemeColor(context).composeThemeType
    }

    fun applyAmoledTheme(context: Context,isAmoled: Boolean){
        val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // if(isDark && isAmoled) context.setTheme(R.style.AmoledTheme)
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
    LIGHT_CYAN(6, R.string.color_label_light_cyan);

    val composeThemeType: DailyThemeType
        get() = when (this) {
            PURPLE -> DailyThemeType.DEFAULT
            RED -> DailyThemeType.RED
            GREEN -> DailyThemeType.GREEN
            BLUE -> DailyThemeType.BLUE
            YELLOW -> DailyThemeType.YELLOW
            PINK -> DailyThemeType.PINK
            LIGHT_CYAN -> DailyThemeType.CYAN
        }

    companion object {
        fun fromId(id: Int): ThemeColor {
            return entries.firstOrNull { it.id == id } ?: PURPLE
        }
    }
}