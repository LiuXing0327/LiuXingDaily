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

class OpenSourceActivity : QRXActivity() {

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
                    title = getString(R.string.open_source_libraries),
                    wallpaperBitmap = wallpaperBitmap,
                    wallpaperAlpha = wallpaperAlpha,
                    cardAlpha = cardAlpha,
                    dataList = getOpenSourceList(),
                    onBack = ::finish
                )
            }
        }
    }

    private fun getOpenSourceList(): List<SpecialThanksData> {
        return listOf(
            SpecialThanksData(
                "Gson", "\nCopyright 2008 Google Inc.\n" +
                        "\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n" +
                        "You may obtain a copy of the License at\n" +
                        "\n" +
                        "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.\n",
                "https://github.com/google/gson",
                1
            ),
            SpecialThanksData(
                "OkHttp", "\nCopyright 2019 Square, Inc.\n" +
                        "\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n" +
                        "You may obtain a copy of the License at\n" +
                        "\n" +
                        "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.\n",
                "https://github.com/square/okhttp",
                1
            ),
            SpecialThanksData(
                "Glide",
                "\nBSD, part MIT and Apache 2.0.\n",
                "https://github.com/bumptech/glide",
                1
            ),
            SpecialThanksData(
                "PhotoView", "\nCopyright 2018 Chris Banes\n" +
                        "\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n" +
                        "You may obtain a copy of the License at\n" +
                        "\n" +
                        "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.\n",
                "https://github.com/Baseflow/PhotoView",
                1
            ),
            SpecialThanksData(
                "subsampling-scale-image-view",
                "\nCopyright 2018 David Morrissey, and licensed under the Apache License, " +
                        "Version 2.0. No attribution is necessary but it's very much appreciated. Star this project if you like it!\n",
                "https://github.com/davemorrissey/subsampling-scale-image-view",
                1
            ),

            SpecialThanksData(
                "sardine.android",
                "\nApache 2.0 License.\n",
                "https://github.com/thegrizzlylabs/sardine-android",
                1
            ),

            SpecialThanksData(
                "zip4j",
                "\nApache 2.0 License.\n",
                "https://github.com/srikanth-lingala/zip4j",
                1
            ),

            SpecialThanksData(
                "ColorPickerView",
                "\nCopyright [2025] [LiuXing]\n" +
                        "\n" +
                        "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "   you may not use this file except in compliance with the License.\n" +
                        "   You may obtain a copy of the License at\n" +
                        "\n" +
                        "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "   Unless required by applicable law or agreed to in writing, software\n" +
                        "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "   See the License for the specific language governing permissions and\n" +
                        "   limitations under the License.\n",
                "https://github.com/LiuXing0327/ColorPickerView",
                1
            )
        )
    }
}