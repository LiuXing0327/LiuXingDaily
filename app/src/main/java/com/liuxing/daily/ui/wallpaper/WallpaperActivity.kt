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

package com.liuxing.daily.ui.wallpaper

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.liuxing.daily.databinding.ActivityWallpaperBinding
import com.liuxing.daily.ui.compose.theme.DailyTheme
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.StatusBarUtil
import java.io.File

class WallpaperActivity : QRXActivity() {

    private val binding by lazy {
        ActivityWallpaperBinding.inflate(layoutInflater)
    }

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            saveWallpaper(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            initCompose()

            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                var wallpaper by remember { mutableStateOf(safeWallpaperBitmap) }
                var wallpaperAlpha by remember { mutableFloatStateOf(safeWallpaperAlpha) }
                var cardAlpha by remember {
                    mutableFloatStateOf(sharedPreferences.getFloat(ConstUtil.CARD_ALPHA_KEY, 0.7f))
                }

                val updateWallpaperState = { newBitmap: android.graphics.Bitmap? ->
                    wallpaper = newBitmap
                    if (newBitmap != null) {
                        StatusBarUtil.setLightStausBarsFromBitmap(newBitmap, wallpaperAlpha, window)
                    }
                }

                WallpaperScreen(
                    wallpaperBitmap = wallpaper,
                    initialWallpaperAlpha = wallpaperAlpha,
                    initialCardAlpha = cardAlpha,
                    onWallpaperAlphaChange = { alpha ->
                        wallpaperAlpha = alpha
                        sharedPreferences.edit { putFloat(ConstUtil.WALLPAPER_ALPHA_KEY, alpha) }
                        wallpaper?.let {
                            StatusBarUtil.setLightStausBarsFromBitmap(it, alpha, window)
                        }
                    },
                    onCardAlphaChange = { alpha ->
                        cardAlpha = alpha
                        sharedPreferences.edit { putFloat(ConstUtil.CARD_ALPHA_KEY, alpha) }
                    },
                    onAddWallpaperClick = {
                        pickMedia.launch("image/*")
                    },
                    onDeleteWallpaperClick = {
                        FileUtil().deleteFile(ConstUtil.WALLPAPER_PATH)
                        wallpaper = null
                    },
                    ::finish
                )

                LaunchedEffect(Unit) {
                    this@WallpaperActivity.onWallpaperUpdated = updateWallpaperState
                }
            }
        }
    }

    private var onWallpaperUpdated: ((android.graphics.Bitmap?) -> Unit)? = null

    private fun saveWallpaper(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(ConstUtil.WALLPAPER_PATH)
                if (!file.parentFile!!.exists()) {
                    file.parentFile!!.mkdirs()
                }
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                val bitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
                this.wallpaperBitmap = bitmap
                onWallpaperUpdated?.invoke(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun qrx() {
        val qrx = (this as QRXActivity)
        qrx.init(binding.wallpaper, binding.appBarLayout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}