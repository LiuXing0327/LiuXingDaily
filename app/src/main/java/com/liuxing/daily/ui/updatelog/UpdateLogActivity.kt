package com.liuxing.daily.ui.updatelog

import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.liuxing.daily.R
import com.liuxing.daily.data.VersionLogData
import com.liuxing.daily.databinding.ActivityUpdateLogBinding
import com.liuxing.daily.markdown.MarkdownParser
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.ThemeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader


class UpdateLogActivity : QRXActivity() {

    private lateinit var activityUpdateLogBinding: ActivityUpdateLogBinding
    private lateinit var typedValue: TypedValue
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        activityUpdateLogBinding = ActivityUpdateLogBinding.inflate(layoutInflater)
        setContentView(activityUpdateLogBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
        }
        qrx()
        initData()
    }

    private fun qrx() {
        val qrx = (this as QRXActivity)
        qrx.init(activityUpdateLogBinding.wallpaper, activityUpdateLogBinding.appBarLayout)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(300)
                    qrx.checkStatusBarColor(true)
                }
            }
        }
    }


    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initSharedPreferences()
        getUpdateLog(sharedPreferences!!.getInt("update_log_sort_by", 0))
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
                    currentVersionLog.add(line.replace("-", "- ").trim())
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
            // 把 V 替换为 # V
            val version = versionLogData.version.replaceFirstChar { "# V" }
            sb.append("${version}(${versionLogData.date})\n")
            versionLogData.versionList.forEach {
                sb.append("$it\n")
            }
            sb.append("\n")
        }
        val parsed = MarkdownParser.parseMarkdown((sb.toString()))
        activityUpdateLogBinding.tvUpdateLog.text = parsed
    }
}