package com.liuxing.daily.ui.main

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.search.SearchView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailySearchAdapter
import com.liuxing.daily.adapter.SelectDailyLabelAdapter
import com.liuxing.daily.databinding.ActivityMainBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.entity.DailyWithMedia
import com.liuxing.daily.extension.rotateOnce
import com.liuxing.daily.listener.DailyLikeFragment
import com.liuxing.daily.listener.OnEnabledChangedListener
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.markdown.color.MarkdownColor
import com.liuxing.daily.ui.add.AddDailyActivity
import com.liuxing.daily.ui.appearance.AppearanceConst
import com.liuxing.daily.ui.compose.theme.DailyTheme
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.config.SystemBarController
import com.liuxing.daily.ui.daily.DailyFragment
import com.liuxing.daily.ui.label.DailyLabelActivity
import com.liuxing.daily.ui.lock.UnlockActivity
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.ui.main.MainActivity.Companion.isCalendarQueryDailyFragment
import com.liuxing.daily.ui.main.MainActivity.Companion.isDailyFragment
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.ui.recyclerbin.RecyclerBinFragment
import com.liuxing.daily.ui.search.SearchScreen
import com.liuxing.daily.ui.search.SearchViewModel
import com.liuxing.daily.ui.settings.SettingsActivity
import com.liuxing.daily.util.BitmapUtil
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.DateUtil.getCurrentDateTime
import com.liuxing.daily.util.DateUtil.getDateString
import com.liuxing.daily.util.DateUtil.getWeek
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.StatusBarUtil
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.util.VersionUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import com.liuxing.daily.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds


