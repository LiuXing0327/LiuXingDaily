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

package com.liuxing.daily.ui.qrx

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.extension.checkFileExistsToPath
import com.liuxing.daily.extension.getFileMD5
import com.liuxing.daily.extension.setLightStausBarsFromBitmap
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.viewmodel.MainViewModel
import java.io.File

open class QRXActivity : AppCompatActivity() {

    /**
     * 用于显示壁纸的 [ImageView].
     */
    private lateinit var wallpaper: ImageView

    /**
     * 用于改变状态栏模式的 [AppBarLayout].
     */
    private lateinit var appBarLayout: AppBarLayout

    protected lateinit var wallpaperBitmap: Bitmap
    val safeWallpaperBitmap: Bitmap?
        get() = if (::wallpaperBitmap.isInitialized) wallpaperBitmap else null

    private val mainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private var wallpaperMD5Map = mutableMapOf<String, String>()
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private var wallpaperAlpha = 0.15f
    val safeWallpaperAlpha
        get() = wallpaperAlpha

    private var cardAlpha = 0.7f
    val safeCardAlpha
        get() = cardAlpha

    /**
     * 初始化
     *
     * @param wallpaper 显示壁纸的 [ImageView].
     * @param appBarLayout 用于改变状态栏模式的 [AppBarLayout].
     */
    fun init(wallpaper: ImageView, appBarLayout: AppBarLayout) {
        this.wallpaper = wallpaper
        this.appBarLayout = appBarLayout

        val fileMD5 = File(ConstUtil.WALLPAPER_PATH).getFileMD5()
        if (wallpaperMD5Map.isEmpty() || wallpaperMD5Map["QRX"] != fileMD5) {
            wallpaperMD5Map["QRX"] = fileMD5
            setWallpaperAndStausBar()
        }

        initView()

        val wallpaperAlpha = sharedPreferences!!.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY, 0.15F)
        setWallpaperAlpha(wallpaperAlpha)
    }

    /**
     * 初始化
     *
     * @param dailyListView 显示日记列表的 [RecyclerView].
     * @param dailyList 日记数据列表。
     */
    fun init(dailyListView: RecyclerView, dailyList: List<DailyEntity>) {
        if (::wallpaperBitmap.isInitialized) {
            dailyListView.setLightStausBarsFromBitmap(dailyList) {
                setLightStausBarsFromBitmap(wallpaperBitmap, wallpaperAlpha, window)
            }
        }
    }

    /**
     * 初始化 Compose 壁纸以及状态栏。
     */
    fun initCompose() {
        if (File(ConstUtil.WALLPAPER_PATH).checkFileExistsToPath()) {
            val wallpaperAlpha = sharedPreferences!!.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY, 0.15F)
            this.wallpaperAlpha = wallpaperAlpha

            val cardAlpha = sharedPreferences!!.getFloat(ConstUtil.CARD_ALPHA_KEY,0.7f)
            this.cardAlpha = cardAlpha
            try {
                val bitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
                this.wallpaperBitmap = bitmap

                setLightStausBarsFromBitmap(bitmap, wallpaperAlpha, window)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun oldWallpaperNameAreRenamed() {
    }


    /**
     * 初始化 View.
     */
    private fun initView() {
        appBarLayout.addOnOffsetChangedListener { _, _ ->
            val offsetChange = mainViewModel.enableAppBarOffsetChange.value ?: true
            checkStatusBarColor(offsetChange)
        }
    }

    /**
     * 检查状态栏颜色。
     */
    fun checkStatusBarColor(offsetChange: Boolean) {
        if (::wallpaperBitmap.isInitialized) {
            getBitmap(wallpaperBitmap)?.let { bitmap ->
                if (offsetChange) checkStatusBarColor(
                    appBarLayout, window, bitmap, wallpaperAlpha
                )
            }
        }
    }

    fun checkStatusBarColorForCompose(topAppBarColor: Color) {
        val bitmap = safeWallpaperBitmap ?: return
        checkStatusBarColor(
            topAppBarColor = topAppBarColor.toArgb(),
            window = window,
            bitmap = bitmap,
            wallpaperAlpha = wallpaperAlpha
        )
    }

    /**
     * 设置壁纸和状态栏。
     */
    private fun setWallpaperAndStausBar() {
        if (File(ConstUtil.WALLPAPER_PATH).checkFileExistsToPath()) {
            wallpaperBitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
            wallpaper.setImageBitmap(wallpaperBitmap)
            setLightStausBarsFromBitmap(wallpaperBitmap, wallpaperAlpha, window)
        }
    }

    /**
     * 获取当前页面壁纸的 MD5 值。
     *
     * @param key 页面键
     *
     * @return 返回当前页面壁纸的 MD5 值。
     */
    fun getWallpaperMD5(key: String = PageWallpaper.QRX.key): String {
        return wallpaperMD5Map[key] ?: ""
    }

    fun setWallpaperAlpha(alpha: Float) {
        wallpaper.alpha = alpha
        wallpaperAlpha = alpha
    }
}