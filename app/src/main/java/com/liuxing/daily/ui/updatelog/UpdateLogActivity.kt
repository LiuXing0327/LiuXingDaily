package com.liuxing.daily.ui.updatelog

import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.preference.PreferenceManager
import com.liuxing.daily.R
import com.liuxing.daily.data.VersionLogData
import com.liuxing.daily.databinding.ActivityUpdateLogBinding
import com.liuxing.daily.util.WindowUtil
import java.io.BufferedReader


class UpdateLogActivity : AppCompatActivity() {

    private lateinit var activityUpdateLogBinding: ActivityUpdateLogBinding
    private lateinit var typedValue: TypedValue
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        activityUpdateLogBinding = ActivityUpdateLogBinding.inflate(layoutInflater)
        setContentView(activityUpdateLogBinding.root)
        /*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }*/
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initSharedPreferences()
        getUpdateLog(sharedPreferences!!.getInt("update_log_sort_by", 0))
        initStatusBarColor()
    }

    /**
     * 初始化工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(activityUpdateLogBinding.toolbar)
        this.supportActionBar?.setDisplayShowTitleEnabled(false)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getString(R.string.update_log).also { activityUpdateLogBinding.toolbar.title = it }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_update_log, menu)
        val currentSortIndex = sharedPreferences!!.getInt("update_log_sort_by", 0)
        when (currentSortIndex) {
            1 -> menu!!.findItem(R.id.item_old_to_new).isChecked = true
            else -> menu!!.findItem(R.id.item_new_to_old).isChecked = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.item_new_to_old -> sortBy(0)

            R.id.item_old_to_new -> sortBy(1)
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 初始化偏好
     */
    private fun initSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    /**
     * 排序
     *
     * @param sortIndex 排序索引 0 -> 正 1 -> 倒
     */
    private fun sortBy(sortIndex: Int) {
        val currentSortIndex = sharedPreferences!!.getInt("update_log_sort_by", 0)
        if (currentSortIndex != sortIndex) {
            sharedPreferences!!.edit().putInt("update_log_sort_by", sortIndex).apply()
            invalidateOptionsMenu()
            getUpdateLog(sortIndex)
        }
    }

    /**
     * 获取更新日志
     */
    private fun getUpdateLog(sortIndex: Int) {
        val versionLog = mutableListOf<VersionLogData>()
        val inputStream = assets.open("UpdateLogText.txt")
        val bufferedReader = BufferedReader(inputStream.reader())
        var currentVersion: String? = null
        var currentDate: String? = null
        val currentVersionLog = mutableListOf<String>()
        bufferedReader.forEachLine { line ->
            when {
                line.matches(Regex("V\\d+(\\.\\d+)*\\(\\d{4}-\\d{2}-\\d{2}\\)")) -> {
                    if (currentVersion != null && currentDate != null) {
                        versionLog.add(
                            VersionLogData(
                                currentVersion!!,
                                currentDate!!,
                                currentVersionLog.toList()
                            )
                        )
                    }
                    currentVersion = line.substringBefore("(")
                    currentDate = line.substringAfter("(").substringBefore(")")
                    currentVersionLog.clear()
                }

                line.startsWith("-") -> {
                    currentVersionLog.add(line.trim())
                }
            }
        }
        if (currentVersion != null && currentDate != null) {
            versionLog.add(
                VersionLogData(
                    currentVersion!!,
                    currentDate!!,
                    currentVersionLog.toList()
                )
            )
        }
        bufferedReader.close()

        val sb = StringBuilder()
        if (sortIndex == 0) {
            versionLog.reverse()
        }
        versionLog.forEach { versionLogData ->
            sb.append("${versionLogData.version}(${versionLogData.date})\n")
            versionLogData.versionList.forEach {
                sb.append("$it\n")
            }
            sb.append("\n")
        }
        activityUpdateLogBinding.tvUpdateLog.text = sb.toString()
    }

    /**
     * 初始化状态栏颜色
     */
    private fun initStatusBarColor() {
        typedValue = TypedValue()
        theme.resolveAttribute(
            R.attr.collapsed_status_bar, typedValue, true
        )
        WindowUtil.FollowPatternSetColor(window, typedValue.data)
        window.statusBarColor =
            ContextCompat.getColor(this@UpdateLogActivity, android.R.color.transparent)
        setScrollStatusBarColor()
    }

    /**
     * 设置滚动后状态栏颜色
     */
    private fun setScrollStatusBarColor() {
        activityUpdateLogBinding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == 0) window.statusBarColor =
                ContextCompat.getColor(
                    this@UpdateLogActivity,
                    android.R.color.transparent
                ) else window.statusBarColor = typedValue.data
        })
    }
}