/*
 * Copyright 2026 流星
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

package com.liuxing.daily.ui.compose.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.liuxing.daily.MyApplication
import com.liuxing.daily.ui.appearance.AppearanceConst
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil

object DailyThemeManager {
    var currentThemeType by mutableStateOf(DailyThemeType.DEFAULT)
    var isDynamicColor by mutableStateOf(false)
    var themeMode by mutableIntStateOf(3)
    var isAmoled by mutableStateOf(false) // 纯黑模式
    private val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext)

    fun syncFromPreferences(context: Context) {
        isDynamicColor = SharedPreferencesUtil.getBoolean(
            context,
            AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY,
            false
        )
        themeMode = sharedPreferences.getInt("theme_mode_preference", 3)
        isAmoled = sharedPreferences.getBoolean("amoled_mode_key", false)

        val themeColorId = SharedPreferencesUtil.getInt(context, "theme_color_id", 0)
        currentThemeType = when (themeColorId) {
            1 -> DailyThemeType.RED
            2 -> DailyThemeType.GREEN
            3 -> DailyThemeType.BLUE
            4 -> DailyThemeType.YELLOW
            5 -> DailyThemeType.PINK
            6 -> DailyThemeType.CYAN
            else -> DailyThemeType.DEFAULT
        }
    }

    fun updateDynamicColor(context: Context, enabled: Boolean) {
        isDynamicColor = enabled
        SharedPreferencesUtil.putBoolean(context, AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY, enabled)
    }

    fun updateThemeType(context: Context, type: DailyThemeType, id: Int) {
        currentThemeType = type
        SharedPreferencesUtil.putInt(context, "theme_color_id", id)
        ThemeUtil.applyAmoledTheme(context,true)
    }

    fun updateThemeMode(mode: Int) {
        themeMode = mode
        sharedPreferences.edit {
            putInt("theme_mode_preference", mode)
            apply()
        }

        ThemeUtil.setThemeMode(mode)
    }

    fun updateAmoledMode(enabled: Boolean) {
        isAmoled = enabled
        sharedPreferences.edit {
            putBoolean("amoled_mode_key", enabled)
            apply()
        }


    }
}