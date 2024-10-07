package com.liuxing.daily.ui.look

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.adapter.LookDailyPagerAdapter
import com.liuxing.daily.databinding.ActivityLookDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.ui.edit.EditDailyActivity
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.util.Date


class LookDailyActivity : AppCompatActivity() {

    private lateinit var lookDailyBinding: ActivityLookDailyBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailyEntity: DailyEntity
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lookDailyBinding = ActivityLookDailyBinding.inflate(layoutInflater)
        setContentView(lookDailyBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initData(savedInstanceState)
    }

    /**
     * 初始化数据
     */
    private fun initData(savedInstanceState: Bundle?) {
        setActionBar()
        initViewModel()
        loadDailyToViewPager(savedInstanceState)
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(lookDailyBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    /**
     * 加载日记
     */
    private fun loadDailyToViewPager(savedInstanceState: Bundle?) {
        dailyViewModel.queryAllDaily().observe(this, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                if (value.isNotEmpty()) {
                    val sortedByDescending = value.sortedByDescending {
                        val dateString = DateUtil.getDateString(2, Date(it.dateTime!!))
                        DateUtil.dateStringToDate(dateString.substring(0, 7), 3)
                    }
                    if (savedInstanceState != null) currentIndex =
                        savedInstanceState.getInt(VIEW_PAGER_INDEX, -1)
                    else {
                        val position = intent.getIntExtra("POSITION", 0)
                        val intentPosition = value[position]
                        currentIndex =
                            sortedByDescending.indexOfFirst { it.id == intentPosition.id }
                    }
                    dailyEntity = sortedByDescending[currentIndex]
                    val lookDailyPagerAdapter =
                        LookDailyPagerAdapter(this@LookDailyActivity, sortedByDescending)
                    lookDailyBinding.viewPagerDaily.adapter = lookDailyPagerAdapter
                    lookDailyBinding.viewPagerDaily.setCurrentItem(currentIndex, false)

                    lookDailyBinding.viewPagerDaily.registerOnPageChangeCallback(object :
                        ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            currentIndex = position
                            dailyEntity = sortedByDescending[currentIndex]
                        }
                    })
                } else finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_look_daily, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_delete -> {
                MaterialAlertDialogBuilder(this@LookDailyActivity)
                    .setMessage("确定永久删除这篇日记吗？")
                    .setPositiveButton(getString(R.string.sure)) { dialog, which ->
                        dailyViewModel.deleteDaily(dailyEntity)
                        finish()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create()
                    .show()
            }

            R.id.item_edit -> {
                val intent = Intent()
                intent.putExtra("daily_id", dailyEntity.id)
                intent.putExtra("daily_title", dailyEntity.title)
                intent.putExtra("daily_content", dailyEntity.content)
                intent.putExtra("daily_date_time", dailyEntity.dateTime)
                intent.putExtra(
                    "daily_backgroundColorIndex",
                    dailyEntity.backgroundColorIndex
                )
                intent.setClass(this@LookDailyActivity, EditDailyActivity::class.java)
                startActivity(intent)
            }

            R.id.item_copy -> {
                dailyEntity.content?.let { CopyUtil.copyTextToClipboard(this, it) }
                SnackbarUtil.showSnackbarShort(lookDailyBinding.viewPagerDaily, "复制成功")
            }

            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(VIEW_PAGER_INDEX, currentIndex)
    }
}

private const val VIEW_PAGER_INDEX = "viewPagerIndex"