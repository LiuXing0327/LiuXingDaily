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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.palette.graphics.Palette
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.liuxing.daily.databinding.ActivityMainBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.entity.DailyWithMedia
import com.liuxing.daily.listener.DailyLikeFragment
import com.liuxing.daily.listener.OnEnabledChangedListener
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.ui.add.AddDailyActivity
import com.liuxing.daily.ui.config.SystemBarController
import com.liuxing.daily.ui.label.DailyLabelActivity
import com.liuxing.daily.ui.lock.UnlockActivity
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.ui.settings.SettingsActivity
import com.liuxing.daily.util.BitmapUtil
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.util.VersionUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import com.liuxing.daily.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailySearchAdapter: DailySearchAdapter
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainerView)) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, 0, navigationBars.right, navigationBars.bottom)
            insets
        }
        currentThemeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
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
        initSearchRecyclerView()
        initSearchView()
        setSearchRecyclerViewData()
        initSearchBar()
        floatingOnClick()
        onDestinationChanged()
        setDailyData()
        getDailyLabel()
        checkContentNotInDatabase()
        setUpContextualToolbar()
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
    }

    /**
     * 初始化搜索列表
     */
    private fun initSearchRecyclerView() {
        val linearLayoutManagerSearch = LinearLayoutManager(this)
        activityMainBinding.searchRecyclerView.layoutManager = linearLayoutManagerSearch
        dailySearchAdapter = DailySearchAdapter()
        activityMainBinding.searchRecyclerView.adapter = dailySearchAdapter
    }

    /**
     * 设置搜索列表数据
     */
    private fun setSearchRecyclerViewData() {
        setSearchRecyclerViewItemOnClick()
        setSearchRecyclerViewItemOnLongClick()
    }

    /**
     * 加载搜索日记的数据
     */
    private fun loadSearchDailyData(searchQuery: String) {
        lifecycleScope.launch {
            val uuids = dailyList.mapNotNull { it.dailyUUID }
            val imageMap = dailyViewModel.getImagePathForUuids(uuids)
            dailySearchAdapter.setDailyList(
                this@MainActivity, dailyList, searchQuery, imageMap
            )
        }
    }

    /**
     * 设置搜索列表点击事件
     */
    private fun setSearchRecyclerViewItemOnClick() {
        dailySearchAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent()
                intent.setClass(this@MainActivity, LookDailyActivity::class.java)
                intent.putExtra("POSITION", position)
                startActivity(intent)
            }

        })
    }

    /**
     * 初始化搜索视图
     */
    private fun initSearchView() {
        activityMainBinding.searchView.inflateMenu(R.menu.menu_search_daily)

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
        activityMainBinding.searchView.editText.setOnEditorActionListener { v, actionId, event ->
            loadSearchDailyData(v.text.toString())
            true
        }

        activityMainBinding.searchView.editText.addTextChangedListener {
            loadSearchDailyData(it.toString())
        }

        // 点击搜索视图菜单搜索事件
        activityMainBinding.searchView.setOnMenuItemClickListener { item ->
            when (item!!.itemId) {
                R.id.item_search -> loadSearchDailyData(activityMainBinding.searchView.text.toString())
            }
            true
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
                        setType("application/zip")
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    importDailyLauncher.launch(intent)
                }

                R.id.item_export_all_daily -> {
                    if (dailyList.isNotEmpty()) {
                        val inflate =
                            layoutInflater.inflate(
                                R.layout.dialog_input_password_layout,
                                null
                            )
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
                            setPositiveButton(
                                getString(R.string.sure)
                            ) { dialog, which ->
                                dailyPassword = inputPassword.text.toString()
                                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                    setType("application/zip")
                                    putExtra(Intent.EXTRA_TITLE, "daily.zip")
                                }
                                exportAllDailyLauncher.launch(intent)
                            }
                            setNeutralButton(getString(R.string.cancel), null)
                                .setCancelable(false)
                                .create()
                            show()
                        }
                    }
                }

                R.id.item_clear -> {
                    if (isRecyclerBinFragment) {
                        val deletedDailyList = dailyList.filter { it.isDeleted }
                        if (deletedDailyList.isNotEmpty())
                            MaterialAlertDialogUtil.showDialog(
                                this,
                                getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently),
                                getString(R.string.sure),
                                {
                                    if (deletedDailyList.isEmpty()) return@showDialog
                                    deletedDailyList.forEach { dailyEntity ->
                                        val fileUtil = FileUtil()
                                        dailyViewModel.queryDailyImageByUuid(dailyEntity.dailyUUID.toString())
                                            .observe(this) { dailyImageList ->
                                                val existingImagePaths =
                                                    dailyImageList.map { it.imagePath }.toSet()
                                                if (existingImagePaths.isNotEmpty()) {
                                                    val list = existingImagePaths.toList()
                                                    list.forEach {
                                                        if (fileUtil.checkFileExists(it!!)) {
                                                            fileUtil.deleteFile(it)
                                                        }
                                                    }
                                                }
                                                dailyViewModel.deletePathImageByDailyUuid(
                                                    dailyEntity.dailyUUID.toString()
                                                )
                                            }
                                        dailyViewModel.queryDailyVideoByUuid(dailyEntity.dailyUUID.toString())
                                            .observe(this) { dailyVideoList ->
                                                val existingVideoPaths =
                                                    dailyVideoList.map { it.videoPath }.toSet()
                                                if (existingVideoPaths.isNotEmpty()) {
                                                    val list = existingVideoPaths.toList()
                                                    list.forEach {
                                                        if (fileUtil.checkFileExists(it!!)) {
                                                            fileUtil.deleteFile(it)
                                                        }
                                                    }
                                                }
                                                dailyViewModel.deletePathVideoByDailyUuid(
                                                    dailyEntity.dailyUUID.toString()
                                                )
                                            }
                                        dailyViewModel.queryDailyAudioByUuid(dailyEntity.dailyUUID.toString())
                                            .observe(this) { dailyAudioList ->
                                                val existingAudioPaths =
                                                    dailyAudioList.map { it.audioPath }.toSet()
                                                if (existingAudioPaths.isNotEmpty()) {
                                                    val list = existingAudioPaths.toList()
                                                    list.forEach {
                                                        if (fileUtil.checkFileExists(it!!)) {
                                                            fileUtil.deleteFile(it)
                                                        }
                                                    }
                                                }
                                                dailyViewModel.deletePathAudioByDailyUuid(
                                                    dailyEntity.dailyUUID.toString()
                                                )
                                            }
                                        dailyViewModel.deleteDaily(dailyEntity)
                                    }
                                }, getString(R.string.restore),
                                {
                                    deletedDailyList.forEach { dailyEntity ->
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
                                                false,
                                                dailyEntity.dailyLabel,
                                                dailyRecyclerDateTime = null,
                                                dailyEntity.isPinned
                                            )
                                        )
                                    }
                                }, getString(R.string.cancel)
                            )
                    } else
                    if (dailyList.isNotEmpty()) {
                        val materialAlertDialogBuilder =
                            MaterialAlertDialogBuilder(this@MainActivity)
                        materialAlertDialogBuilder.setView(
                            layoutInflater.inflate(
                                R.layout.loading_lndicators_dialog_layout,
                                null
                            )
                        )
                        materialAlertDialogBuilder.setCancelable(true)
                        dialog = materialAlertDialogBuilder.create()
                        val moveInRecyclerBin =
                            sharedPreferences!!.getBoolean(
                                "switch_delete_to_recycler_bin_daily",
                                true
                            )
                        if (moveInRecyclerBin) {
                            MaterialAlertDialogBuilder(this).apply {
                                setMessage(getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin))
                                setPositiveButton(getString(R.string.sure)) { _, _ ->
                                    dailyList.forEach { dailyEntity ->
                                        dialog!!.show()
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
                                    dialog?.dismiss()
                                }
                                setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                    dialog?.dismiss()
                                }
                                setOnDismissListener { dialog?.dismiss() }
                                create()
                                show()
                            }

                        } else {
                            MaterialAlertDialogBuilder(this).apply {
                                setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                                setPositiveButton(getString(R.string.delete)) { _, _ ->
                                    dialog!!.show()
                                    dailyList.forEach { dailyEntity ->
                                        dailyEntity.dailyUUID?.let {
                                            dailyViewModel.deletePathImageByDailyUuid(
                                                it
                                            )
                                            dailyViewModel.deletePathVideoByDailyUuid(it)
                                            dailyViewModel.deletePathAudioByDailyUuid(it)
                                        }
                                        dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                        dailyViewModel.deletePathVideoByDailyUuid(dailyEntity.dailyUUID.toString())
                                        dailyViewModel.deletePathAudioByDailyUuid(dailyEntity.dailyUUID.toString())
                                        dailyViewModel.deleteDaily(dailyEntity)
                                    }
                                    dialog?.dismiss()
                                }
                                setNegativeButton(getString(R.string.recycler_bin)) { _, _ ->
                                    dailyList.forEach { dailyEntity ->
                                        dialog!!.show()
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
                                    dialog?.dismiss()
                                }
                                setNeutralButton(getString(R.string.cancel)) { _, _ ->
                                    dialog?.dismiss()
                                }
                                setOnDismissListener { dialog?.dismiss() }
                                create()
                                show()
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
                                            R.layout.loading_lndicators_dialog_layout,
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
                    suspendCoroutine { continuation ->
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

            delay(3000)
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
                            layoutInflater.inflate(R.layout.loading_lndicators_dialog_layout, null)
                        )
                        materialAlertDialogBuilder.setCancelable(false)
                        dialog = materialAlertDialogBuilder.create()
                        dialog.show()
                    }

                    val dailyWithMediaList = mutableListOf<DailyWithMedia>()
                    dailyList.forEach { dailyEntity ->
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

                    // 遍历每个每日条目并将其添加到 ZIP 文件中
                    dailyWithMediaList.forEach { dailyWithMedia ->
                        // 将日记导出为 JSON 文件
                        val jsonFile = File(tempDir, "${dailyWithMedia.dailyEntity.dailyUUID}.json")
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
                                if (imageFile.exists() && !processedImageFileList.contains(imageFile.name)) {
                                    processedImageFileList.add(imageFile.name)
                                    val imageParams =
                                        net.lingala.zip4j.model.ZipParameters().apply {
                                            isEncryptFiles = baseZipParameters.isEncryptFiles
                                            encryptionMethod = baseZipParameters.encryptionMethod
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
                                if (videoFile.exists() && !processedVideoFileList.contains(videoFile.name)) {
                                    processedVideoFileList.add(videoFile.name)
                                    val videoParams =
                                        net.lingala.zip4j.model.ZipParameters().apply {
                                            isEncryptFiles = baseZipParameters.isEncryptFiles
                                            encryptionMethod = baseZipParameters.encryptionMethod
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
                                if (audioFile.exists() && !processedAudioFileList.contains(audioFile.name)) {
                                    processedAudioFileList.add(audioFile.name)
                                    val audioParams =
                                        net.lingala.zip4j.model.ZipParameters().apply {
                                            isEncryptFiles = baseZipParameters.isEncryptFiles
                                            encryptionMethod = baseZipParameters.encryptionMethod
                                            aesKeyStrength = baseZipParameters.aesKeyStrength
                                            fileNameInZip = "Music/${audioFile.name}"
                                        }
                                    zipFile.addFile(audioFile, audioParams)
                                }
                            }
                        }
                    }

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

                    contentResolver.openOutputStream(url)?.use { outputStream ->
                        tempZipFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    tempZipFile.delete()
                    tempDir.deleteRecursively()

                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
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
            activityMainBinding.floatingActionButton.visibility =
                when (fragment.id) {
                    R.id.dailyFragment -> {
                        isDailyFragment = true
                        isRecyclerBinFragment = false
                        onEnabledChangedListener?.onEnableChanged(false)
                        View.VISIBLE
                    }

                    R.id.calendarQueryDailyFragment -> {
                        isDailyFragment = false
                        isRecyclerBinFragment = false
                        onEnabledChangedListener?.onEnableChanged(true)
                        View.VISIBLE
                    }

                    R.id.recyclerBinFragment -> {
                        isDailyFragment = false
                        isRecyclerBinFragment = true
                        onEnabledChangedListener?.onEnableChanged(true)
                        View.GONE
                    }

                    else -> {
                        isDailyFragment = false
                        isRecyclerBinFragment = false
                        View.GONE
                    }
                }
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

        activityMainBinding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val offsetChange = mainViewModel.enableAppBarOffsetChange.value ?: true
            if (offsetChange) checkStatusBarColor()
        }
    }

    fun checkStatusBarColor() {
        val background = activityMainBinding.appBarLayout.background
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (background is MaterialShapeDrawable) {
            val fillColor = background.fillColor
            val color = fillColor?.defaultColor ?: Color.TRANSPARENT
            if (color == Color.TRANSPARENT) {
                val bitmapValid = BitmapUtil.check({ bitmap })
                if (bitmapValid) {
                    setLightStausBarsFromBitmap(bitmap)
                }
            } else {
                val isDark = ColorUtils.calculateLuminance(color) < 0.5
                insetsController.isAppearanceLightStatusBars = !isDark
            }
        }
    }

    fun getBitmap(): Bitmap? {
        BitmapUtil.check { bitmap }.let {
            return if (it) {
                bitmap
            } else {
                null
            }
        }
    }

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
                loadSearchDailyData(activityMainBinding.searchView.text.toString())
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
        val headerYearMonth = sharedPreferences?.getBoolean(
            "switch_preference_header_display",
            true
        )
        val textFontSize = sharedPreferences!!.getFloat(ConstUtil.TEXT_SIZE_KEY, 16F)
        if (dailySearchAdapter.headerYearMonth != headerYearMonth
            || dailySearchAdapter.textSize != textFontSize
        ) {
            loadSearchDailyData("")
        }

        val fileMD5 = FileUtil().getFileMD5(File(ConstUtil.WALLPAPER_PATH))
        if (wallpaperFileMD5.isEmpty() || wallpaperFileMD5 != fileMD5) {
            wallpaperFileMD5 = fileMD5
            setWallpaperAndStausBar()
        }

        val wallpaperAlpha = sharedPreferences!!.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY, 0.15F)
        activityMainBinding.wallpaper.alpha = wallpaperAlpha
    }

    private fun setWallpaperAndStausBar() {
        if (File(ConstUtil.WALLPAPER_PATH).exists()) {
            bitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
            activityMainBinding.wallpaper.setImageBitmap(bitmap)
            setLightStausBarsFromBitmap(bitmap)
        }
    }

    fun setLightStausBarsFromBitmap(bitmap: Bitmap) {
        Palette.from(bitmap).maximumColorCount(7).setRegion(0, 0, bitmap.width, 100)
            .generate { palette ->
                val mostUsed = palette?.swatches?.maxByOrNull { it.population }
                mostUsed?.let { swatch ->
                    val isDark = ColorUtils.calculateLuminance(swatch.rgb) < 0.5
                    val wallpaperAlpha = activityMainBinding.wallpaper.alpha
                    val insetsController =
                        WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !isDark && wallpaperAlpha > 0.5f
                }
            }
    }

    companion object {
        private var isDailyFragment: Boolean = true
        private var isRecyclerBinFragment: Boolean = false
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
     * 设置列表长按事件
     */
    private fun setSearchRecyclerViewItemOnLongClick() {
        dailySearchAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongOnClick(position: Int) {
                val moveInRecyclerBin =
                    sharedPreferences!!.getBoolean("switch_delete_to_recycler_bin_daily", true)
                val dailyEntity = dailyList.filter { !it.isDeleted }[position]
                val neutralButtonText =
                    if (dailyEntity.isPinned) getString(R.string.cancel_pinned) else getString(
                        R.string.pinned
                    )
                if (moveInRecyclerBin) {
                    MaterialAlertDialogBuilder(this@MainActivity).apply {
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
                            .setNeutralButton(neutralButtonText) { _, _ ->

                                val isPinned: Boolean =
                                    neutralButtonText == getString(R.string.pinned)

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
                                        false,
                                        dailyEntity.dailyLabel,
                                        isPinned = isPinned
                                    )
                                )
                            }
                            .create()
                            .show()
                    }

                } else {
                    MaterialAlertDialogBuilder(this@MainActivity).apply {
                        setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                        setPositiveButton(getString(R.string.delete)) { _, _ ->
                            val fileUtil = FileUtil()
                            dailyViewModel.queryDailyImageByUuid(dailyEntity.dailyUUID.toString())
                                .observe(this@MainActivity) { dailyImageList ->
                                    val existingImagePaths = dailyImageList.map { it.imagePath }.toSet()
                                    if (existingImagePaths.isNotEmpty()) {
                                        val list = existingImagePaths.toList()
                                        list.forEach {
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                }
                            dailyViewModel.queryDailyVideoByUuid(dailyEntity.dailyUUID.toString())
                                .observe(this@MainActivity) { dailyVideoList ->
                                    val existingVideoPaths = dailyVideoList.map { it.videoPath }.toSet()
                                    if (existingVideoPaths.isNotEmpty()) {
                                        val list = existingVideoPaths.toList()
                                        list.forEach {
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathVideoByDailyUuid(dailyEntity.dailyUUID.toString())
                                }
                            dailyViewModel.queryDailyAudioByUuid(dailyEntity.dailyUUID.toString())
                                .observe(this@MainActivity) { dailyAudioList ->
                                    val existingAudioPaths = dailyAudioList.map { it.audioPath }.toSet()
                                    if (existingAudioPaths.isNotEmpty()) {
                                        val list = existingAudioPaths.toList()
                                        list.forEach {
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathAudioByDailyUuid(dailyEntity.dailyUUID.toString())
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
        })
    }

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
                        LogUtil.d("delete filePath : $filepath")
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val themeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        if (themeColorId == currentThemeColorId) return
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

        lifecycleScope.launch {
            delay(300)
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
        Handler(Looper.getMainLooper()).postDelayed({

        }, 300)
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
            }
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
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
            if (currentFragment is DailyLikeFragment) {
                val selectedUUIDs = currentFragment.getSelectedItems()
                val dailyList = currentFragment.getDailyList()
                val tempDailyList = dailyList.filter { it.dailyUUID in selectedUUIDs }

                when (menuItem.itemId) {
                    R.id.item_select_all -> currentFragment.selectAllItems()

                    R.id.item_delete -> {
                        val moveInRecyclerBin = sharedPreferences!!
                            .getBoolean("switch_delete_to_recycler_bin_daily", true)

                        val isDeleted = tempDailyList.any { it.isDeleted }
                        if (isDeleted) {
                            dialog = MaterialAlertDialogUtil.showDialog(
                                this,
                                getString(R.string.do_you_want_to_delete_or_restore_the_daily),
                                getString(R.string.delete), {
                                    deleteSelected(tempDailyList)
                                    hideContextualToolbar()
                                },
                                getString(R.string.restore), {
                                    recyclerSelected(tempDailyList)
                                    hideContextualToolbar()
                                }, getString(R.string.cancel)
                            )
                        } else {
                            if (moveInRecyclerBin) {
                                dialog = MaterialAlertDialogUtil.showDialog(
                                    this,
                                    getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin),
                                    getString(R.string.sure),
                                    {
                                        recyclerSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    getString(R.string.delete), {
                                        deleteSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    getString(R.string.cancel)
                                )
                            } else {
                                dialog = MaterialAlertDialogUtil.showDialog(
                                    this,
                                    getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently),
                                    getString(R.string.sure),
                                    {
                                        deleteSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    getString(R.string.recycler_bin), {
                                        recyclerSelected(tempDailyList)
                                        hideContextualToolbar()
                                    },
                                    getString(R.string.cancel)
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
                                getString(
                                    R.string.confirm_pinned_journal_message,
                                    pinnedButtonText
                                ),
                                pinnedButtonText,
                                {
                                    tempDailyList.forEach { entity ->
                                        dailyViewModel.updateDaily(
                                            entity.copy(isPinned = !isPinned)
                                        )
                                    }
                                    hideContextualToolbar()
                                },
                                getString(R.string.cancel)
                            )
                        }
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
        dailyViewModel.toggleIsDelete(selectedList.map { it.dailyUUID ?: "" })
    }

    /**
     * 删除选中的日记项
     *
     * @param selectedList 选中的日记列表
     */
    private fun deleteSelected(selectedList: List<DailyEntity>) {
        dailyViewModel.deleteSelected(selectedList)
    }

    /**
     * 设置多选模式下的工具栏标题
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
            val showItem =
                if (currentFragment.isPinnedDisplay()) tempDailyList.isNotEmpty() &&
                        (onlyPinned || onlyUnpinned) else false

            activityMainBinding.contextualToolbar.menu.findItem(R.id.item_pinned)?.isVisible =
                showItem
        }
    }
}