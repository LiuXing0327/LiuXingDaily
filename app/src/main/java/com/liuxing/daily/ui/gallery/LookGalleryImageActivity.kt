package com.liuxing.daily.ui.gallery

import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.liuxing.daily.R
import com.liuxing.daily.adapter.LookGalleryImageAdapter
import com.liuxing.daily.databinding.ActivityLookGalleryImageBinding
import com.liuxing.daily.ui.immersive.ImmersiveActivity
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.io.File

class LookGalleryImageActivity : ImmersiveActivity() {

    lateinit var lookGalleryImageBinding: ActivityLookGalleryImageBinding
    private lateinit var dailyViewModel: DailyViewModel
    private val imageList = mutableSetOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        lookGalleryImageBinding = ActivityLookGalleryImageBinding.inflate(layoutInflater)
        setContentView(lookGalleryImageBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initViewmodel()
        loadImage()
        initImagePager()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(lookGalleryImageBinding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    /**
     * 初始化视图模型
     */
    private fun initViewmodel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    /**
     * 加载图片
     */
    private fun loadImage() {
        val fileUtil = FileUtil()
        val imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageDir?.let { dir ->
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && fileUtil.isImageFile(file)) {
                        imageList.add(file)
                    }
                }
            }
        }
    }

    /**
     * 初始化图片页面
     */
    private fun initImagePager() {
        val lookGalleryImageAdapter = LookGalleryImageAdapter(this, imageList.toList())
        lookGalleryImageBinding.imagePager.adapter = lookGalleryImageAdapter
        val position = intent.getIntExtra("look_all_daily_image_position", 0)
        lookGalleryImageBinding.imagePager.setCurrentItem(position, false)
        lookGalleryImageBinding.toolbar.title = "${position + 1}/${imageList.size}"
        lookGalleryImageBinding.imagePager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                lookGalleryImageBinding.toolbar.title = "${position + 1}/${imageList.size}"
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}