class MainActivity : QRXActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailySearchAdapter: DailySearchAdapter

    // 搜索 ViewModel
    private val searchViewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }
    private var dailyList: List<DailyEntity> = ArrayList()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private var sharedPreferences: SharedPreferences? = null
    private var isUpdating = false
    private lateinit var headerView: View
    private lateinit var tvDailyCount: MaterialTextView
    private lateinit var tvDailyTextCount: MaterialTextView
    private lateinit var tvDailyImageCount: MaterialTextView
    private lateinit var navigationViewMenu: Menu
    private lateinit var gLabel: SubMenu
    private lateinit var createLabel: MenuItem
    private var dailyLabelList: List<DailyLabelEntity> = ArrayList()
    private var currentThemeColorId: Int = 0

    /**
     * 当前动态取色开关值，默认为 false.
     *
     * 在 [onCreate] 获取存储的值。
     *
     * 当执行 [onRestart] 时 配合 [currentThemeColorId] 来决定是否重新应用主题，并使用 [recreate] 重建 Activity。
     */
    private var currentDynamicColorChecked = false
    private var dialog: AlertDialog? = null
    private lateinit var navHostFragment: NavHostFragment

    /**
     * 是否正在导入数据
     *
     * 在导入日记时为true，阻止 [checkContentNotInDatabase] 执行导致的媒体文件未正确处理的问题
     */
    private var isImporting = false

    private lateinit var mainViewModel: MainViewModel
    private lateinit var onEnabledChangedListener: OnEnabledChangedListener
    private lateinit var bitmap: Bitmap
    private var wallpaperFileMD5 = ""
    private val termsAndPrivacyAgreedKey = ConstUtil.TERMS_AND_PRIVACY_AGREED_KEY
    private val bottomSheetDialog by lazy {
        BottomSheetDialog(this)
    }

    /**
     * 已选中的日记项列表。
     *
     * 选择说明：
     * - 在应用首次，或从多选模式退出时，应调用 [selectAllDailies] 将所有日记加入列表。
     * - 这样可以确保在未进行选择操作时，“导出全部”能够正常执行。
     *
     * 注意：
     * - 当多选模式开启时，此列表仅反映用户实际选择的日记
     * - 当多选模式关闭时，此列表应始终选中所有日记。
     */
    private var selectedDailyList: List<DailyEntity> = ArrayList()

    /**
     * 当已选中的日记列表发生变化时触发的回调。
     */
    private var onSelectedDailyListListener: SelectedDailyListListener? = null

    /**
     * 当前导出格式。
     */
    private var exportFormat = ExportFormat.JSON

    /**
     * 日记数据导出格式。
     *
     * - JSON JSON格式。
     * - TXT 纯文本格式。
     */
    private enum class ExportFormat {
        JSON, TXT
    }

    /**
     * 搜索关键词
     */
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contextual_toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator_layout)) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, 0, navigationBars.right, navigationBars.bottom)
            insets
        }
        currentThemeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        currentDynamicColorChecked =
            SharedPreferencesUtil.getBoolean(this, AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY, false)
        DailyThemeManager.syncFromPreferences(this)
        MarkdownColor.init(this)
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(ConstUtil.CHECK_APP_VERSION_URL).build()
        val handler = Handler(Looper.getMainLooper())

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
/*                handler.post {
                    e.printStackTrace()
                    CheckAppUpdateUtil.checkFailedOrNoVersionDialog(
                        this@MainActivity,
                        getString(R.string.failed_to_check_for_updates)
                    )
                }*/
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonString = response.body?.string()
                    val jsonObject = JSONObject(jsonString!!)
                    val latestVersionCode = jsonObject.getInt("versionCode")
                    val currentVersionCode = VersionUtil.getVersionCode(this@MainActivity)

                    if (latestVersionCode > currentVersionCode) {
                        handler.post {
                            CheckAppUpdateUtil.checkUpdate(this@MainActivity)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    /*handler.post {
                        CheckAppUpdateUtil.checkFailedOrNoVersionDialog(
                            this@MainActivity,
                            getString(R.string.failed_to_check_for_updates)
                        )
                    }*/
                }
            }
        })
        // 添加返回键回调
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
        insertAutoDaily()
        initView()
        initData()
        val agreed = sharedPreferences?.getBoolean(termsAndPrivacyAgreedKey, false) ?: false
        if (!agreed) {
            showPolicyDialog()
        }
        val appPassword = sharedPreferences?.getString("app_password", "")
        val lock = intent.getBooleanExtra("lock", true)
        if (!appPassword.isNullOrEmpty() && lock) {
            IntentUtil.startActivity(this, UnlockActivity::class.java)
            finish()
        }
    }

    /**
     * 插入自动保存的日记
     */
    private fun insertAutoDaily() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val autoSave = sharedPreferences.getBoolean("switch_preference_auto_save", true)
        if (autoSave) {
            val autoSaveId = sharedPreferences.getLong("switch_preference_auto_save_id", 0)
            val autoSaveTitle = sharedPreferences.getString("switch_preference_auto_save_title", "")
            val autoSaveContent =
                sharedPreferences.getString("switch_preference_auto_save_content", "")
            val autoSaveImageIsNotNull = sharedPreferences.getBoolean(
                "switch_preference_auto_save_image_list_not_null",
                false
            )
            val autoSaveVideoIsNotNull = sharedPreferences.getBoolean(
                "switch_preference_auto_save_video_list_not_null",
                false
            )
            val autoSaveAudioIsNotNull = sharedPreferences.getBoolean(
                "switch_preference_auto_save_audio_list_not_null",
                false
            )
            if (autoSaveTitle!!.isNotEmpty() || autoSaveContent!!.isNotEmpty() || autoSaveImageIsNotNull || autoSaveVideoIsNotNull || autoSaveAudioIsNotNull) {
                val insertUpdate =
                    sharedPreferences.getInt("switch_preference_auto_save_is_insert_or_update", 0)
                val autoSaveDateTime =
                    sharedPreferences.getLong("switch_preference_auto_save_date_time", 0)
                val autoSaveBackgroundColorIndex =
                    sharedPreferences.getInt(
                        "switch_preference_auto_save_background_color_index",
                        0
                    )
                val autoSaveSinglePassword =
                    sharedPreferences.getString("switch_preference_auto_save_single_password", "")
                val autoSaveMoodIndex =
                    sharedPreferences.getInt("switch_preference_auto_save_mood_index", 0)
                val autoSaveWeatherIndex =
                    sharedPreferences.getInt("switch_preference_auto_save_weather_index", 0)
                val autoSaveDailyUuid =
                    sharedPreferences.getString("switch_preference_auto_save_daily_uuid", "")
                val autoSaveDailyLabel =
                    sharedPreferences.getString("switch_preference_auto_save_daily_label", "")
                val autoSaveIsPinned =
                    sharedPreferences.getBoolean("switch_preference_auto_save_is_pinned", false)
                initViewModel()
                when (insertUpdate) {
                    1 -> dailyViewModel.updateDaily(
                        DailyEntity(
                            id = autoSaveId,
                            title = autoSaveTitle,
                            content = autoSaveContent,
                            dateTime = autoSaveDateTime,
                            backgroundColorIndex = autoSaveBackgroundColorIndex,
                            singlePassword = autoSaveSinglePassword,
                            moodIndex = autoSaveMoodIndex,
                            weatherIndex = autoSaveWeatherIndex,
                            dailyUUID = autoSaveDailyUuid,
                            dailyLabel = autoSaveDailyLabel,
                            isPinned = autoSaveIsPinned
                        )
                    )


                    else -> {
                        dailyViewModel.insertDaily(
                            DailyEntity(
                                title = autoSaveTitle,
                                content = autoSaveContent,
                                dateTime = autoSaveDateTime,
                                backgroundColorIndex = autoSaveBackgroundColorIndex,
                                singlePassword = autoSaveSinglePassword,
                                moodIndex = autoSaveMoodIndex,
                                weatherIndex = autoSaveWeatherIndex,
                                dailyUUID = autoSaveDailyUuid,
                                dailyLabel = autoSaveDailyLabel
                            )
                        )
                    }
                }

                // 将自动保存的数据清空
                SharedPreferencesUtil.autoSaveDailySharedPreferences(
                    this, 1, "", "", 0, 0, "", 0, 0, "", false, "", false, false
                )
            }
        }
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initNavController()
        setNavigation()
        initViewModel()
        initSharePreferences()
        initSearchView()
        initSearchBar()
        floatingOnClick()
        onDestinationChanged()
        setDailyData()
        getDailyLabel()
        checkContentNotInDatabase()
        setUpContextualToolbar()
        refreshSelectedDailyList()

        lifecycleScope.launch {
            delay(300.milliseconds)
            selectAllDailies()
        }
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(activityMainBinding.searchBar)
        activityMainBinding.searchView.setupWithSearchBar(activityMainBinding.searchBar)
        appBarConfiguration =
            AppBarConfiguration.Builder(
                R.id.dailyFragment,
                R.id.calendarQueryDailyFragment,
                R.id.galleryFragment,
                R.id.recyclerBinFragment,
                R.id.onThisDayFragment
            )
                .setOpenableLayout(activityMainBinding.main).build()
    }

    /**
     * 初始化导航控制器
     */
    private fun initNavController() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
    }

    /**
     * 设置导航
     */
    private fun setNavigation() {
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(activityMainBinding.navigationView, navController)
    }

    /**
     * 是否打开搜索视图
     *
     * @return 搜索视图的开关值
     */
    private fun isOpenSearchView(): Boolean {
        return activityMainBinding.searchView.isShowing
    }

    /**
     * 监听返回键
     */
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
                if (currentFragment is DailyLikeFragment && currentFragment.getSelectMode()) {
                    hideContextualToolbar()
                    return
                }
                when {
                    isOpenSearchView() -> activityMainBinding.searchView.hide()
                    activityMainBinding.main.isOpen -> activityMainBinding.main.close()

                    isDailyFragment -> finish()
                    !isDailyFragment -> navController.navigate(R.id.dailyFragment)

                    else -> finish()
                }
            }
        }

    fun enableOnBack(enable: Boolean) {
        onBackPressedCallback.isEnabled = enable
    }

    private fun setOnEnableOnBackListener(onEnabledChangedListener: OnEnabledChangedListener) {
        this.onEnabledChangedListener = onEnabledChangedListener
    }

    /**
     * 浮动按钮点击事件
     */
    private fun floatingOnClick() {
        activityMainBinding.floatingActionButton.setOnClickListener {
            IntentUtil.startActivity(
                this,
                AddDailyActivity::class.java,
                mapOf(
                    "isDailyFragment" to isDailyFragment,
                    "selectedYearMonthDay" to mainViewModel.selectedYearMonthDay.value
                )
            )
        }

        activityMainBinding.floatingActionButton.setOnLongClickListener {
            it.rotateOnce()
            true
        }
    }

    /**
     * 初始化搜索视图
     */
    private fun initSearchView() {
        activityMainBinding.searchView.inflateMenu(R.menu.menu_search_daily)

        activityMainBinding.composeSearchView.setContent {
            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onResultClick = { clickedDaily ->
                        val filteredList = dailyList.filter { !it.isDeleted }
                        val position =
                            filteredList.indexOfFirst { it.dailyUUID == clickedDaily.dailyUUID }

                        if (position != -1) {
                            val intent =
                                Intent(this@MainActivity, LookDailyActivity::class.java).apply {
                                    putExtra("POSITION", position)
                                    putExtra("search_query", searchViewModel.query)
                                }
                            startActivity(intent)
                        }
                    },
                    onResultLongClick = { daily ->
                        showDailyManagementDialog(daily)
                    }
                )
            }
        }

        activityMainBinding.searchView.addTransitionListener { searchView, previousState, newState ->
            val enable =
                SearchView.TransitionState.SHOWN == newState || SearchView.TransitionState.SHOWING == newState
            onEnabledChangedListener?.onEnableChanged(enable)
            mainViewModel.setEnableAppBarOffsetChange(!enable)
            if (enable) {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                val typedValue = TypedValue()
                theme.resolveAttribute(
                    com.google.android.material.R.attr.colorSurfaceContainerHigh, typedValue, true
                )
                val color = typedValue.data
                val isDark = ColorUtils.calculateLuminance(color) < 0.5
                insetsController.isAppearanceLightStatusBars = !isDark
            }
        }
        searchDaily()
    }

    /**
     * 搜索日记
     */
    private fun searchDaily() {
        // 点击键盘搜索事件
        activityMainBinding.searchView.editText.setOnEditorActionListener { v, _, _ ->
            searchViewModel.performSearch(v.text.toString())
            true
        }

        activityMainBinding.searchView.editText.addTextChangedListener {
            val currentText = it.toString()
            searchViewModel.performSearch(currentText)
            searchQuery = currentText
        }

        // 点击搜索视图菜单搜索事件
        activityMainBinding.searchView.setOnMenuItemClickListener { item ->
            when (item!!.itemId) {
                R.id.item_search -> searchViewModel.performSearch(activityMainBinding.searchView.editText.text.toString())
            }
            true
        }
    }

    /**
     * 显示搜索结果或列表项的长按管理对话框
     */
    private fun showDailyManagementDialog(dailyEntity: DailyEntity) {
        val moveInRecyclerBin =
            sharedPreferences?.getBoolean("switch_delete_to_recycler_bin_daily", true) ?: true
        val neutralButtonText =
            if (dailyEntity.isPinned) getString(R.string.cancel_pinned) else getString(R.string.pinned)

        if (moveInRecyclerBin) {
            MaterialAlertDialogBuilder(this).apply {
                setMessage(getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin))
                setPositiveButton(getString(R.string.sure)) { _, _ ->
                    dailyViewModel.updateDaily(
                        dailyEntity.copy(
                            isDeleted = true,
                            dailyRecyclerDateTime = getCurrentDateTime()
                        )
                    )
                }
                setNegativeButton(getString(R.string.cancel), null)
                setNeutralButton(neutralButtonText) { _, _ ->
                    val isPinned = neutralButtonText == getString(R.string.pinned)
                    dailyViewModel.updateDaily(dailyEntity.copy(isPinned = isPinned))
                }
                create()
                show()
            }
        } else {
            MaterialAlertDialogBuilder(this).apply {
                setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                setPositiveButton(getString(R.string.delete)) { _, _ ->
                    lifecycleScope.launch {
                        dailyViewModel.deleteSelected(listOf(dailyEntity))
                    }
                }
                setNegativeButton(getString(R.string.recycler_bin)) { _, _ ->
                    dailyViewModel.updateDaily(
                        dailyEntity.copy(
                            isDeleted = true,
                            dailyRecyclerDateTime = getCurrentDateTime()
                        )
                    )
                }
                setNeutralButton(getString(R.string.cancel), null)
                create()
                show()
            }
        }
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    /**
     * 初始化搜索栏
     */
    private fun initSearchBar() {
        searchBarMenuItemOnClick()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_bar, menu)
        val currentSortIndex = sharedPreferences?.getInt("daily_sort_by", 0)
        when (currentSortIndex) {

            1 -> menu?.findItem(R.id.item_old_to_new)?.isChecked = true

            else -> menu?.findItem(R.id.item_new_to_old)?.isChecked = true
        }
        return true
    }

    /**
     * 排序
     *
     * @param sortByIndex 排序索引 0 -> 正 1 -> 倒
     */
    private fun sortBy(sortByIndex: Int) {
        val currentSortIndex = sharedPreferences?.getInt("daily_sort_by", 0)
        if (currentSortIndex != sortByIndex) {
            sharedPreferences?.edit {
                putInt("daily_sort_by", sortByIndex).apply()
                recreate()
            }
        }
    }

    private var dailyPassword = ""

    /**
     * 搜索栏菜单点击事件
     */
    private fun searchBarMenuItemOnClick() {
        activityMainBinding.searchBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_settings -> IntentUtil.startActivity(
                    this,
                    SettingsActivity::class.java
                )

                R.id.item_import_daily -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = "application/zip"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    importDailyLauncher.launch(intent)
                }

                R.id.item_export_all_daily -> {
                    showExportBottomSheet()
                    // exportDailyBackup()
                    LogUtil.d(message = "selectedDailyList size=${selectedDailyList.size}")
                }

                R.id.item_clear -> {
                    if (isDailyLikeFragment()) {
                        val moveInRecyclerBin = sharedPreferences!!.getBoolean(
                            "switch_delete_to_recycler_bin_daily", true
                        )
                        val dailyLikeFragment = getDailyLikeFragment()
                        val tempList = dailyLikeFragment.getDailyList()
                        val isDeleted = tempList.any { it.isDeleted }
                        if (tempList.isNotEmpty()) {
                            if (isDeleted) {
                                dialog = MaterialAlertDialogUtil.showDialog(
                                    this,
                                    message = getString(R.string.do_you_want_to_delete_or_restore_the_daily),
                                    positiveText = getString(R.string.delete),
                                    onPositive = {
                                        deleteSelected(tempList)
                                    },
                                    negativeText = getString(R.string.restore),
                                    onNegative = {
                                        recyclerSelected(tempList)
                                        isDailyFragment
                                    },
                                    neutralText = getString(R.string.cancel)
                                )
                            } else {
                                if (moveInRecyclerBin) {
                                    dialog = MaterialAlertDialogUtil.showDialog(
                                        this,
                                        message = getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin),
                                        positiveText = getString(R.string.sure),
                                        onPositive = {
                                            recyclerSelected(tempList)
                                        },
                                        negativeText = getString(R.string.delete),
                                        onNegative = {
                                            deleteSelected(tempList)
                                        },
                                        neutralText = getString(R.string.cancel)
                                    )
                                } else {
                                    dialog = MaterialAlertDialogUtil.showDialog(
                                        this,
                                        message = getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently),
                                        positiveText = getString(R.string.sure),
                                        onPositive = {
                                            deleteSelected(tempList)
                                        },
                                        negativeText = getString(R.string.recycler_bin),
                                        onNegative = {
                                            recyclerSelected(tempList)
                                        },
                                        neutralText = getString(R.string.cancel)
                                    )
                                }
                            }
                        }
                    }
                }

                R.id.item_new_to_old -> sortBy(0)

                R.id.item_old_to_new -> sortBy(1)
            }
            true
        }
    }

    /**
     * 导出日记备份
     */
    private fun exportDailyBackup() {
        if (selectedDailyList.isNotEmpty()) {
            val inflate = layoutInflater.inflate(
                R.layout.dialog_input_password_layout, null
            )
            val inputPasswordLayout =
                inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
            val inputPassword = inflate.findViewById<TextInputEditText>(R.id.input_password)
            inputPasswordLayout.hint = getString(R.string.encryption_no_encryption_no_input)
            inputPassword.setText(dailyPassword)
            MaterialAlertDialogBuilder(this@MainActivity).apply {
                setTitle(getString(R.string.locked))
                setView(inflate)
                setPositiveButton(
                    getString(R.string.sure)
                ) { _, _ ->

                    dailyPassword = inputPassword.text.toString()
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        type = "application/zip"
                        val backFileName =
                            if (exportFormat == ExportFormat.JSON) "daily.zip" else "daily_text.zip"
                        putExtra(Intent.EXTRA_TITLE, backFileName)
                    }
                    exportAllDailyLauncher.launch(intent)
                }
                setNeutralButton(getString(R.string.cancel), null).setCancelable(false).create()
                show()
            }
        }
    }

    /**
     * 导入日记启动器
     */
    private val importDailyLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode != Activity.RESULT_OK) return
                    val data = result.data ?: return
                    val uri: Uri = data.data!!

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val fileExtension = contentResolver.getType(uri)?.lowercase()
                            if (fileExtension == "application/zip") {

                                withContext(Dispatchers.Main) {
                                    val materialAlertDialogBuilder =
                                        MaterialAlertDialogBuilder(this@MainActivity)
                                    materialAlertDialogBuilder.setView(
                                        layoutInflater.inflate(
                                            R.layout.loading_indicators_dialog_layout,
                                            null
                                        )
                                    )
                                    materialAlertDialogBuilder.setCancelable(false)
                                    dialog = materialAlertDialogBuilder.create()
                                    dialog!!.show()
                                }
                                importDailyZip(uri)
                            } else {
                                if (dialog != null) dialog!!.dismiss()
                                showFailedDialog()
                            }
                        } catch (e: Exception) {
                            if (dialog != null) dialog!!.dismiss()
                            showFailedDialog()
                        }
                    }
                }
            })

    /**
     * 显示导入失败的对话框
     */
    private suspend fun showFailedDialog() {
        withContext(Dispatchers.Main) {
            MaterialAlertDialogBuilder(this@MainActivity).apply {
                setMessage(getString(R.string.import_failed))
                setPositiveButton(getString(R.string.sure), null)
                create()
                show()
            }
        }
    }

    /**
     * 导入日记
     */
    private suspend fun importDailyZip(uri: Uri) = withContext(Dispatchers.IO) {
        isImporting = true
        try {
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                withContext(Dispatchers.Main) {
                    isImporting = false
                    showFailedDialog()
                }
                return@withContext
            }

            val tempZipFile = File(externalCacheDir, "temp_import.zip")
            inputStream.use { input ->
                FileOutputStream(tempZipFile).use { output ->
                    input.copyTo(output)
                }
            }
            inputStream.close()

            val zipFile = net.lingala.zip4j.ZipFile(tempZipFile)
            val password: String?

            if (zipFile.isEncrypted) {
                // 如果备份文件加密，提示输入密码
                password = withContext(Dispatchers.Main) {
                    suspendCancellableCoroutine { continuation ->
                        val inflate =
                            layoutInflater.inflate(R.layout.dialog_input_password_layout, null)
                        val inputPasswordLayout =
                            inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
                        val inputPassword =
                            inflate.findViewById<TextInputEditText>(R.id.input_password)
                        inputPasswordLayout.hint =
                            getString(R.string.encryption_no_encryption_no_input)
                        inputPassword.setText(dailyPassword)

                        MaterialAlertDialogBuilder(this@MainActivity).apply {
                            setTitle(getString(R.string.locked))
                            setView(inflate)
                            setPositiveButton(getString(R.string.sure)) { _, _ ->
                                continuation.resume(inputPassword.text.toString())
                            }
                            setNeutralButton(getString(R.string.cancel)) { _, _ ->
                                continuation.resume(null) // 取消，返回 null
                            }
                            setCancelable(false)
                        }.create().show()
                    }
                }

                // 密码为空
                if (password.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        dialog?.dismiss()
                        MaterialAlertDialogBuilder(this@MainActivity).apply {
                            setMessage(getString(R.string.require_password_to_import))
                            setPositiveButton(getString(R.string.sure), null)
                            show()
                        }
                    }
                    tempZipFile.delete()
                    return@withContext
                }

                zipFile.close()
                val encryptedZipFile =
                    net.lingala.zip4j.ZipFile(tempZipFile, password.toCharArray())
                processZipFile(encryptedZipFile)
            } else {
                processZipFile(zipFile)
            }

            tempZipFile.delete()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                File(externalCacheDir, "temp_import.zip")?.delete()
                isImporting = false
                showFailedDialog()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                File(externalCacheDir, "temp_import.zip")?.delete()
                isImporting = false
                showFailedDialog()
            }
        }
    }

    /**
     * 处理 daily.zip
     *
     * @param zipFile net.lingala.zip4j.ZipFile
     */
    private suspend fun processZipFile(zipFile: net.lingala.zip4j.ZipFile) {
        val diaryWithMediaList = mutableListOf<DailyWithMedia>()
        val processedFileList = mutableSetOf<String>()

        val imageDir = File(getExternalFilesDir(null), "Pictures").apply { mkdirs() }
        val audioDir = File(getExternalFilesDir(null), "Music").apply { mkdirs() }
        val videoDir = File(getExternalFilesDir(null), "Movies").apply { mkdirs() }

        zipFile.fileHeaders.forEach { fileHeader ->
            val entryName = fileHeader.fileName
            when {
                entryName.endsWith(".json") -> {
                    if (processedFileList.contains(entryName)) return@forEach
                    val input = zipFile.getInputStream(fileHeader)
                    val jsonString = input.bufferedReader().readText()
                    input.close()
                    val gson = Gson()
                    if (fileHeader.fileName == "Label/labels.json") {
                        // 获取标签数据库，提取 label 字段
                        val queryDailyLabel =
                            dailyViewModel.queryDailyLabelToList().map { it.label }.toSet()
                        // 反序列化
                        val labelList: List<DailyLabelEntity> = gson.fromJson(
                            jsonString,
                            object : TypeToken<List<DailyLabelEntity>>() {}.type
                        )
                        // 过滤掉已经存在的标签，避免重复插入
                        val newLabels = labelList.filter { it.label !in queryDailyLabel }
                        newLabels.forEach {
                            dailyViewModel.insertDailyLabel(it)
                        }
                    } else {
                        val dailyWithMedia = gson.fromJson(jsonString, DailyWithMedia::class.java)
                        diaryWithMediaList.add(dailyWithMedia)
                    }
                    processedFileList.add(entryName)
                }
                entryName.startsWith("Pictures/") -> {
                    val imageFile = File(imageDir, entryName.removePrefix("Pictures/"))
                    zipFile.extractFile(fileHeader, imageDir.absolutePath, imageFile.name)
                    val imagePath = imageFile.absolutePath
                    diaryWithMediaList.forEach { dailyWithImage ->
                        dailyWithImage.imageList?.forEach { dailyImageEntity ->
                            if (dailyImageEntity.imagePath == null) {
                                dailyImageEntity.imagePath = imagePath
                            }
                        }
                    }
                }
                entryName.startsWith("Music/") -> {
                    val audioFile = File(audioDir, entryName.removePrefix("Music/"))
                    zipFile.extractFile(fileHeader, audioDir.absolutePath, audioFile.name)
                    val audioPath = audioFile.absolutePath
                    diaryWithMediaList.forEach { dailyWithMedia ->
                        dailyWithMedia.audioList?.forEach { audioEntity ->
                            if (audioEntity.audioPath == null) {
                                audioEntity.audioPath = audioPath
                            }
                        }
                    }
                }
                entryName.startsWith("Movies/") -> {
                    val videoFile = File(videoDir, entryName.removePrefix("Movies/"))
                    zipFile.extractFile(fileHeader, videoDir.absolutePath, videoFile.name)
                    val videoPath = videoFile.absolutePath
                    diaryWithMediaList.forEach { dailyWithMedia ->
                        dailyWithMedia.videoList?.forEach { videoEntity ->
                            if (videoEntity.videoPath == null) {
                                videoEntity.videoPath = videoPath
                            }
                        }
                    }
                }
            }
        }

        diaryWithMediaList.forEach { dailyWithMedia ->
            val daily = dailyWithMedia.dailyEntity
            if (daily != null) {
                val exists = dailyViewModel.checkDailyExists(daily.dailyUUID.toString())
                if (exists) return@forEach // 存在，跳过
                dailyViewModel.insertDaily(daily)
                dailyWithMedia.imageList?.forEach { dailyImageEntity ->
                    dailyImageEntity?.imagePath?.let {
                        dailyImageEntity.dailyUuid = daily.dailyUUID
                        dailyViewModel.insertDailyImagePath(
                            dailyImageEntity.dailyUuid.toString(),
                            listOf(it)
                        )
                    }
                }
                dailyWithMedia.audioList?.forEach { audioEntity ->
                    audioEntity?.audioPath?.let {
                        audioEntity.dailyUuid = daily.dailyUUID
                        dailyViewModel.insertDailyAudioPath(
                            audioEntity.dailyUuid.toString(),
                            listOf(it)
                        )
                    }
                }
                dailyWithMedia.videoList?.forEach { videoEntity ->
                    videoEntity?.videoPath?.let {
                        videoEntity.dailyUuid = daily.dailyUUID
                        dailyViewModel.insertDailyVideoPath(
                            videoEntity.dailyUuid.toString(),
                            listOf(it)
                        )
                    }
                }
            }
        }

        withContext(Dispatchers.Main) {
            dialog?.dismiss()
            SnackbarUtil.showSnackbarShort(
                activityMainBinding.fragmentContainerView,
                getString(R.string.import_successful)
            )

            delay(3000.milliseconds)
            isImporting = false
        }
    }

    /**
     * 导出所有日记启动器
     */
    private val exportAllDailyLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode != RESULT_OK) return
                val data = result.data ?: return
                val url = data.data!!
                val processedFileList = mutableSetOf<String>()
                val processedImageFileList = mutableSetOf<String>()
                val processedVideoFileList = mutableSetOf<String>()
                val processedAudioFileList = mutableSetOf<String>()
                var dialog: AlertDialog

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        val materialAlertDialogBuilder =
                            MaterialAlertDialogBuilder(this@MainActivity)
                        materialAlertDialogBuilder.setView(
                            layoutInflater.inflate(R.layout.loading_indicators_dialog_layout, null)
                        )
                        materialAlertDialogBuilder.setCancelable(false)
                        dialog = materialAlertDialogBuilder.create()
                        dialog.show()
                    }

                    val dailyWithMediaList = mutableListOf<DailyWithMedia>()
                    selectedDailyList.forEach { dailyEntity ->
                        if (!processedFileList.contains(dailyEntity.dailyUUID)) {
                            processedFileList.add(dailyEntity.dailyUUID.toString())
                            val queryImageList = dailyViewModel.queryDailyImageByUuidToList(dailyEntity.dailyUUID.toString())
                            val queryVideoList = dailyViewModel.queryDailyVideoByUuidToList(dailyEntity.dailyUUID.toString())
                            val queryAudioList = dailyViewModel.queryDailyAudioByUuidToList(dailyEntity.dailyUUID.toString())
                            dailyWithMediaList.add(
                                DailyWithMedia(
                                    dailyEntity,
                                    queryImageList,
                                    queryVideoList,
                                    queryAudioList
                                )
                            )
                        }
                    }

                    val tempZipFile = File(externalCacheDir, "export_daily.zip")
                    val password = dailyPassword

                    val zipFile = if (password.isNotEmpty()) {
                        net.lingala.zip4j.ZipFile(tempZipFile, password.toCharArray())
                    } else {
                        net.lingala.zip4j.ZipFile(tempZipFile)
                    }

                    val baseZipParameters = net.lingala.zip4j.model.ZipParameters().apply {
                        if (password.isNotEmpty()) {
                            isEncryptFiles = true
                            encryptionMethod = net.lingala.zip4j.model.enums.EncryptionMethod.AES
                            aesKeyStrength =
                                net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_256
                        }
                    }

                    val tempDir = File(externalCacheDir, "temp_zip").apply { mkdirs() }
                    val sb = StringBuilder()
                    val currentSortIndex = sharedPreferences?.getInt("daily_sort_by", 0)
                    val sortedList = if (currentSortIndex == 0) {
                        dailyWithMediaList.sortedByDescending {
                            it.dailyEntity.dateTime
                        }
                    } else {
                        dailyWithMediaList.sortedBy {
                            it.dailyEntity.dateTime
                        }
                    }

                    // 遍历每个每日条目并将其添加到 ZIP 文件中
                    sortedList.forEach { dailyWithMedia ->
                        if (exportFormat == ExportFormat.JSON) {
                            // 将日记导出为 JSON 文件
                            val jsonFile =
                                File(tempDir, "${dailyWithMedia.dailyEntity.dailyUUID}.json")
                            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                            jsonFile.writeText(gson.toJson(dailyWithMedia))
                            val jsonParams = net.lingala.zip4j.model.ZipParameters().apply {
                                isEncryptFiles = baseZipParameters.isEncryptFiles
                                encryptionMethod = baseZipParameters.encryptionMethod
                                aesKeyStrength = baseZipParameters.aesKeyStrength
                                fileNameInZip = "${dailyWithMedia.dailyEntity.dailyUUID}.json"
                            }
                            zipFile.addFile(jsonFile, jsonParams)

                            // 将关联图像导出到 ZIP 文件中的 Pictures 目录
                            dailyWithMedia.imageList?.forEach { dailyImageEntity ->
                                dailyImageEntity?.imagePath?.let { imagePath ->
                                    val imageFile = File(imagePath)
                                    if (imageFile.exists() && !processedImageFileList.contains(
                                            imageFile.name
                                        )
                                    ) {
                                        processedImageFileList.add(imageFile.name)
                                        val imageParams =
                                            net.lingala.zip4j.model.ZipParameters().apply {
                                                isEncryptFiles = baseZipParameters.isEncryptFiles
                                                encryptionMethod =
                                                    baseZipParameters.encryptionMethod
                                                aesKeyStrength = baseZipParameters.aesKeyStrength
                                                fileNameInZip = "Pictures/${imageFile.name}"
                                            }
                                        zipFile.addFile(imageFile, imageParams)
                                    }
                                }
                            }

                            // 将关联视频导出到 ZIP 文件中的 Movies 目录
                            dailyWithMedia.videoList?.forEach { dailyVideoEntity ->
                                dailyVideoEntity?.videoPath?.let { videoPath ->
                                    val videoFile = File(videoPath)
                                    if (videoFile.exists() && !processedVideoFileList.contains(
                                            videoFile.name
                                        )
                                    ) {
                                        processedVideoFileList.add(videoFile.name)
                                        val videoParams =
                                            net.lingala.zip4j.model.ZipParameters().apply {
                                                isEncryptFiles = baseZipParameters.isEncryptFiles
                                                encryptionMethod =
                                                    baseZipParameters.encryptionMethod
                                                aesKeyStrength = baseZipParameters.aesKeyStrength
                                                fileNameInZip = "Movies/${videoFile.name}"
                                            }
                                        zipFile.addFile(videoFile, videoParams)
                                    }
                                }
                            }

                            // 将关联音频导出到 ZIP 文件中的 Music 目录
                            dailyWithMedia.audioList?.forEach { dailyAudioEntity ->
                                dailyAudioEntity?.audioPath?.let { audioPath ->
                                    val audioFile = File(audioPath)
                                    if (audioFile.exists() && !processedAudioFileList.contains(
                                            audioFile.name
                                        )
                                    ) {
                                        processedAudioFileList.add(audioFile.name)
                                        val audioParams =
                                            net.lingala.zip4j.model.ZipParameters().apply {
                                                isEncryptFiles = baseZipParameters.isEncryptFiles
                                                encryptionMethod =
                                                    baseZipParameters.encryptionMethod
                                                aesKeyStrength = baseZipParameters.aesKeyStrength
                                                fileNameInZip = "Music/${audioFile.name}"
                                            }
                                        zipFile.addFile(audioFile, audioParams)
                                    }
                                }
                            }
                        } else {
                            val dailyEntity = dailyWithMedia.dailyEntity
                            val dateTime =
                                dailyEntity.dateTime ?: getCurrentDateTime()
                            val dateString = getDateString(3, Date(dateTime))
                            val txtFile = File(tempDir, "daily.txt")
                            val title = dailyEntity.title ?: ""
                            val moodIndex = dailyEntity.moodIndex ?: 0
                            val moodString =
                                if (moodIndex <= 0) "" else getString(ConstUtil.moodLabelList[moodIndex - 1])
                            val weatherIndex = dailyEntity.weatherIndex ?: 0
                            val weatherString =
                                if (weatherIndex <= 0) "" else getString(ConstUtil.weatherLabelList[weatherIndex - 1])

                            val parts =
                                listOf(dateString, getWeek(this@MainActivity, dateString, 3))
                            val moodPart = moodString.takeIf { it.isNotBlank() }
                            val weatherPart = weatherString.takeIf { it.isNotBlank() }

                            val txtContent = buildString {
                                if (title != "") appendLine(title)
                                appendLine(
                                    (parts + listOfNotNull(
                                        moodPart,
                                        weatherPart
                                    )).joinToString(" ")
                                )
                                appendLine()
                                appendLine(TextUtil.replaceTag(dailyEntity.content ?: "", ""))
                                appendLine()
                                appendLine("========================================")
                            }

                            sb.appendLine(txtContent)

                            txtFile.writeText(sb.toString())
                            val txtParams = net.lingala.zip4j.model.ZipParameters().apply {
                                isEncryptFiles = baseZipParameters.isEncryptFiles
                                encryptionMethod = baseZipParameters.encryptionMethod
                                aesKeyStrength = baseZipParameters.aesKeyStrength
                                fileNameInZip = "Text/daily.txt"
                            }

                            zipFile.addFile(txtFile, txtParams)
                        }
                    }

                    if (exportFormat == ExportFormat.JSON) {
                        // 处理标签
                        val queryLabelList = dailyViewModel.queryDailyLabelToList()
                        val labelFile = File(tempDir, "labels.json")
                        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                        labelFile.writeText(gson.toJson(queryLabelList))
                        val labelParams = net.lingala.zip4j.model.ZipParameters().apply {
                            isEncryptFiles = baseZipParameters.isEncryptFiles
                            encryptionMethod = baseZipParameters.encryptionMethod
                            aesKeyStrength = baseZipParameters.aesKeyStrength
                            fileNameInZip = "Label/labels.json"
                        }
                        zipFile.addFile(labelFile, labelParams)
                    }

                    contentResolver.openOutputStream(url)?.use { outputStream ->
                        tempZipFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    tempZipFile.delete()
                    tempDir.deleteRecursively()

                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        bottomSheetDialog.dismiss()
                        hideContextualToolbar()
                        SnackbarUtil.showSnackbarShort(
                            activityMainBinding.fragmentContainerView,
                            getString(R.string.export_success)
                        )
                    }
                }
            }
        }
    )

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }

    /**
     * Fragment切换时
     */
    private fun onDestinationChanged() {
        navController.addOnDestinationChangedListener { _, fragment, _ ->
            when (fragment.id) {
                R.id.dailyFragment -> {
                    isDailyFragment = true
                    isRecyclerBinFragment = false
                    isCalendarQueryDailyFragment = false
                    showAddDailyButton()
                    onEnabledChangedListener.onEnableChanged(false)
                }

                R.id.calendarQueryDailyFragment -> {
                    isDailyFragment = false
                    isRecyclerBinFragment = false
                    isCalendarQueryDailyFragment = true
                    showAddDailyButton()
                    onEnabledChangedListener.onEnableChanged(true)
                }

                R.id.recyclerBinFragment -> {
                    isDailyFragment = false
                    isRecyclerBinFragment = true
                    isCalendarQueryDailyFragment = false
                    onEnabledChangedListener.onEnableChanged(true)
                    hideAddDailyButton()
                }

                else -> {
                    isDailyFragment = false
                    isRecyclerBinFragment = false
                    isCalendarQueryDailyFragment = false
                    hideAddDailyButton()
                }
            }

            hideContextualToolbar()
        }
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        headerView = activityMainBinding.navigationView.getHeaderView(0)
        navigationViewMenu = activityMainBinding.navigationView.menu
        navigationViewMenu.findItem(R.id.item_menu_label).subMenu?.let {
            gLabel = it
        }
        createLabelItem()
        tvDailyCount = headerView.findViewById(R.id.tv_daily_count)
        tvDailyTextCount = headerView.findViewById(R.id.tv_daily_text_count)
        tvDailyImageCount = headerView.findViewById(R.id.tv_daily_image_count)

        setOnEnableOnBackListener(object : OnEnabledChangedListener {
            override fun onEnableChanged(enable: Boolean) {
                onBackPressedCallback.isEnabled = enable
            }

        })

        activityMainBinding.main.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                onEnabledChangedListener?.onEnableChanged(true)
            }

            override fun onDrawerClosed(drawerView: View) {
                onEnabledChangedListener?.onEnableChanged(false)
            }

            override fun onDrawerStateChanged(newState: Int) {
            }

        })

