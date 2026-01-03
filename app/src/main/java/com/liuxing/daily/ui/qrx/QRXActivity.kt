/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.ui.qrx

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var wallpaperBitmap: Bitmap
    private val mainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private var wallpaperMD5Map = mutableMapOf<String, String>()
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

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
        dailyListView.setLightStausBarsFromBitmap(dailyList) {
            setLightStausBarsFromBitmap(wallpaperBitmap, wallpaper, window)
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
                    appBarLayout, window, bitmap, wallpaper
                )
            }
        }
    }

    /**
     * 设置壁纸和状态栏。
     */
    private fun setWallpaperAndStausBar() {
        if (File(ConstUtil.WALLPAPER_PATH).checkFileExistsToPath()) {
            wallpaperBitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
            wallpaper.setImageBitmap(wallpaperBitmap)
            setLightStausBarsFromBitmap(wallpaperBitmap, wallpaper, window)
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
    }
}