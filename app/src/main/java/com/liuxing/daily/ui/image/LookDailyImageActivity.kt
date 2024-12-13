package com.liuxing.daily.ui.image

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.liuxing.daily.R
import com.liuxing.daily.adapter.LookImageAdapter
import com.liuxing.daily.databinding.ActivityLookDailyImageBinding
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.viewmodel.DailyViewModel

class LookDailyImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLookDailyImageBinding
    private lateinit var dailyViewModel: DailyViewModel
    private var dailyUuid: String? = ""
    private var currentImagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLookDailyImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
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
        loadDailyImage()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(binding.toolbar)
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
     * 加载日记图片
     */
    private fun loadDailyImage() {
        dailyUuid = intent.getStringExtra("look_daily_image_uuid")
        val dailyImagePosition = intent.getIntExtra("look_daily_image_position", 0)
        dailyViewModel.queryDailyImageByUuid(dailyUuid!!).observe(this) { dailyImageList ->
            val existingImagePaths = dailyImageList.map { it.imagePath }.toSet()
            if (existingImagePaths.isNotEmpty()) {
                val list = existingImagePaths.toList()
                binding.imagePager.adapter = LookImageAdapter(this, list)
                if (dailyImagePosition in list.indices) {
                    currentImagePath = list[dailyImagePosition]!!
                    binding.imagePager.currentItem = dailyImagePosition
                } else {
                    currentImagePath = list.firstOrNull().toString()
                    binding.imagePager.currentItem = dailyImagePosition - 1
                }
                binding.toolbar.title = "${dailyImagePosition + 1}/${dailyImageList.size}"
                binding.imagePager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        binding.toolbar.title = "${position + 1}/${dailyImageList.size}"
                    }
                })
            } else {
                finish()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_look_image, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        when (itemId) {

            R.id.item_delete -> {
                if (currentImagePath.isNotEmpty()) {
                    FileUtil().deleteFile(currentImagePath)
                    dailyViewModel.deleteSelectPathImage(currentImagePath)
                }
            }

            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        dailyUuid?.let { dailyViewModel.queryDailyImageByUuid(it).removeObservers(this) }
    }
}