package com.liuxing.daily.ui.main

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Menu
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailySearchAdapter
import com.liuxing.daily.databinding.ActivityMainBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyWithImage
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.ui.add.AddDailyActivity
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.ui.settings.SettingsActivity
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.util.VersionUtil
import com.liuxing.daily.util.WindowUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailySearchAdapter: DailySearchAdapter
    private var dailyList: List<DailyEntity> = ArrayList()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private var sharedPreferences: SharedPreferences? = null
    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        /*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }*/
        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url(ConstUtil.CHECK_APP_VERSION_URL).build()
        val handler = Handler(Looper.getMainLooper())
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                val jsonString = response.body?.string()
                val jsonObject = JSONObject(jsonString.toString())
                val latestVersionCode = jsonObject.getInt("versionCode")
                val currentVersionCode = VersionUtil.getVersionCode(this@MainActivity)
                if (latestVersionCode > currentVersionCode) {
                    handler.post {
                        CheckAppUpdateUtil.checkUpdate(this@MainActivity)
                    }

                }
            }

        })
        // 添加返回键回调
        onBackPressedDispatcher.addCallback(
            this,
            onBackPressedCallback
        )
        insertAutoDaily()
        initData()
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
            if (autoSaveTitle!!.isNotEmpty() || autoSaveContent!!.isNotEmpty() || autoSaveImageIsNotNull) {
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
                            dailyUUID = autoSaveDailyUuid
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
                                dailyUUID = autoSaveDailyUuid
                            )
                        )
                    }
                }

                // 将自动保存的数据清空
                SharedPreferencesUtil.autoSaveDailySharedPreferences(
                    this, 1, "", "", 0, 0, "", 0, 0, "", false,
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
        searchViewShowingStatusBarColor(false)
        searchViewFocus()
        initViewModel()
        initSharePreferences()
        initSearchRecyclerView()
        initSearchView()
        setSearchRecyclerViewData()
        initSearchBar()
        floatingOnClick()
        onDestinationChanged()
        setDailyData()
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
                R.id.recyclerBinFragment
            )
                .setOpenableLayout(activityMainBinding.main).build()
    }

    /**
     * 初始化导航控制器
     */
    private fun initNavController() {
        val navHostFragment =
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
     * 搜索视图的焦点监听
     */
    private fun searchViewFocus() {
        activityMainBinding.searchView.editText.setOnFocusChangeListener { v, hasFocus ->
            searchViewShowingStatusBarColor(hasFocus)
        }
    }

    /**
     * 搜索视图显示？不显示的状态栏颜色
     *
     * @param showing 是否显示
     */
    private fun searchViewShowingStatusBarColor(showing: Boolean) {
        // 获取主题属性值
        val typedValue = TypedValue()
        theme.resolveAttribute(
            R.attr.searchViewShowingColor, typedValue, true
        )
        WindowUtil.FollowPatternSetColor(window, typedValue.data)
        when {
            showing -> {
                window.statusBarColor = typedValue.data
            }

            else -> {
                window.statusBarColor =
                    ContextCompat.getColor(this, android.R.color.transparent)
            }
        }
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
                when {
                    isOpenSearchView() -> activityMainBinding.searchView.hide()
                    activityMainBinding.main.isOpen -> activityMainBinding.main.close()

                    isDailyFragment -> finish()
                    !isDailyFragment -> navController.navigate(R.id.dailyFragment)

                    else -> finish()
                }
            }
        }

    /**
     * 浮动按钮点击事件
     */
    private fun floatingOnClick() {
        activityMainBinding.floatingActionButton.setOnClickListener {
            IntentUtil.startActivity(
                this,
                AddDailyActivity::class.java
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
        loadSearchDailyData("")
        setSearchRecyclerViewItemOnClick()
        setSearchRecyclerViewItemOnLongClick()
    }

    /**
     * 加载搜索日记的数据
     */
    private fun loadSearchDailyData(searchQuery: String) {
        dailySearchAdapter.setDailyList(
            this@MainActivity,
            dailyList,
            searchQuery,
            dailyViewModel,
            this
        )
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
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            setType("application/zip")
                            putExtra(Intent.EXTRA_TITLE, "daily.zip")
                        }
                        exportAllDailyLauncher.launch(intent)
                    }
                }

                R.id.item_clear -> {
                    val moveInRecyclerBin =
                        sharedPreferences!!.getBoolean("switch_delete_to_recycler_bin_daily", true)
                    if (moveInRecyclerBin) {
                        MaterialAlertDialogBuilder(this).apply {
                            setMessage(getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin))
                            setPositiveButton(getString(R.string.sure)) { dialog, which ->
                                dailyList.forEach { dailyEntity ->
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
                                            true
                                        )
                                    )
                                }
                            }
                                .setNegativeButton(getString(R.string.cancel), null)
                                .create()
                                .show()
                        }

                    } else {
                        MaterialAlertDialogBuilder(this).apply {
                            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                            setPositiveButton(getString(R.string.delete)) { _, _ ->
                                dailyList.forEach { dailyEntity ->
                                    dailyEntity.dailyUUID?.let {
                                        dailyViewModel.deletePathImageByDailyUuid(
                                            it
                                        )
                                    }
                                    dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                    dailyViewModel.deleteDaily(dailyEntity)
                                }
                            }
                            setNegativeButton(getString(R.string.recycler_bin)) { _, _ ->
                                dailyList.forEach { dailyEntity ->
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
                                            true
                                        )
                                    )
                                }
                            }
                            setNeutralButton(getString(R.string.cancel), null)
                            create()
                            show()
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
                                importDailyZip(uri)
                            } else {
                                withContext(Dispatchers.Main) {
                                    MaterialAlertDialogBuilder(this@MainActivity).apply {
                                        setMessage(getString(R.string.import_failed))
                                        setPositiveButton(getString(R.string.sure), null)
                                        create()
                                        show()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                MaterialAlertDialogBuilder(this@MainActivity).apply {
                                    setMessage(getString(R.string.import_failed))
                                    setPositiveButton(getString(R.string.sure), null)
                                    create()
                                    show()
                                }
                            }
                        }
                    }
                }
            })

    /**
     * 导入日记
     */
    private suspend fun importDailyZip(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            withContext(Dispatchers.Main) {
                MaterialAlertDialogBuilder(this@MainActivity).apply {
                    setMessage(getString(R.string.import_failed))
                    setPositiveButton(getString(R.string.sure), null)
                    create()
                    show()
                }
            }
            return
        }
        val zipInputStream = ZipInputStream(inputStream)
        val diaryWithImageList = mutableListOf<DailyWithImage>()
        val processedFileList = mutableSetOf<String>()
        val imageDir = File(getExternalFilesDir(null), "Pictures")
        if (!imageDir.exists()) imageDir.mkdirs()
        var zipEntry: ZipEntry?
        while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
            val entryName = zipEntry!!.name
            if (entryName.endsWith(".json")) {
                if (processedFileList.contains(entryName)) {
                    zipInputStream.closeEntry()
                    continue
                }
                val reader = InputStreamReader(zipInputStream)
                val gson = Gson()
                val dailyWithImage = gson.fromJson(reader, DailyWithImage::class.java)
                diaryWithImageList.add(dailyWithImage)
                processedFileList.add(entryName)
            } else if (entryName.startsWith("Pictures/")) {
                val imageFile = File(imageDir, zipEntry!!.name.removePrefix("Pictures/"))
                val imageOutputStream =
                    withContext(Dispatchers.IO) {
                        FileOutputStream(imageFile)
                    }
                zipInputStream.copyTo(imageOutputStream)
                val imagePath = imageFile.absolutePath
                diaryWithImageList.forEach { dailyWithImage ->
                    dailyWithImage.imageList.forEach { dailyImageEntity ->
                        if (dailyImageEntity.imagePath == null) {
                            dailyImageEntity.imagePath = imagePath
                        }
                    }
                }
            }
            withContext(Dispatchers.IO) {
                zipInputStream.closeEntry()
            }
        }
        withContext(Dispatchers.IO) {
            zipInputStream.close()
        }
        diaryWithImageList.forEach { dailyWithImage ->
            val daily = dailyWithImage.dailyEntity
            if (daily != null) {
                dailyViewModel.insertDaily(daily)
                dailyWithImage.imageList?.forEach { dailyImageEntity ->
                    if (dailyImageEntity?.imagePath != null) {
                        dailyImageEntity.dailyUuid = daily.dailyUUID
                        val imagePaths = mutableSetOf(dailyImageEntity.imagePath)
                        dailyViewModel.insertDailyImagePath(
                            dailyImageEntity.dailyUuid.toString(),
                            imagePaths.toList() as List<String>
                        )
                    }
                }
            }
        }
    }

    /**
     * 导出所有日记启动器
     */
    private val exportAllDailyLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode != Activity.RESULT_OK) return
                val data = result.data ?: return
                val url = data.data!!
                val processedFileList = mutableSetOf<String>()
                CoroutineScope(Dispatchers.IO).launch {
                    val dailyWithImageList = mutableListOf<DailyWithImage>()
                    dailyList.forEach { dailyEntity ->
                        if (!processedFileList.contains(dailyEntity.dailyUUID)) {
                            processedFileList.add(dailyEntity.dailyUUID.toString())
                            val queryDailyImageByUuidToList =
                                dailyViewModel.queryDailyImageByUuidToList(dailyEntity.dailyUUID.toString())
                            dailyWithImageList.add(
                                DailyWithImage(
                                    dailyEntity,
                                    queryDailyImageByUuidToList
                                )
                            )
                        }
                    }
                    val zipOutputStream = ZipOutputStream(contentResolver.openOutputStream(url))
                    // 遍历每个每日条目并将其添加到 ZIP 文件中
                    dailyWithImageList.forEach { dailyWithImage ->
                        // 将日记导出为 JSON 文件
                        val entry = ZipEntry("${dailyWithImage.dailyEntity.dailyUUID}.json")
                        zipOutputStream.putNextEntry(entry)
                        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                        val json = gson.toJson(dailyWithImage)
                        zipOutputStream.write(json.toByteArray())
                        zipOutputStream.closeEntry()
                        // 将关联图像导出到 ZIP 文件中的 Pictures 目录
                        dailyWithImage.imageList?.forEach { dailyImageEntity ->
                            dailyImageEntity?.imagePath?.let { imagePath ->
                                val imageFile = File(imagePath)
                                if (imageFile.exists()) {
                                    val imageEntry = ZipEntry("Pictures/${imageFile.name}")
                                    zipOutputStream.putNextEntry(imageEntry)
                                    val imageStream = FileInputStream(imageFile)
                                    imageStream.copyTo(zipOutputStream)
                                    zipOutputStream.closeEntry()
                                }
                            }
                        }
                    }
                    // 关流
                    zipOutputStream.close()
                }
            }
        })

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
                if (fragment.id == R.id.dailyFragment) {
                    isDailyFragment = true
                    View.VISIBLE
                } else {
                    isDailyFragment = false
                    View.GONE
                }
        }
    }

    /**
     * 设置日记数据
     */
    private fun setDailyData() {
        val headerView = activityMainBinding.navigationView.getHeaderView(0)
        val tvDailyCount = headerView.findViewById<MaterialTextView>(R.id.tv_daily_count)
        val tvDailyTextCount =
            headerView.findViewById<MaterialTextView>(R.id.tv_daily_text_count)
        val tvDailyImageCount = headerView.findViewById<MaterialTextView>(R.id.tv_daily_image_count)
        dailyViewModel.queryImageCount().observe(this) {
            tvDailyImageCount.text = "${it}${getString(R.string.sheet)}"
        }

        dailyViewModel.queryAllDaily().observe(this, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                dailyList = value
                var dailyTextSize = 0
                if (isUpdating) return
                val tempDailyList = dailyList
                tempDailyList.forEach { dailyEntity ->
                    dailyTextSize += TextUtil.getWordCount(dailyEntity.title!!.plus(dailyEntity.content!!))
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
                            false
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val headerYearMonth = sharedPreferences.getBoolean(
            "switch_preference_header_display",
            true
        )
        if (dailySearchAdapter.headerYearMonth != headerYearMonth
        ) {
            loadSearchDailyData("")
        }
    }

    companion object {
        private var isDailyFragment: Boolean = true
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
                if (moveInRecyclerBin) {
                    MaterialAlertDialogBuilder(this@MainActivity).apply {
                        setMessage(getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin))
                        setPositiveButton(getString(R.string.sure)) { dialog, which ->
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
                                    true
                                )
                            )
                        }
                            .setNegativeButton(getString(R.string.cancel), null)
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
                                    dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                    dailyViewModel.deleteDaily(dailyEntity)
                                }
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
                                    true
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
}