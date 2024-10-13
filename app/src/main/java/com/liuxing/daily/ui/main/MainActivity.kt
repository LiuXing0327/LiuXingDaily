package com.liuxing.daily.ui.main

import android.app.Activity
import android.content.Intent
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
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailySearchAdapter
import com.liuxing.daily.databinding.ActivityMainBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.add.AddDailyActivity
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.ui.settings.SettingsActivity
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.VersionUtil
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
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter


class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailySearchAdapter: DailySearchAdapter
    private var dailyList: List<DailyEntity> = ArrayList()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

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
        initData()
    }

    private fun initData() {
        setActionBar()
        initNavController()
        setNavigation()
        searchViewShowingStatusBarColor(false)
        searchViewFocus()
        initViewModel()
        initSearchRecyclerView()
        initSearchView()
        setSearchRecyclerViewData("")
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
            AppBarConfiguration.Builder(R.id.dailyFragment, R.id.calendarQueryDailyFragment)
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
        FollowPatternSetColor(typedValue.data)
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
     * 浅色模式：-1120012
     * 深色墨色：-13685706
     */
    private fun FollowPatternSetColor(ColorValue: Int) = if (ColorValue == -1120012) {
        window.getDecorView()
            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    } else {
        window.getDecorView().setSystemUiVisibility(0)
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
    private fun setSearchRecyclerViewData(searchQuery: String) {
        dailySearchAdapter.setDailyList(this@MainActivity, dailyList, searchQuery)
        setSearchRecyclerViewItemOnClick()
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
            setSearchRecyclerViewData(v.text.toString())
            true
        }

        // 点击搜索视图菜单搜索事件
        activityMainBinding.searchView.setOnMenuItemClickListener { item ->
            when (item!!.itemId) {
                R.id.item_search -> setSearchRecyclerViewData(activityMainBinding.searchView.text.toString())
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
        return true
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
                        setType("text/*")
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    importDailyLauncher.launch(intent)
                }

                R.id.item_export_all_daily -> {
                    if (dailyList.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            setType("text/csv")
                            putExtra(Intent.EXTRA_TITLE, "daily.csv")
                        }
                        exportAllDailyLauncher.launch(intent)
                    }
                }
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
                            val inputStream = contentResolver.openInputStream(uri)
                            val bufferedReader = BufferedReader(
                                InputStreamReader(inputStream)
                            )
                            val type = object : TypeToken<List<DailyEntity>>() {}.type
                            val gson = Gson()
                            val dailyList: List<DailyEntity> = gson.fromJson(bufferedReader, type)
                            dailyList.forEach { dailyEntity ->
                                dailyViewModel.insertDaily(dailyEntity)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                MaterialAlertDialogBuilder(this@MainActivity).apply {
                                    setMessage("导入失败")
                                    setPositiveButton(
                                        getString(R.string.sure),
                                        null
                                    )
                                    create()
                                    show()
                                }
                            }
                        }
                    }
                }
            }
        )

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

                val outputStream: OutputStream =
                    contentResolver.openOutputStream(url)!!
                val bufferedWriter =
                    BufferedWriter(OutputStreamWriter(outputStream))
                val gson = GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                bufferedWriter.write(gson.toJson(dailyList))
                bufferedWriter.close()
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
        dailyViewModel.queryAllDaily().observe(this, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                dailyList = value
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (dailySearchAdapter.headerYearMonth != sharedPreferences.getBoolean(
                "switch_preference_header_display",
                true
            )
        ) {
            setSearchRecyclerViewData("")
        }
    }

    companion object {
        private var isDailyFragment: Boolean = true
    }
}