/*        activityMainBinding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val offsetChange = mainViewModel.enableAppBarOffsetChange.value ?: true
            if (offsetChange) checkStatusBarColor()
        }*/
    }

    /**
     * 获取壁纸的 Bitmap，并使用 [BitmapUtil.check] 检测是否有效。
     *
     * @return 有效时返回 bitmap，
     *         无效时返回 null。
     */
    fun getBitmap(): Bitmap? {
        BitmapUtil.check { bitmap }.let {
            return if (it) {
                bitmap
            } else {
                null
            }
        }
    }

    /**
     * 显示隐私政策的对话框。
     */
    private fun showPolicyDialog() {
        val webLayout = layoutInflater.inflate(R.layout.web_view_layout, null)
        val webView = webLayout.findViewById<WebView>(R.id.web_view)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                return if (url.startsWith("mailto:")) {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO)
                        intent.data = url.toUri()
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        CopyUtil.copyTextToClipboard(this@MainActivity, url.split(":")[1])
                    }
                    true
                } else {
                    true
                }
            }
        }

        Thread {
            try {
                val url =
                    URL("https://gitee.com/LiuXing0327/app-privacy/raw/master/daily/terms_and_privacy.html")
                val htmlContent = url.readText()

                runOnUiThread {
                    webView.loadDataWithBaseURL(
                        "https://gitee.com/", htmlContent, "text/html", "UTF-8", null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog).apply {
            setView(webLayout)
            setPositiveButton("已阅读并同意") { _, _ ->
                sharedPreferences?.edit {
                    putBoolean(termsAndPrivacyAgreedKey, true)
                    apply()
                }
            }
            setNeutralButton("不同意并退出") { _, _ ->
                finish()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    /**
     * 创建标签项
     */
    private fun createLabelItem() {
        createLabel = gLabel.add(
            Menu.NONE, R.id.create_label_id, Menu.NONE,
            getString(R.string.create_label)
        )
            ?.setIcon(R.drawable.baseline_add_24)!!
        createLabel.setOnMenuItemClickListener {
            activityMainBinding.main.close()
            showLabelInputDialog()
            true
        }
    }

    /**
     * 显示输入标签的对话框
     */
    private fun showLabelInputDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input_label_layout, null)
        val inputLabel = view.findViewById<TextInputEditText>(R.id.input_label)
        val inputLabelLayout = view.findViewById<TextInputLayout>(R.id.input_label_layout)
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.create_label))
            setView(view)
            setPositiveButton(getString(R.string.sure)) { dialog, which ->
                val label = inputLabel.text.toString()
                if (label.isNotEmpty()) {
                    addDailyLabel(label)
                }
            }
            setNegativeButton(getString(R.string.cancel), null)
            val dialog = create()
            dialog.show()
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
            inputLabel.addTextChangedListener {
                val inputText = it.toString()
                val input = dailyLabelList.any { dailyLabelEntity ->
                    dailyLabelEntity.label == inputText
                }
                inputLabelLayout.error = when {
                    inputText.isEmpty() -> getString(R.string.the_label_is_empty)
                    input -> getString(R.string.the_label_already_exists)
                    else -> null
                }
                positiveButton.isEnabled = inputText.isNotEmpty() && !input
            }
        }
    }

    /**
     * 获取日记标签
     */
    private fun getDailyLabel() {
        dailyViewModel.queryAllDailyLabel().observe(this) { dailyLabelList ->
            this.dailyLabelList = dailyLabelList
            val sortedBy = dailyLabelList.sortedBy { it.label?.lowercase() }
            gLabel.clear()
            sortedBy.forEach { dailyLabelEntity ->
                gLabel.add(dailyLabelEntity.label).setIcon(R.drawable.outline_label_24)
                    .setOnMenuItemClickListener { _ ->
                        val intent = Intent(this, DailyLabelActivity::class.java).apply {
                            putExtra("daily_label_id", dailyLabelEntity.id)
                            putExtra("daily_label_label", dailyLabelEntity.label)

                        }
                        startActivity(intent)
                        true
                    }
            }
            createLabel.let {
                gLabel.removeItem(it.itemId)
            }
            createLabelItem()
        }
    }

    /**
     * 创建日记标签
     *
     * @param label 标签
     */
    private fun addDailyLabel(label: String) {
        dailyViewModel.insertDailyLabel(DailyLabelEntity(label = label))
    }

    /**
     * 设置日记数据
     */
    private fun setDailyData() {
        val fileUtil = FileUtil()
        val autoDeleteDays = sharedPreferences!!.getInt("auto_delete_recycler_bin_daily", 7)

        dailyViewModel.queryImageCount().observe(this) {
            tvDailyImageCount.text = "${it}${getString(R.string.sheet)}"
        }

        dailyViewModel.queryAllDaily().observe(this, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                dailyList = value

                mainViewModel.setDailyList(dailyList)

                // loadSearchDailyData(activityMainBinding.searchView.text.toString())
                searchViewModel.performSearch(activityMainBinding.searchView.text.toString())
                var dailyTextSize = 0
                if (isUpdating) return
                val tempDailyList = dailyList
                tempDailyList.forEach { dailyEntity ->
                    dailyTextSize += TextUtil.getWordCount(dailyEntity.title!!.plus(dailyEntity.content!!))
                    if (dailyEntity.isDeleted) {
                        if (dailyEntity.dailyRecyclerDateTime == null) {
                            val updatedEntity = DailyEntity(
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
                                dailyRecyclerDateTime = DateUtil.getCurrentDateTime(),
                                dailyEntity.isPinned
                            )
                            dailyViewModel.updateDaily(updatedEntity)
                        } else {
                            val startDate =
                                DateUtil.getDateString(1, Date(dailyEntity.dailyRecyclerDateTime!!))
                            val daysBetween = DateUtil.getDaysBetween(startDate)
                            if (autoDeleteDays != 0) {
                                if (autoDeleteDays - daysBetween <= 0) {
                                    dailyViewModel.queryDailyImageByUuid(dailyEntity.dailyUUID!!)
                                        .observe(this@MainActivity) { dailyImageList ->
                                            val existingImagePaths =
                                                dailyImageList.map { it.imagePath }
                                            if (existingImagePaths.isNotEmpty()) {
                                                val toList = existingImagePaths.toList()
                                                toList.forEach {
                                                    if (fileUtil.checkFileExists(it!!)) {
                                                        fileUtil.deleteFile(it)
                                                    }
                                                }
                                            }
                                            dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                        }
                                    dailyViewModel.queryDailyVideoByUuid(dailyEntity.dailyUUID!!)
                                        .observe(this@MainActivity) { dailyVideoList ->
                                            val existingVideoPaths =
                                                dailyVideoList.map { it.videoPath }
                                            if (existingVideoPaths.isNotEmpty()) {
                                                val toList = existingVideoPaths.toList()
                                                toList.forEach {
                                                    if (fileUtil.checkFileExists(it!!)) {
                                                        fileUtil.deleteFile(it)
                                                    }
                                                }
                                            }
                                            dailyViewModel.deletePathVideoByDailyUuid(dailyEntity.dailyUUID.toString())
                                        }
                                    dailyViewModel.queryDailyAudioByUuid(dailyEntity.dailyUUID!!)
                                        .observe(this@MainActivity) { dailyAudioList ->
                                            val existingAudioPaths =
                                                dailyAudioList.map { it.audioPath }
                                            if (existingAudioPaths.isNotEmpty()) {
                                                val toList = existingAudioPaths.toList()
                                                toList.forEach {
                                                    if (fileUtil.checkFileExists(it!!)) {
                                                        fileUtil.deleteFile(it)
                                                    }
                                                }
                                            }
                                            dailyViewModel.deletePathAudioByDailyUuid(dailyEntity.dailyUUID.toString())
                                        }
                                    dailyViewModel.deleteDaily(dailyEntity)
                                }
                            }
                        }
                    }
                    // 如果dailyUUID是空的
                    if (dailyEntity.dailyUUID.isNullOrEmpty()) {
                        isUpdating = true // 开始更新
                        val updatedEntity = DailyEntity(
                            dailyEntity.id,
                            dailyEntity.title,
                            dailyEntity.content,
                            dailyEntity.dateTime,
                            dailyEntity.backgroundColorIndex,
                            dailyEntity.singlePassword,
                            dailyEntity.moodIndex,
                            dailyEntity.weatherIndex,
                            UUID.randomUUID().toString(),
                            false,
                            dailyEntity.dailyLabel,
                            isPinned = dailyEntity.isPinned

                        )
                        dailyViewModel.updateDaily(updatedEntity)
                        isUpdating = false // 更新完成
                    }
                }
                tvDailyCount.text = "${dailyList.size}${getString(R.string.entries)}"
                tvDailyTextCount.text = "${dailyTextSize}${getString(R.string.word)}"
            }
        })
    }

    override fun onResume() {
        super.onResume()
        (this as QRXActivity).init(
            activityMainBinding.wallpaper,
            activityMainBinding.appBarLayout
        )

        refreshDailies()
    }

    /**
     * 刷新所有日记
     */
    private fun refreshDailies() {
        if (!isDailyLikeFragment()) {
            // 不是 DailyLikeFragment，不执行任何操作。
            return
        }

        val dailyLikeFragment = getDailyLikeFragment()
        val selectMode = dailyLikeFragment.getSelectMode()
        if (!selectMode) {
            // 不是多选模式，延迟 300 毫秒后执行 selectAllDailies.
            CoroutineScope(Dispatchers.Main).launch {
                lifecycleScope.launch {
                    delay(300.milliseconds)

                    selectAllDailies()
                }
            }
        }
    }

    /**
     * 设置壁纸和状态栏。
     */
    private fun setWallpaperAndStausBar() {
        if (File(ConstUtil.WALLPAPER_PATH).exists()) {
            bitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
            activityMainBinding.wallpaper.setImageBitmap(bitmap)
            setLightStausBarsFromBitmap(bitmap)

            (this as QRXActivity).init(
                activityMainBinding.wallpaper,
                activityMainBinding.appBarLayout
            )
        }
    }

    /**
     * 根据 Bitmap 的顶部颜色调整状态栏外观
     *
     * @param bitmap 用于分析的壁纸 Bitmap
     */
    fun setLightStausBarsFromBitmap(bitmap: Bitmap) {
        StatusBarUtil.setLightStausBarsFromBitmap(
            bitmap,
            activityMainBinding.wallpaper.alpha,
            window
        )
    }

    companion object {
        /**
         * 是否是 [DailyFragment]，默认是。
         */
        private var isDailyFragment: Boolean = true

        /**
         * 是否是 [RecyclerBinFragment]，默认不是。
         */
        private var isRecyclerBinFragment: Boolean = false

        /**
         * 是否是 [com.liuxing.daily.ui.calendar.CalendarQueryDailyFragment]，默认不是。
         */
        private var isCalendarQueryDailyFragment = false
    }

    /**
     * 初始化偏好
     */
    private fun initSharePreferences() =
        PreferenceManager.getDefaultSharedPreferences(this).also { sharedPreferences = it }

    /**
     * 添加图片启动器
     */
    private val addImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode != Activity.RESULT_OK) return
                val data = result.data ?: return
                val uri = data.data
                val headerView = activityMainBinding.navigationView.getHeaderView(0)
                val ivIcon = headerView.findViewById<ShapeableImageView>(R.id.iv_icon)
                ivIcon.setImageURI(uri)
            }

        })

    /**
     * 检查数据库中不存在的内容,并将它删除
     *
     * [isImporting] 在导入时，跳过检查，避免出现媒体文件未正确处理的问题
     */
    private fun checkContentNotInDatabase() {
        val fileUtil = FileUtil()

        dailyViewModel.queryAllDaily().observe(this) { dailyList ->

            if (isImporting) return@observe

            CoroutineScope(Dispatchers.IO).launch {

                val filePaths =
                    fileUtil.getFilePaths(File("/storage/emulated/0/Android/data/com.liuxing.daily/files"))
                val filesToDelete = mutableListOf<String>()

                for (path in filePaths) {

                    // 排除壁纸
                    if(path == ConstUtil.WALLPAPER_PATH) continue

                    when (File(path).extension) {
                        "jpg" -> {
                            var contentExistsInDatabase = false

                            dailyList.forEach {
                                val dailyImages =
                                    dailyViewModel.queryDailyImageByUuidToList(it.dailyUUID!!)
                                dailyImages.forEach { dailyImageEntity ->
                                    if (dailyImageEntity.imagePath == path) {
                                        contentExistsInDatabase = true
                                        return@forEach
                                    }
                                }
                            }

                            if (!contentExistsInDatabase) {
                                filesToDelete.add(path)
                            }
                        }

                        "mp4" -> {
                            var contentExistsInDatabase = false

                            dailyList.forEach {
                                val dailyVideos =
                                    dailyViewModel.queryDailyVideoByUuidToList(it.dailyUUID!!)

                                dailyVideos.forEach { dailyVideoEntity ->
                                    if (dailyVideoEntity.videoPath == path) {
                                        contentExistsInDatabase = true
                                        return@forEach
                                    }
                                }
                            }

                            if (!contentExistsInDatabase) {
                                filesToDelete.add(path)
                            }
                        }

                        "mp3" -> {
                            var contentExistsInDatabase = false

                            dailyList.forEach {
                                val dailyAudios =
                                    dailyViewModel.queryDailyAudioByUuidToList(it.dailyUUID!!)

                                dailyAudios.forEach { dailyAudioEntity ->
                                    if (dailyAudioEntity.audioPath == path) {
                                        contentExistsInDatabase = true
                                        return@forEach
                                    }
                                }
                            }

                            if (!contentExistsInDatabase) {
                                filesToDelete.add(path)
                            }
                        }
                    }
                }

                filesToDelete.forEach { filepath ->
                    val fileExists = fileUtil.checkFileExists(filepath)
                    if (fileExists) {
                        fileUtil.deleteFile(filepath)
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val themeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        val dynamicColorChecked =
            SharedPreferencesUtil.getBoolean(this, AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY, false)
        if (themeColorId == currentThemeColorId && dynamicColorChecked == currentDynamicColorChecked) return
        ThemeUtil.applyTheme(this)
        recreate()
    }

    /**
     * 展开多选模式下的工具栏
     */
    fun expandContextualToolbar() {
        activityMainBinding.searchBar.expand(
            activityMainBinding.contextualToolbarContainer, activityMainBinding.appBarLayout
        )

        activityMainBinding.floatingActionButton.hide()

        lifecycleScope.launch {
            delay(300.milliseconds)
            disableLightStatusBarWithAppBar()
            val enable = mainViewModel.enableAppBarOffsetChange.value ?: false
            if (!enable) {
                val background = activityMainBinding.contextualToolbarContainer.background
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (background is MaterialShapeDrawable) {
                    val fillColor = background.fillColor
                    val color = fillColor?.defaultColor ?: Color.TRANSPARENT
                    val isDark = ColorUtils.calculateLuminance(color) < 0.5
                    insetsController.isAppearanceLightStatusBars = !isDark
                }
            }
        }
    }

    /**
     * 折叠多选模式下的工具栏
     *
     * @return 工具栏的折叠状态
     */
    fun collapseContextualToolbar(): Boolean = activityMainBinding.searchBar.collapse(
        activityMainBinding.contextualToolbarContainer, activityMainBinding.appBarLayout
    )

    /**
     * 隐藏多选模式下的工具栏
     */
    private fun hideContextualToolbar() {
        if (collapseContextualToolbar()) {
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
            if (currentFragment is DailyLikeFragment) {
                currentFragment.clearSection()
                enableLightStatusBarWithAppBar()
                selectAllDailies()
            }
            showAddDailyButton()
        }
    }

    /**
     * 启用浅色状态栏，并允许 AppBar 偏移
     */
    fun enableLightStatusBarWithAppBar() {
        mainViewModel.setEnableAppBarOffsetChange(true)
        SystemBarController.isLightStatusBarEnabled = true
    }

    /**
     * 禁用浅色状态栏，并禁止 AppBar 偏移
     */
    fun disableLightStatusBarWithAppBar() {
        mainViewModel.setEnableAppBarOffsetChange(false)
        SystemBarController.isLightStatusBarEnabled = false
    }

    /**
     * 设置多选模式下的工具栏
     */
    private fun setUpContextualToolbar() {
        activityMainBinding.contextualToolbar.setNavigationOnClickListener {
            hideContextualToolbar()
            val bitmapValid = BitmapUtil.check { bitmap }
            if (bitmapValid) setLightStausBarsFromBitmap(bitmap)
        }
        activityMainBinding.contextualToolbar.inflateMenu(R.menu.menu_searchbar_contextual_toolbar)

        activityMainBinding.contextualToolbar.setOnMenuItemClickListener { menuItem ->
            val currentFragment = getVisibleFragment()
            if (currentFragment is DailyLikeFragment) {
                val selectedUUIDs = currentFragment.getSelectedItems()
                val dailyList = currentFragment.getDailyList()
                val tempDailyList = dailyList.filter { it.dailyUUID in selectedUUIDs }

                when (menuItem.itemId) {
                    R.id.item_select_all -> currentFragment.selectAllItems()

                    R.id.item_export_daily -> {
                        showExportBottomSheet()
                    }

                    R.id.item_delete -> {
                        val moveInRecyclerBin = sharedPreferences!!
                            .getBoolean("switch_delete_to_recycler_bin_daily", true)

                        val isDeleted = tempDailyList.any { it.isDeleted }
                        if (isDeleted) {
                            dialog = MaterialAlertDialogUtil.showDialog(
                                this,
                                message = getString(R.string.do_you_want_to_delete_or_restore_the_daily),
                                positiveText = getString(R.string.delete),
                                onPositive = {
                                    deleteSelected(tempDailyList)
                                    hideContextualToolbar()
                                },
                                negativeText = getString(R.string.restore),
                                onNegative = {
                                    recyclerSelected(tempDailyList)
                                    hideContextualToolbar()
                                },
                                neutralText = getString(R.string.cancel)
                            )
                        } else {
                            if (moveInRecyclerBin) {
                                dialog = MaterialAlertDialogUtil.showDialog(
                                    this,
                                    message = getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin),
                                    positiveText = getString(R.string.sure),
                                    onPositive = {
                                        recyclerSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    negativeText = getString(R.string.delete),
                                    onNegative = {
                                        deleteSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    neutralText = getString(R.string.cancel)
                                )
                            } else {
                                dialog = MaterialAlertDialogUtil.showDialog(
                                    this,
                                    message = getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently),
                                    positiveText = getString(R.string.sure),
                                    onPositive = {
                                        deleteSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    negativeText = getString(R.string.recycler_bin),
                                    onNegative = {
                                        recyclerSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    neutralText = getString(R.string.cancel)
                                )
                            }
                        }
                    }

                    R.id.item_pinned -> {
                        if (tempDailyList.isNotEmpty()) {
                            val isPinned = tempDailyList.first().isPinned
                            val pinnedButtonText = if (isPinned)
                                getString(R.string.cancel_pinned)
                            else
                                getString(R.string.pinned)

                            dialog = MaterialAlertDialogUtil.showDialog(
                                this,
                                message = getString(
                                    R.string.confirm_pinned_journal_message,
                                    pinnedButtonText
                                ),
                                positiveText = pinnedButtonText,
                                onPositive = {
                                    tempDailyList.forEach { entity ->
                                        dailyViewModel.updateDaily(
                                            entity.copy(isPinned = !isPinned)
                                        )
                                    }
                                    hideContextualToolbar()
                                },
                                negativeText = getString(R.string.cancel)
                            )
                        }
                    }

                    R.id.item_label -> {
                        setSelectedDailiesLabel()
                    }
                }
            }
            true
        }
    }

    /**
     * 回收或还原被选中的日记项
     *
     * @param selectedList 选中的日记列表
     */
    private fun recyclerSelected(selectedList: List<DailyEntity>) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(layoutInflater.inflate(R.layout.loading_indicators_dialog_layout, null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()

        lifecycleScope.launch {
            dailyViewModel.toggleIsDelete(selectedList.map { it.dailyUUID ?: "" })
            dialog?.dismiss()
        }
    }

    /**
     * 删除选中的日记项
     *
     * @param selectedList 选中的日记列表
     */
    private fun deleteSelected(selectedList: List<DailyEntity>) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(layoutInflater.inflate(R.layout.loading_indicators_dialog_layout, null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog?.show()

        lifecycleScope.launch {
            if (isDailyLikeFragment()) {
                val dailyLikeFragment = getDailyLikeFragment()
                if (isDailyFragment) {
                    dailyViewModel.deleteSelected(selectedList)
                    (dailyLikeFragment as DailyFragment).getAdapter()
                        .removeItemsByUuid(selectedList.mapNotNull { it.dailyUUID })
                } else {
                    dailyViewModel.deleteSelected(selectedList)
                    (dailyLikeFragment as RecyclerBinFragment).getAdapter()
                        .removeItemsByUuid(selectedList.mapNotNull { it.dailyUUID })
                }
            }
            dialog?.dismiss()
        }
    }

    /**
     * 设置多选模式下的工具栏标题
     *
     * @param title 工具栏标题
     */
    fun setUpContextualToolbarTitle(title: String) {
        activityMainBinding.contextualToolbar.title = title
    }

    /**
     * 设置多选模式下工具栏中「置顶/取消置顶」按钮的可见性
     *
     * 避免对两个状态的项进行置顶或取消置顶
     */
    fun setUpContextualToolbarPinnedVisibility() {
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
        if (currentFragment is DailyLikeFragment) {
            val selectedUUIDs = currentFragment.getSelectedItems()
            val dailyList = currentFragment.getDailyList()

            val tempDailyList = dailyList.filter { it.dailyUUID in selectedUUIDs }

            val onlyPinned = tempDailyList.all { it.isPinned }
            val onlyUnpinned = tempDailyList.all { !it.isPinned }
            val showPinnedItem =
                if (currentFragment.isPinnedDisplay()) tempDailyList.isNotEmpty() &&
                        (onlyPinned || onlyUnpinned) else false
            val pinned = tempDailyList.first().isPinned

            activityMainBinding.contextualToolbar.menu.findItem(R.id.item_pinned)
                ?.let { itemPinned ->
                    itemPinned.isVisible = showPinnedItem

                    itemPinned.icon = if (pinned) ContextCompat.getDrawable(
                        this, R.drawable.outline_toolbar_push_pin_off_24
                    ) else ContextCompat.getDrawable(this, R.drawable.outline_toolbar_push_pin_24)
                }

            onSelectedDailyListListener?.onSelectedDailyListChanged(tempDailyList)

            val showLabelItem = currentFragment.isLabelDisplay()
            val itemLabel = activityMainBinding.contextualToolbar.menu.findItem(R.id.item_label)
            itemLabel.isVisible = showLabelItem
        }
    }

    /**
     * 判断当前 Fragment 是否是 [DailyLikeFragment]
     *
     * @return true 是;
     *         false 不是
     */
    private fun isDailyLikeFragment(): Boolean {
        val currentFragment = getVisibleFragment()
        return (currentFragment is DailyLikeFragment)
    }

    /**
     * 获取 [DailyLikeFragment]
     *
     * @return DailyFragment
     */
    private fun getDailyLikeFragment(): DailyLikeFragment {
        val currentFragment = getVisibleFragment()
        return (currentFragment as DailyLikeFragment)
    }

    /**
     * 获取当前可见的 Fragment
     *
     * @return 当前显示的 Fragment，如果没有则返回 null 刷新
     */
    private fun getVisibleFragment(): Fragment? {
        return navHostFragment.childFragmentManager.fragments.firstOrNull { it.isVisible }
    }

    /**
     * 刷新已选中的日记列表
     */
    private fun refreshSelectedDailyList() {
        setOnSelectedDailyListListener(object : SelectedDailyListListener {
            override fun onSelectedDailyListChanged(selectedList: List<DailyEntity>) {
                selectedDailyList = selectedList
            }
        })
    }

    /**
     * 选择所有日记
     */
    fun selectAllDailies() {
        onSelectedDailyListListener?.onSelectedDailyListChanged(dailyList)
    }

    /**
     * 用于监听“已选中日记列表”变化的回调接口。
     *
     * 当选中状态发生更新（如添加、移除，或多选模式变化）时，该接口会被触发。
     */
    private interface SelectedDailyListListener {

        /**
         * 当已选中的日记列表发生变化时调用。
         *
         * @param selectedList 更新后的选中日记列表
         */
        fun onSelectedDailyListChanged(selectedList: List<DailyEntity>)
    }

    /**
     * 注册一个用于监听“选中日记列表变化”的监听器。
     *
     * @param listener 要注册的监听器实例。
     */
    private fun setOnSelectedDailyListListener(listener: SelectedDailyListListener) {
        onSelectedDailyListListener = listener
    }

    /**
     * 显示导出的底部弹窗
     */
    private fun showExportBottomSheet() {
        exportFormat = ExportFormat.JSON

        bottomSheetDialog.setContentView(R.layout.bottomsheet_export_content)
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<MaterialButton>(R.id.btn_close)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<MaterialButtonToggleGroup>(R.id.btn_toggle_group)
            ?.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (!isChecked) return@addOnButtonCheckedListener

                exportFormat =
                    if (checkedId == R.id.btn_export_txt) ExportFormat.TXT else ExportFormat.JSON
            }

        bottomSheetDialog.findViewById<MaterialButton>(R.id.btn_export)?.setOnClickListener {
            exportDailyBackup()
        }
    }

    /**
     * 设置选中日记的标签。
     */
    private fun setSelectedDailiesLabel() {
        // 如果不是 DailyFragment ，则不执行任何操作。
        if (!isDailyFragment) {
            return
        }

        showSelectDailyLabelDialog(selectedDailyList)
    }

    /**
     * 显示选择日记标签的对话框。
     *
     * 当 onPositive/onViewCreated 执行时，会调用 [updateDailiesLabel] 更新日记标签。
     *
     * @param selectedItems 已选中的日记项列表。
     */
    private fun showSelectDailyLabelDialog(selectedItems: List<DailyEntity>) {
        // 当前选中的标签
        var currentSelectedLabel = ""

        dialog = MaterialAlertDialogUtil.showDialog(
            this,
            getString(R.string.label),
            layoutRes = R.layout.dialog_select_daily_label_layout,
            positiveText = getString(R.string.not_add),
            onPositive = {
                updateDailiesLabel(selectedItems, currentSelectedLabel)

                hideContextualToolbar()
            },
            negativeText = getString(R.string.new_label),
            onNegative = {
                showLabelInputDialog()
            },
            neutralText = getString(R.string.cancel),
            onViewCreated = { view, _ ->
                val labelListView = view.findViewById<RecyclerView>(R.id.recycler_view)

                labelListView.layoutManager = LinearLayoutManager(this)

                val labelMap = dailyLabelList.mapNotNull {
                    it.label
                }
                val selectDailyLabelAdapter = SelectDailyLabelAdapter(labelMap)
                labelListView.adapter = selectDailyLabelAdapter

                selectDailyLabelAdapter.setOnItemClickListener(object : OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        currentSelectedLabel = labelMap[position]
                        updateDailiesLabel(selectedItems, currentSelectedLabel)

                        dialog?.dismiss()

                        hideContextualToolbar()
                    }
                })
            })
    }

    /**
     * 更新选中日记的标签。
     *
     * @param selectedItems 已选中的日记项列表。
     * @param currentSelectedLabel 当前选中的标签
     */
    private fun updateDailiesLabel(
        selectedItems: List<DailyEntity>, currentSelectedLabel: String
    ) {
        selectedItems.forEach { entity ->
            dailyViewModel.updateDaily(
                entity.copy(
                    dailyLabel = currentSelectedLabel
                )
            )

        }
    }

    /**
     * 显示添加日记的按钮。
     *
     * 仅在当前 Fragment 是 [isDailyFragment] 和 [isCalendarQueryDailyFragment] 时，显示按钮。
     */
    fun showAddDailyButton() {
        if (isDailyFragment || isCalendarQueryDailyFragment) {
            activityMainBinding.floatingActionButton.show()
        }
    }

    /**
     * 隐藏添加日记的按钮。
     */
    fun hideAddDailyButton() = activityMainBinding.floatingActionButton.hide()
}
