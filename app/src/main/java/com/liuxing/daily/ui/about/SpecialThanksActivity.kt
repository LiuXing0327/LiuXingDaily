/*
 * Copyright 2025-2026 流星
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

package com.liuxing.daily.ui.about

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.liuxing.daily.R
import com.liuxing.daily.data.SpecialThanksData
import com.liuxing.daily.ui.compose.theme.DailyTheme
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.qrx.QRXActivity

class SpecialThanksActivity : QRXActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            initCompose()
            
            LaunchedEffect(Unit) {
                checkStatusBarColorForCompose(
                    topAppBarColor = androidx.compose.ui.graphics.Color.Transparent
                )
            }

            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                val wallpaperBitmap = remember { safeWallpaperBitmap }
                val wallpaperAlpha = remember { safeWallpaperAlpha }
                val cardAlpha = remember { safeCardAlpha }

                SpecialThanksScreen(
                    title = getString(R.string.special_thanks),
                    wallpaperBitmap = wallpaperBitmap,
                    wallpaperAlpha = wallpaperAlpha,
                    cardAlpha = cardAlpha,
                    dataList = getSpecialThanksList(),
                    onBack = ::finish
                )
            }
        }
    }

    private fun getSpecialThanksList(): List<SpecialThanksData> {
        return listOf(
            SpecialThanksData(
                "zoyonsheng",
                "对醒悟推广的支持与帮助",
                "",
                0
            ),
            SpecialThanksData(
                "XuRuo",
                "对醒悟推广的支持与帮助",
                "",
                0
            ),
            SpecialThanksData(
                "南城双念",
                "对醒悟推广的支持与帮助",
                "",
                0
            )
        )
    }
}