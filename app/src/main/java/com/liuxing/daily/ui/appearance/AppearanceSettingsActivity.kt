package com.liuxing.daily.ui.appearance

import android.animation.ValueAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.listitem.ListItemCardView
import com.google.android.material.shape.ShapeAppearanceModel
import com.liuxing.daily.R
import com.liuxing.daily.adapter.ThemeColorAdapter
import com.liuxing.daily.data.DailySettingsData
import com.liuxing.daily.data.ThemeColorData
import com.liuxing.daily.databinding.ActivityAppearanceSettingsBinding
import com.liuxing.daily.extension.initDefIcon
import com.liuxing.daily.extension.setVisibility
import com.liuxing.daily.extension.toggleDoneIcon
import com.liuxing.daily.material.widget.DailyMaterialSwitch
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SPAN_COUNT = 4

class AppearanceSettingsActivity : QRXActivity() {

    private lateinit var appearanceSettingsBinding: ActivityAppearanceSettingsBinding
    private var sharedPreferences: SharedPreferences? = null

    /**
     * 动态取色切换键。
     */
    private val dynamicColorSwitchKey = AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY

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
        (this as QRXActivity).init(
            appearanceSettingsBinding.wallpaper,
            appearanceSettingsBinding.appBarLayout
        )
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initSharedPreferences()
        changeThemeMode()
        setColorData()
        setDynamicColorData()
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
        val id = when (themeModeIndex) {
            1 -> appearanceSettingsBinding.floatingToolbarButtonLight
            2 -> appearanceSettingsBinding.floatingToolbarButtonNight
            else -> appearanceSettingsBinding.floatingToolbarButtonFollowSystem
        }.apply {
            isChecked = true
        }.id

        changeDoneIcon(id)

