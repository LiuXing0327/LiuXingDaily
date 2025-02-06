package com.liuxing.daily.ui.appearance

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.liuxing.daily.R
import com.liuxing.daily.adapter.ThemeColorAdapter
import com.liuxing.daily.data.ThemeColorData
import com.liuxing.daily.databinding.ActivityAppearanceSettingsBinding
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.util.WindowUtil

class AppearanceSettingsActivity : AppCompatActivity() {

    private lateinit var appearanceSettingsBinding: ActivityAppearanceSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        appearanceSettingsBinding = ActivityAppearanceSettingsBinding.inflate(layoutInflater)
        setContentView(appearanceSettingsBinding.root)
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
        initStatusBarColor()
        setColorData()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(appearanceSettingsBinding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            appearanceSettingsBinding.toolbar.title = getString(R.string.appearance)
        }
    }

    /**
     * 初始化状态栏颜色
     */
    private fun initStatusBarColor() {
        val typedValue = TypedValue()
        theme.resolveAttribute(
            R.attr.collapsed_status_bar, typedValue, true
        )
        WindowUtil.followPatternSetColor(window, this)
        window.statusBarColor =
            ContextCompat.getColor(this, android.R.color.transparent)
    }

    /**
     * 设置颜色数据
     */
    private fun setColorData() {
        val colorDataList = listOf(
            ThemeColorData(
                0,
                ContextCompat.getColor(this, R.color.md_theme_primaryFixed),
                ContextCompat.getColor(this, R.color.md_theme_primary),
                ContextCompat.getColor(this, R.color.md_theme_inversePrimary),
                false
            ),
            ThemeColorData(
                1,
                ContextCompat.getColor(this, R.color.md_theme_primaryFixed_red),
                ContextCompat.getColor(this, R.color.md_theme_primary_red),
                ContextCompat.getColor(this, R.color.md_theme_inversePrimary_red),
                false
            ),

            ThemeColorData(
                2,
                ContextCompat.getColor(this, R.color.md_theme_primaryFixed_green),
                ContextCompat.getColor(this, R.color.md_theme_primary_green),
                ContextCompat.getColor(this, R.color.md_theme_inversePrimary_green),
                false
            ),

            ThemeColorData(
                3,
                ContextCompat.getColor(this, R.color.md_theme_primaryFixed_blue),
                ContextCompat.getColor(this, R.color.md_theme_primary_blue),
                ContextCompat.getColor(this, R.color.md_theme_inversePrimary_blue),
                false
            ),

            ThemeColorData(
                4,
                ContextCompat.getColor(this, R.color.md_theme_primaryFixed_yellow),
                ContextCompat.getColor(this, R.color.md_theme_primary_yellow),
                ContextCompat.getColor(this, R.color.md_theme_inversePrimary_yellow),
                false
            ),

            ThemeColorData(
                5,
                ContextCompat.getColor(this, R.color.md_theme_primaryFixed_pink),
                ContextCompat.getColor(this, R.color.md_theme_primary_pink),
                ContextCompat.getColor(this, R.color.md_theme_inversePrimary_pink),
                false
            )
        )
        val themeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        colorDataList.forEach {
            it.isSelected = it.id == themeColorId
        }
        val themeColorAdapter = ThemeColorAdapter(this, colorDataList)
        appearanceSettingsBinding.recyclerTheme.layoutManager = GridLayoutManager(this, 4)
        appearanceSettingsBinding.recyclerTheme.adapter = themeColorAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}