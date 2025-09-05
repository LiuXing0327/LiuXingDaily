package com.liuxing.daily.ui.appearance

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.liuxing.daily.R
import com.liuxing.daily.adapter.ThemeColorAdapter
import com.liuxing.daily.data.ThemeColorData
import com.liuxing.daily.databinding.ActivityAppearanceSettingsBinding
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil

class AppearanceSettingsActivity : AppCompatActivity() {

    private lateinit var appearanceSettingsBinding: ActivityAppearanceSettingsBinding
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        appearanceSettingsBinding = ActivityAppearanceSettingsBinding.inflate(layoutInflater)
        setContentView(appearanceSettingsBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
        }
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initSharedPreferences()
        changeThemeMode()
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
     * 初始化 [sharedPreferences]
     */
    private fun initSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        }
    }

    /**
     * 更改主题模式
     */
    private fun changeThemeMode() {
        val themeModeIndex = sharedPreferences!!.getInt("theme_mode_preference", 0)
        when (themeModeIndex) {
            1 -> appearanceSettingsBinding.floatingToolbarButtonLight.isChecked = true

            2 -> appearanceSettingsBinding.floatingToolbarButtonNight.isChecked = true

            else -> appearanceSettingsBinding.floatingToolbarButtonFollowSystem.isChecked = true
        }

        appearanceSettingsBinding.floatingToolbarButtonLight.setOnClickListener {
            ThemeUtil.setThemeMode(1)
            saveThemeMode(1)
            appearanceSettingsBinding.floatingToolbarButtonLight.isChecked = true

            appearanceSettingsBinding.floatingToolbarButtonNight.isChecked = false
            appearanceSettingsBinding.floatingToolbarButtonFollowSystem.isChecked = false
        }

        appearanceSettingsBinding.floatingToolbarButtonNight.setOnClickListener {
            ThemeUtil.setThemeMode(2)
            saveThemeMode(2)
            appearanceSettingsBinding.floatingToolbarButtonLight.isChecked = false

            appearanceSettingsBinding.floatingToolbarButtonNight.isChecked = true

            appearanceSettingsBinding.floatingToolbarButtonFollowSystem.isChecked = false
        }

        appearanceSettingsBinding.floatingToolbarButtonFollowSystem.setOnClickListener {
            ThemeUtil.setThemeMode(0)
            saveThemeMode(0)
            appearanceSettingsBinding.floatingToolbarButtonLight.isChecked = false
            appearanceSettingsBinding.floatingToolbarButtonNight.isChecked = false

            appearanceSettingsBinding.floatingToolbarButtonFollowSystem.isChecked = true
        }

    }

    /**
     * 保存主题模式
     *
     * @param themeMode 主题模式索引
     */
    private fun saveThemeMode(themeMode: Int) {
        sharedPreferences!!.edit {
            putInt("theme_mode_preference", themeMode)
            apply()
        }
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