        appearanceSettingsBinding.floatingToolbarButtonLight.setOnClickListener(::onFloatingButtonClicked)
        appearanceSettingsBinding.floatingToolbarButtonNight.setOnClickListener(::onFloatingButtonClicked)
        appearanceSettingsBinding.floatingToolbarButtonFollowSystem.setOnClickListener(::onFloatingButtonClicked)
    }

    /**
     * FloatingToolbar 按钮点击事件分发处理
     *
     * 所有按钮都执行 -> [selectTheme]
     *
     * @param view 被点击的按钮 View
     */
    private fun onFloatingButtonClicked(view: View) {
        when (val id = view.id) {
            R.id.floating_toolbar_button_light -> selectTheme(1, id)
            R.id.floating_toolbar_button_night -> selectTheme(2, id)
            R.id.floating_toolbar_button_follow_system -> selectTheme(0, id)
        }
    }

    /**
     * 选择主题
     *
     * @param mode 主题模型索引
     * @param id 按钮 Id
     */
    private fun selectTheme(mode: Int, id: Int) {
        ThemeUtil.setThemeMode(mode)
        saveThemeMode(mode)

        with(appearanceSettingsBinding) {
            floatingToolbarButtonLight.isChecked = id == floatingToolbarButtonLight.id
            floatingToolbarButtonNight.isChecked = id == floatingToolbarButtonNight.id
            floatingToolbarButtonFollowSystem.isChecked = id == floatingToolbarButtonFollowSystem.id
        }

        changeDoneIcon(id)
    }

    /**
     * 根据按钮 [id] 切换完成图标。
     *
     *
     * @param id 按钮 Id.
     */
    private fun changeDoneIcon(id: Int) {
        mapOf(
            appearanceSettingsBinding.floatingToolbarButtonLight to ContextCompat.getDrawable(
                this, R.drawable.outline_light_mode_24
            ),
            appearanceSettingsBinding.floatingToolbarButtonNight to ContextCompat.getDrawable(
                this, R.drawable.outline_mode_night_24
            ),
            appearanceSettingsBinding.floatingToolbarButtonFollowSystem to ContextCompat.getDrawable(
                this, R.drawable.outline_phone_android_24
            )
        ).forEach { (button, drawable) ->
            button.let {
                it.initDefIcon(drawable)
                it.toggleDoneIcon(id)
            }
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
            ),

            ThemeColorData(
                6,
                ContextCompat.getColor(this, R.color.md_theme_primaryFixed_light_cyan),
                ContextCompat.getColor(this, R.color.md_theme_primary_light_cyan),
                ContextCompat.getColor(this, R.color.md_theme_inversePrimary_light_cyan),
                false
            )
        )
        val themeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        colorDataList.forEach {
            it.isSelected = it.id == themeColorId
        }
        val themeColorAdapter = ThemeColorAdapter(this, colorDataList)

        with(appearanceSettingsBinding.recyclerTheme) {
            layoutManager = GridLayoutManager(this@AppearanceSettingsActivity, SPAN_COUNT)
            adapter = themeColorAdapter
            addItemDecoration(
                ThemeColorAdapter.MarginItemDecoration(
                    this@AppearanceSettingsActivity,
                    SPAN_COUNT
                )
            )
        }
    }

    /**
     * 设置动态取色数据。
     */
    private fun setDynamicColorData() {
        // 如果动态取色不可用，则隐藏容器。
        if (!DynamicColors.isDynamicColorAvailable()) {
            appearanceSettingsBinding.settingsContainer.root.setVisibility(false)
            return
        }

        // 获取动态取色开关值
        val dynamicColorChecked =
            SharedPreferencesUtil.getBoolean(this, dynamicColorSwitchKey, false)

        val data = DailySettingsData(
            AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY,
            getString(R.string.dynamic_color),
            dynamicColorChecked,
            R.drawable.outline_palette_preference_color_primary_24,
            getString(R.string.dynamic_color_supporting_string)
        )

        val startIcon = appearanceSettingsBinding.settingsContainer.listItemStartIcon
        val textView = appearanceSettingsBinding.settingsContainer.listItemText
        val switch = appearanceSettingsBinding.settingsContainer.listItemSwitch
        val cardView = appearanceSettingsBinding.settingsContainer.listItemCardView
        val supportingText =
            appearanceSettingsBinding.settingsContainer.listItemSupportingText

        val px = 16 * resources.displayMetrics.density
        val shape = ShapeAppearanceModel.builder()
            .setAllCornerSizes(px)
            .build()

        appearanceSettingsBinding.settingsContainer.listItemCardView.shapeAppearanceModel =
            shape

        startIcon.setImageResource(data.iconResource)
        textView.text = data.text
        supportingText.text = data.supportingString
        supportingText.setVisibility(true)
        onDynamicColorDataChanged(startIcon, switch, cardView, data)
        animateThemeList(!data.checked)

        cardView.setOnClickListener {
            val newChecked = !cardView.isChecked
            data.checked = newChecked
            onDynamicColorDataChanged(startIcon, switch, cardView, data)
            SharedPreferencesUtil.putBoolean(this, dynamicColorSwitchKey, data.checked)

            animateThemeList(!data.checked)

            lifecycleScope.launch {
                delay(300)

                val animation =
                    ActivityOptions.makeCustomAnimation(
                        this@AppearanceSettingsActivity,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                finish()
                startActivity(
                    Intent(this@AppearanceSettingsActivity, AppearanceSettingsActivity::class.java),
                    animation.toBundle()
                )
            }
        }

    }

    /**
     * 当动态取色数据发生变化时
     *
     * 数据发生变化时，会主动使用 [onDynamicColorStatusChanged] 更新视图状态。
     *
     * @param startIcon 动态取色的图标。
     * @param switch 动态取色启用开关。
     * @param cardView 动态取色的卡片视图。
     * @param data 动态取色数据。
     */
    private fun onDynamicColorDataChanged(
        startIcon: ImageView,
        switch: DailyMaterialSwitch,
        cardView: ListItemCardView,
        data: DailySettingsData
    ) {
        onDynamicColorStatusChanged(listOf(startIcon, switch, cardView), data)
    }

    /**
     * 当动态取色视图状态发生变化时。
     *
     * @param views 需要更新状态的视图列表。
     * @param data 新的数据。
     */
    private fun onDynamicColorStatusChanged(views: List<View>, data: DailySettingsData) {
        views.forEach { view ->
            /*
                如果 View 是 DailyMaterialSwitch 或 ListItemCardView，调用 isChecked 设置状态。
                不是则通过 isSelected 设置状态。
             */
            if (view is DailyMaterialSwitch || view is ListItemCardView) {
                view.isChecked = data.checked
            } else {
                view.isSelected = data.checked
            }
        }
    }

    /**
     * 主题列表的展开与收起动画
     *
     * @param show 是否展开列表：
     *              true -> 展开。else -> 收起
     */
    private fun animateThemeList(show: Boolean) {
        val view = appearanceSettingsBinding.themeListCard

        // 防止动画叠加
        view.animate().cancel()

        if (show) {
            if (view.isVisible) return

            view.visibility = View.VISIBLE
            view.alpha = 0f

            // 测量高度
            val parent = view.parent as View
            val widthSpec =
                View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            val targetHeight = view.measuredHeight

            // 从 0 开始展开
            val lp = view.layoutParams
            lp.height = 0
            view.layoutParams = lp

            val animator = ValueAnimator.ofInt(0, targetHeight)
            animator.duration = 250

            animator.addUpdateListener {
                lp.height = it.animatedValue as Int
                view.layoutParams = lp
            }

            animator.doOnEnd {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                view.layoutParams = lp
            }

            animator.start()

            view.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        } else {
            if (view.isGone) return

            val initHeight = view.height
            val lp = view.layoutParams

            val animator = ValueAnimator.ofInt(initHeight, 0)
            animator.duration = 200

            animator.addUpdateListener {
                lp.height = it.animatedValue as Int
                view.layoutParams = lp

            }

            animator.doOnEnd {
                // 动画结束后隐藏
                view.setVisibility(false)

                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                view.layoutParams = lp
            }

            animator.start()

            view.animate()
                .alpha(0f)
                .setDuration(150)
                .start()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}