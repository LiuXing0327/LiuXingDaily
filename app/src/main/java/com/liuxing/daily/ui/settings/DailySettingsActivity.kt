package com.liuxing.daily.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailySettingsAdapter
import com.liuxing.daily.adapter.DailySettingsAdapter.MarginItemDecoration
import com.liuxing.daily.data.DailySettingsData
import com.liuxing.daily.databinding.ActivityDailySettingsBinding
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil


class DailySettingsActivity : QRXActivity() {

    private val binding: ActivityDailySettingsBinding by lazy {
        ActivityDailySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, 0, navigationBars.right, navigationBars.bottom)
            insets
        }

        initData()
        (this as QRXActivity).init(binding.wallpaper, binding.appBarLayout)
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initRecyclerView()
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
        binding.toolbar.title = getString(R.string.daily_settings)
    }

    /**
     * 初始化 RecyclerView
     */
    private fun initRecyclerView() {
        val recyclerView = binding.listFragment.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dailySettingsDataList: List<DailySettingsData> = listOf(
            titleSettingsData(),
            weekSettingsData()
        )
        val dailySettingsAdapter = DailySettingsAdapter { data, newValue ->
            SharedPreferencesUtil.putBoolean(this, data.key, newValue)
        }

        recyclerView.adapter = dailySettingsAdapter
        recyclerView.addItemDecoration(MarginItemDecoration(this))

        dailySettingsAdapter.setDailySettingsDataList(dailySettingsDataList)
    }

    /**
     * 标题设置数据
     *
     * @return 标题设置数据
     */
    private fun titleSettingsData(): DailySettingsData {
        val titleKey = DailySettingsConst.TITLE_SWITCH_KEY

        return DailySettingsData(
            titleKey, getString(R.string.title), SharedPreferencesUtil.getBoolean(
                this, titleKey, false
            ), R.drawable.baseline_title_24
        )
    }

    /**
     * 星期设置数据
     *
     * @return 星期设置数据
     */
    private fun weekSettingsData(): DailySettingsData {
        val weekSwitchKey = DailySettingsConst.WEEK_SWITCH_KEY

        return DailySettingsData(
            weekSwitchKey, getString(R.string.week),
            SharedPreferencesUtil.getBoolean(this, weekSwitchKey, true),
            R.drawable.outline_calendar_month_24
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}