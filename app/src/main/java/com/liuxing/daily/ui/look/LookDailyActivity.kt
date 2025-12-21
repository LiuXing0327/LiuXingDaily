package com.liuxing.daily.ui.look

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.adapter.LookDailyPagerAdapter
import com.liuxing.daily.databinding.ActivityLookDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.ui.edit.EditDailyActivity
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.StatusBarUtil
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.io.File
import java.util.Date


class LookDailyActivity : AppCompatActivity() {

    private lateinit var lookDailyBinding: ActivityLookDailyBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailyEntity: DailyEntity
    private var currentIndex = 0
    private var originalSignalPassword = ""
    private lateinit var originalSignalPasswordMap: MutableMap<Long, String>
    private lateinit var tempSignalPasswordMap: MutableMap<Long, String>
    private var finalList = mutableListOf<DailyEntity>()
    private val unlockedIdMap = mutableMapOf<Long, String>()// 已解锁的日记 ID

    companion object {
        /**
         * 搜索关键词
         */
        var searchQuery = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        lookDailyBinding = ActivityLookDailyBinding.inflate(layoutInflater)
        setContentView(lookDailyBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
                            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
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
        initSharedPreferences()
        getSearchQuery()
        setupFloatingToolbar()
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
        dailyViewModel = ViewModelProvider(this)[DailyViewModel::class.java]
    }

    /**
     * 加载日记
     */
    private fun loadDailyToViewPager(savedInstanceState: Bundle?) {
        dailyViewModel.queryAllDaily().observe(this, Observer<List<DailyEntity>> { value ->
            val filter = value.filter { !it.isDeleted }
            if (filter.isNotEmpty()) {
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this@LookDailyActivity)
                val currentSortIndex = sharedPreferences?.getInt("daily_sort_by", 0)

                // 分离置顶项和非置顶项
                val pinnedItems = filter.filter { it.isPinned }.sortedByDescending { it.dateTime }
                val nonPinnedItems = filter.filterNot { it.isPinned }

                val sortedByDescending = if (currentSortIndex == 1) {
                    nonPinnedItems.sortedBy {
                        DateUtil.getDateString(0, Date(it.dateTime!!))
                    }
                } else {
                    nonPinnedItems.sortedByDescending {
                        DateUtil.getDateString(0, Date(it.dateTime!!))
                    }
                }

                finalList = mutableListOf()
                finalList.addAll(pinnedItems)
                finalList.addAll(sortedByDescending)

                if (currentIndex == 0) {
                    if (savedInstanceState != null) currentIndex =
                        savedInstanceState.getInt(VIEW_PAGER_INDEX, 0)
                    else {
                        val position = intent.getIntExtra("POSITION", 0)
                        if (position in filter.indices) {
                            val intentPosition = filter[position]
                            currentIndex =
                                finalList.indexOfFirst { it.id == intentPosition.id }
                        }
                    }
                }
                if (currentIndex >= finalList.size) {
                    currentIndex = finalList.size - 1
                }
                if (currentIndex in finalList.indices) {
                    dailyEntity = finalList[currentIndex]
                    val lookDailyPagerAdapter =
                        LookDailyPagerAdapter(this@LookDailyActivity, finalList)
                    lookDailyBinding.viewPagerDaily.adapter = lookDailyPagerAdapter
                    lookDailyBinding.viewPagerDaily.setCurrentItem(
                        currentIndex,
                        false
                    )
                    originalSignalPassword = dailyEntity.singlePassword ?: ""
                    lookDailyBinding.viewPagerDaily.registerOnPageChangeCallback(object :
                        ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            currentIndex = position
                            dailyEntity = finalList[currentIndex]
                            originalSignalPassword = dailyEntity.singlePassword ?: ""
                            originalSignalPasswordMap[dailyEntity.id!!] = originalSignalPassword
                            tempSignalPasswordMap[dailyEntity.id!!].let {
                                if (it.isNullOrEmpty()) originalSignalPassword
                            }
                            invalidateOptionsMenu()
                        }
                    })

                    originalSignalPasswordMap = mutableMapOf(
                        dailyEntity.id!! to originalSignalPassword
                    )
                    tempSignalPasswordMap = mutableMapOf(
                        dailyEntity.id!! to originalSignalPassword
                    )
                }
            } else finish()
        })
    }

    /**
     * 根据 Bitmap 的顶部颜色调整状态栏外观
     *
     * @param bitmap 用于分析的壁纸 Bitmap
     */
    fun setLightStausBarsFromBitmap(bitmap: Bitmap) {
        StatusBarUtil.setLightStausBarsFromBitmap(bitmap,lookDailyBinding.wallpaper,window)
    }

    /**
     * 获取搜索关键词
     */
    private fun getSearchQuery() {
        searchQuery = intent.getStringExtra("search_query") ?: ""
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_look_daily, menu)
        if (::originalSignalPasswordMap.isInitialized) {
            menu?.findItem(R.id.item_unlock)?.isVisible =
                originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() == false && !unlockedIdMap.contains(
                    dailyEntity.id
                )
            updateUnlockButtonVisibility()
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        dailyViewModel.queryAllDaily().removeObservers(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_delete -> {
                deleteOrRecyclerDaily()
            }

            R.id.item_edit -> {
                editDaily()
            }

            R.id.item_copy -> {
                copyAllContent()

            }

            R.id.item_unlock -> {
                unlockingDaily()
            }

            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 解锁日记
     */
    private fun unlockingDaily() {
        val inflate =
            layoutInflater.inflate(R.layout.dialog_input_password_layout, null)
        val inputPasswordLayout =
            inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
        val inputPassword = inflate.findViewById<TextInputEditText>(R.id.input_password)
        inputPasswordLayout.hint = getString(R.string.unlocked)
        var singlePassword = ""
        inputPassword.setText(singlePassword)
        MaterialAlertDialogBuilder(this@LookDailyActivity).apply {
            setTitle(getString(R.string.unlocked))
            setView(inflate)
            setPositiveButton(
                getString(R.string.sure)
            ) { _, _ ->
                singlePassword = inputPassword.text.toString()
                val lookDailyPagerFragment =
                    supportFragmentManager.findFragmentByTag("f${currentIndex}") as LookDailyPagerFragment
                when (val hashSHA256 = HashUtil.hashSHA256(singlePassword)) {
                    dailyEntity.singlePassword -> {
                        // originalSignalPasswordMap[dailyEntity.id!!] = ""
                        tempSignalPasswordMap[dailyEntity.id!!] =
                            inputPassword.text.toString()
                        unlockedIdMap[dailyEntity.id!!] = singlePassword
                        invalidateOptionsMenu()
                        lookDailyPagerFragment.updateSinglePassword(hashSHA256)
                    }

                    else -> {
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.floatingToolbarLayout,
                            getString(R.string.wrong_password)
                        )
                    }
                }
            }
            setNegativeButton(
                getString(R.string.forgot_password)
            ) { _, _ ->
                val inflate1 = LayoutInflater.from(this@LookDailyActivity)
                    .inflate(R.layout.dialog_input_password_layout, null)
                val inputPasswordLayout =
                    inflate1.findViewById<TextInputLayout>(R.id.input_password_layout)
                val inputPassword =
                    inflate1.findViewById<TextInputEditText>(R.id.input_password)
                inputPasswordLayout.hint = getString(R.string.key)
                MaterialAlertDialogBuilder(this@LookDailyActivity).apply {
                    setTitle(getString(R.string.forgot_password))
                    setView(inflate1)
                    setPositiveButton(
                        getString(R.string.sure)
                    ) { _, _ ->
                        val key = sharedPreferences?.getString(
                            "forget_password_key", ""
                        )
                        if (key != "") {
                            when {
                                HashUtil.hashSHA256(inputPassword.text.toString()) == key -> {
                                    dailyViewModel.updateDaily(
                                        DailyEntity(
                                            dailyEntity.id,
                                            dailyEntity.title,
                                            dailyEntity.content,
                                            dailyEntity.dateTime,
                                            dailyEntity.backgroundColorIndex,
                                            "",
                                            dailyEntity.moodIndex,
                                            dailyEntity.weatherIndex,
                                            dailyEntity.dailyUUID,
                                            false,
                                            dailyEntity.dailyLabel,
                                            isPinned = dailyEntity.isPinned
                                        )
                                    )
                                    SnackbarUtil.showSnackbarShort(
                                        lookDailyBinding.floatingToolbarLayout,
                                        getString(R.string.the_password_has_been_cleared)
                                    )
                                }

                                else -> SnackbarUtil.showSnackbarShort(
                                    lookDailyBinding.floatingToolbarLayout,
                                    getString(R.string.the_key_is_incorrect)
                                )
                            }
                        } else SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.floatingToolbarLayout,
                            getString(R.string.please_set_key)
                        )
                    }
                    setNegativeButton(getString(R.string.cancel), null)
                    create()
                    show()
                }
            }
            setNeutralButton(getString(R.string.cancel), null)
                .setCancelable(false)
                .create()
            show()
        }
    }

    /**
     * 复制当前日记的所有内容
     */
    private fun copyAllContent() {
        when {
            unlockedIdMap.contains(dailyEntity.id) || originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() -> {
                dailyEntity.content?.let {
                    CopyUtil.copyTextToClipboard(
                        this,
                        TextUtil.replaceTag(it, "")
                    )
                }
                SnackbarUtil.showSnackbarShort(
                    lookDailyBinding.floatingToolbarLayout,
                    getString(R.string.copy_successful)
                )
            }

            else -> {
                SnackbarUtil.showSnackbarShort(
                    lookDailyBinding.floatingToolbarLayout,
                    getString(R.string.please_unlock_it_first_to_verify_your_identity)
                )
            }
        }
    }

    /**
     * 编辑当前日记内容
     */
    private fun editDaily() {
        when {
            unlockedIdMap.contains(dailyEntity.id) || originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() -> {
                val intent = Intent()
                intent.putExtra("daily_id", dailyEntity.id)
                intent.putExtra("daily_title", dailyEntity.title)
                intent.putExtra("daily_content", dailyEntity.content)
                intent.putExtra("daily_date_time", dailyEntity.dateTime)
                intent.putExtra(
                    "daily_backgroundColorIndex",
                    dailyEntity.backgroundColorIndex
                )
                intent.putExtra(
                    "single_password",
                    tempSignalPasswordMap[dailyEntity.id!!]
                )
                intent.putExtra("mood_index", dailyEntity.moodIndex)
                intent.putExtra("weather_index", dailyEntity.weatherIndex)
                intent.putExtra("daily_uuid", dailyEntity.dailyUUID)
                intent.putExtra("daily_label", dailyEntity.dailyLabel)
                intent.putExtra("is_pinned", dailyEntity.isPinned)
                intent.setClass(this@LookDailyActivity, EditDailyActivity::class.java)
                startActivity(intent)
            }

            else -> {
                SnackbarUtil.showSnackbarShort(
                    lookDailyBinding.floatingToolbarLayout,
                    getString(R.string.please_unlock_it_first_to_verify_your_identity)
                )
            }
        }
    }

    /**
     * 删除或回收日记
     */
    private fun deleteOrRecyclerDaily() {
        when {
            unlockedIdMap.contains(dailyEntity.id) || originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() -> {
                val moveInRecyclerBin =
                    sharedPreferences!!.getBoolean(
                        "switch_delete_to_recycler_bin_daily",
                        true
                    )
                if (moveInRecyclerBin) {
                    MaterialAlertDialogBuilder(this).apply {
                        setMessage(getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin))
                        setPositiveButton(getString(R.string.sure)) { _, _ ->
                            dailyViewModel.updateDaily(
                                DailyEntity(
                                    dailyEntity.id,
                                    dailyEntity.title,
                                    dailyEntity.content,
                                    dailyEntity.dateTime,
                                    dailyEntity.backgroundColorIndex,
                                    dailyEntity.singlePassword,
                                    dailyEntity.moodIndex,
                                    dailyEntity.weatherIndex,
                                    dailyEntity.dailyUUID,
                                    true,
                                    dailyEntity.dailyLabel,
                                    isPinned = dailyEntity.isPinned
                                )
                            )
                        }
                            .setNegativeButton(getString(R.string.cancel), null)
                            .create()
                            .show()
                    }

                } else {
                    MaterialAlertDialogBuilder(this).apply {
                        setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                        setPositiveButton(getString(R.string.delete)) { _, _ ->
                            dailyEntity.dailyUUID?.let {
                                dailyViewModel.deletePathImageByDailyUuid(
                                    it
                                )
                                dailyViewModel.deletePathVideoByDailyUuid(
                                    it
                                )
                                dailyViewModel.deletePathAudioByDailyUuid(
                                    it
                                )
                            }
                            dailyViewModel.deleteDaily(dailyEntity)
                        }
                        setNegativeButton(getString(R.string.recycler_bin)) { _, _ ->
                            dailyViewModel.updateDaily(
                                DailyEntity(
                                    dailyEntity.id,
                                    dailyEntity.title,
                                    dailyEntity.content,
                                    dailyEntity.dateTime,
                                    dailyEntity.backgroundColorIndex,
                                    dailyEntity.singlePassword,
                                    dailyEntity.moodIndex,
                                    dailyEntity.weatherIndex,
                                    dailyEntity.dailyUUID,
                                    true,
                                    dailyEntity.dailyLabel,
                                    isPinned = dailyEntity.isPinned
                                )
                            )
                        }
                        setNeutralButton(getString(R.string.cancel), null)
                        create()
                        show()
                    }
                }
            }

            else -> {
                SnackbarUtil.showSnackbarShort(
                    lookDailyBinding.floatingToolbarLayout,
                    getString(R.string.please_unlock_it_first_to_verify_your_identity)
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(VIEW_PAGER_INDEX, currentIndex)
    }

    override fun onPause() {
        super.onPause()
        currentIndex = lookDailyBinding.viewPagerDaily.currentItem
    }

    override fun onResume() {
        super.onResume()
        if (File(ConstUtil.WALLPAPER_PATH).exists()) {
            val bitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
            setLightStausBarsFromBitmap(bitmap)
            lookDailyBinding.wallpaper.setImageBitmap(bitmap)
        }
        val wallpaperAlpha = sharedPreferences!!.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY, 0.15F)
        lookDailyBinding.wallpaper.alpha = wallpaperAlpha

        verifyPassword()
    }

    /**
     * 验证密码
     */
    private fun verifyPassword(){
        // 无原密码或当前无已解锁记录
        if (originalSignalPassword.isEmpty() || unlockedIdMap.isEmpty()) return

        val dailyId = dailyEntity.id ?: 0
        // 从 SharedPreferences 读取可能更新的密码
        val overrideSinglePassword = getSharedPreferences(
            "DAILY_CONTENT_UPDATE", MODE_PRIVATE
        ).getString("daily_update_single_password_${dailyEntity.dailyUUID}", "")

        val isOriginalMismatch =
            dailyEntity.singlePassword != originalSignalPasswordMap[dailyId]
        val isOverrideMismatch =
            !overrideSinglePassword.isNullOrEmpty() &&
                    overrideSinglePassword != dailyEntity.singlePassword
        if (isOriginalMismatch || isOverrideMismatch) {
            removeUnlockedIdKey(dailyId, overrideSinglePassword)
        }
    }

    /**
     * 当日记密码被修改或验证失败时，清除已解锁相关记录。
     *
     * @param dailyId 日记 ID
     * @param overrideSinglePassword 新的日记密码
     */
    private fun removeUnlockedIdKey(dailyId: Long, overrideSinglePassword: String?) {
        val unlockedMap = unlockedIdMap
        val isPasswordMismatch =
            HashUtil.hashSHA256(unlockedMap[dailyId].orEmpty()) != originalSignalPasswordMap[dailyId]
        val isOverrideChanged =
            !overrideSinglePassword.isNullOrEmpty() && overrideSinglePassword != dailyEntity.singlePassword

        if (isPasswordMismatch || isOverrideChanged) {
            unlockedMap.remove(dailyId)
            originalSignalPasswordMap[dailyId] = ""
            tempSignalPasswordMap[dailyId] = ""
        }
    }

    /**
     * 初始化 FloatingToolbar 的按钮点击事件
     *
     * 遍历 FloatingToolbar 中 OverflowLinearLayout 的 MaterialButton，统一设置点击监听器。
     * 当按钮被点击时，会调用 [onFloatingButtonClicked] 进行事件分发。
     */
    private fun setupFloatingToolbar() {
        val toolbarChild = lookDailyBinding.floatingToolbarChild
        for (i in 0 until toolbarChild.childCount) {
            val child = toolbarChild.getChildAt(i)
            if (child is MaterialButton) {
                child.setOnClickListener(::onFloatingButtonClicked)
            }
        }
    }

    /**
     * FloatingToolbar 按钮点击事件分发处理
     *
     * 根据被点击按钮的 id 调用对应操作：
     * - [R.id.floating_toolbar_button_delete] → 删除或回收日记
     * - [R.id.floating_toolbar_button_edit] → 编辑日记
     * - [R.id.floating_toolbar_button_copy] → 复制日记全部内容
     * - [R.id.floating_toolbar_button_unlock] → 解锁日记
     *
     * @param view 被点击的按钮 View
     */
    private fun onFloatingButtonClicked(view: View) {
        when (view.id) {
            R.id.floating_toolbar_button_delete -> deleteOrRecyclerDaily()
            R.id.floating_toolbar_button_edit -> editDaily()
            R.id.floating_toolbar_button_copy -> copyAllContent()
            R.id.floating_toolbar_button_unlock -> unlockingDaily()
        }
    }

    /**
     * 根据日记当前解锁状态和日记密码是否存在，更新 FloatingToolbar 上的解锁按钮显示
     */
    private fun updateUnlockButtonVisibility() {
        val id = dailyEntity.id
        val hasPassword = originalSignalPasswordMap[id].isNullOrEmpty()
        val isUnlocked = unlockedIdMap.contains(id)

        if (hasPassword || isUnlocked) {
            hideUnlockButton()
        } else {
            showUnlockButton()
        }
    }

    /**
     * 隐藏 FloatingToolbar 上的解锁按钮
     */
    private fun hideUnlockButton() {
        val btnUnlock = lookDailyBinding.floatingToolbarButtonUnlock
        val parent = btnUnlock.parent as? ViewGroup ?: return

        // 避免重复 remove
        if (btnUnlock.parent != null) {
            parent.removeView(btnUnlock)
        }
    }

    /**
     * 显示 FloatingToolbar 上的解锁按钮
     */
    private fun showUnlockButton() {
        val btnUnlock = lookDailyBinding.floatingToolbarButtonUnlock
        val parent = btnUnlock.parent as? ViewGroup

        // 避免重复 add
        if (parent == null) {
            val toolbarParent = lookDailyBinding.floatingToolbarChild
            toolbarParent.addView(btnUnlock)
        }
    }

    /**
     * 初始化偏好
     */
    private fun initSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private var sharedPreferences: SharedPreferences? = null
}

private const val VIEW_PAGER_INDEX = "viewPagerIndex"