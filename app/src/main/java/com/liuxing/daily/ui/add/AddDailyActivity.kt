package com.liuxing.daily.ui.add

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.adapter.ChangeDailyCardColorAdapter
import com.liuxing.daily.adapter.MoodAdapter
import com.liuxing.daily.adapter.WeatherAdapter
import com.liuxing.daily.databinding.ActivityAddDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.SharedPreferencesUtil.autoSaveDailySharedPreferences
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import com.liuxing.daily.util.StringUtil
import com.liuxing.daily.view.DailyTextInputEdit
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

private const val BACK_GROUND_COLOR_INDEX = "backgroundColorIndex"
private const val MOOD_INDEX = "moodIndex"
private const val TEMP_MOOD_INDEX = "tempMoodIndex"
private const val WEATHER_INDEX = "weatherIndex"
private const val TEMP_WEATHER_INDEX = "tempWeatherIndex"

class AddDailyActivity : AppCompatActivity() {

    private lateinit var activityAddDailyBinding: ActivityAddDailyBinding
    private var backgroundColorIndex = 0
    private lateinit var dailyViewModel: DailyViewModel
    private var singlePassword: String? = ""
    private var moodIndex = 0
    private var tempMoodIndex = 0
    private var weatherIndex = 0
    private var tempWeatherIndex = 0
    private val dailyUuid = UUID.randomUUID().toString()
    private val imageList = mutableSetOf<String>()
    private val tempImageList = mutableSetOf<String>()
    private lateinit var dailyTextInputEdit: DailyTextInputEdit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityAddDailyBinding = ActivityAddDailyBinding.inflate(layoutInflater)
        setContentView(activityAddDailyBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        initData(savedInstanceState)
        // 添加返回键回调
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    /**
     * 初始化视图
     */
    private fun initView() {
        SoftHideKeyBoardUtil(this)
        activityAddDailyBinding.tvDailyCount.text = "0${getString(R.string.word)}"
        dailyTextInputEdit = findViewById(R.id.input_content)
        dailyTextInputEdit.setImageDeletionListener(object :
            DailyTextInputEdit.ImageDeletionListener {
            override fun onImageDeleted(imagePath: String) {
                dailyViewModel.deleteSelectPathImage(imagePath)
                if (FileUtil().checkFileExists(imagePath)) {
                    FileUtil().deleteFile(imagePath)
                    imageList.remove(imagePath)
                }
            }
        })
    }

    /**
     * 初始化数据
     */
    private fun initData(savedInstanceState: Bundle?) {
        setActionBar()
        initMenu()
        setDailyCount()
        initViewModel()
        setDateTime()
        checkedTitleLength()
        restoreIndex(savedInstanceState)
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(activityAddDailyBinding.toolbar)
        this.supportActionBar?.setDisplayShowTitleEnabled(false)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * 初始化菜单
     */
    private fun initMenu() {
        val menuHost: MenuHost = this
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_add_daily, menu)
                when {
                    activityAddDailyBinding.inputTitle.text!!.trim()
                        .isEmpty() && dailyTextInputEdit.text!!.trim()
                        .isEmpty() && imageList.isEmpty() -> {
                        menu.findItem(R.id.item_save).setVisible(false)
                        invalidateOptionsMenu()
                    }

                    else -> {
                        menu.findItem(R.id.item_save).setVisible(true)
                        invalidateOptionsMenu()
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> isDailyNull()

                    R.id.item_change_background_color -> {
                        val view =
                            LayoutInflater.from(this@AddDailyActivity)
                                .inflate(R.layout.dialog_change_daily_card_color, null)
                        val materialAlertDialogBuilder =
                            MaterialAlertDialogBuilder(this@AddDailyActivity)
                        materialAlertDialogBuilder.setView(view)
                        val dialog = materialAlertDialogBuilder.create()
                        dialog.show()
                        val colorRecycler = view.findViewById<RecyclerView>(R.id.color_recycler)
                        colorRecycler.layoutManager = LinearLayoutManager(this@AddDailyActivity)
                        colorRecycler.adapter =
                            ChangeDailyCardColorAdapter(ConstUtil.backgroundColorList) { selectedColor, position ->
                                backgroundColorIndex = position
                            activityAddDailyBinding.main.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    selectedColor
                                )
                            )
                            activityAddDailyBinding.toolbar.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    selectedColor
                                )
                            )
                                dialog.dismiss()
                        }
                    }

                    R.id.item_save -> isDailyNull()

                    R.id.item_on_lock -> {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(this@AddDailyActivity)
                        when {
                            sharedPreferences.getString("forget_password_key", "") == "" -> {
                                val inflate =
                                    layoutInflater.inflate(
                                        R.layout.dialog_input_password_layout,
                                        null
                                    )
                                val inputPasswordLayout =
                                    inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
                                val inputPassword =
                                    inflate.findViewById<TextInputEditText>(R.id.input_password)
                                inputPasswordLayout.hint = getString(R.string.key)
                                MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                                    setTitle(getString(R.string.key))
                                    setView(inflate)
                                    setPositiveButton(
                                        getString(R.string.sure),
                                        object : DialogInterface.OnClickListener {
                                            override fun onClick(
                                                dialog: DialogInterface?,
                                                which: Int
                                            ) {
                                                when {
                                                    inputPassword.text.toString() == "" -> {
                                                        SnackbarUtil.showSnackbarShort(
                                                            activityAddDailyBinding.inputContent.rootView,
                                                            getString(R.string.please_enter_the_key_to_reset_your_password_if_you_forget_it)
                                                        )
                                                    }

                                                    else -> {
                                                        MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                                                            setMessage(getString(R.string.the_key_was_set_successfully_please_keep_it_in_mind))
                                                            setPositiveButton(
                                                                getString(R.string.sure), null
                                                            )
                                                            create()
                                                            show()
                                                        }
                                                        sharedPreferences.edit {
                                                            putString(
                                                                "forget_password_key",
                                                                HashUtil.hashSHA256(inputPassword.text.toString())
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                        })
                                    setNeutralButton(
                                        getString(R.string.cancel)
                                    ) { dialog, which ->
                                        SnackbarUtil.showSnackbarShort(
                                            activityAddDailyBinding.inputContent.rootView,
                                            getString(R.string.please_enter_the_key_to_reset_your_password_if_you_forget_it)
                                        )
                                    }
                                        .setCancelable(false)
                                        .create()
                                    show()
                                }
                            }

                            else -> {
                                val inflate =
                                    layoutInflater.inflate(
                                        R.layout.dialog_input_password_layout,
                                        null
                                    )
                                val inputPasswordLayout =
                                    inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
                                val inputPassword =
                                    inflate.findViewById<TextInputEditText>(R.id.input_password)
                                inputPasswordLayout.hint = getString(R.string.locked)
                                inputPassword.setText(singlePassword)
                                MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                                    setTitle(getString(R.string.locked))
                                    setView(inflate)
                                    setPositiveButton(
                                        getString(R.string.sure),
                                        object : DialogInterface.OnClickListener {
                                            override fun onClick(
                                                dialog: DialogInterface?,
                                                which: Int
                                            ) {
                                                singlePassword = inputPassword.text.toString()
                                            }

                                        })
                                    setNeutralButton(getString(R.string.cancel), null)
                                        .setCancelable(false)
                                        .create()
                                    show()
                                }
                            }
                        }
                    }

                    R.id.item_mood -> {
                        val inflate =
                            LayoutInflater.from(this@AddDailyActivity)
                                .inflate(R.layout.dialog_mood_layout, null)
                        val recyclerView = inflate.findViewById<RecyclerView>(R.id.recycler_view)
                        val gridLayoutManager = GridLayoutManager(this@AddDailyActivity, 3)
                        recyclerView.layoutManager = gridLayoutManager
                        val moodAdapter = MoodAdapter(this@AddDailyActivity)
                        recyclerView.adapter = moodAdapter
                        val dialog: AlertDialog?
                        MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                            setTitle(
                                getString(R.string.mood)
                            )
                            setView(inflate)
                            setPositiveButton(getString(R.string.cancel), null)
                            create()
                            dialog = show()
                        }
                        moodAdapter.setOnItemClickListener(object : OnItemClickListener {
                            override fun onItemClick(position: Int) {
                                setMoodIcon(position)
                                dialog?.dismiss()
                            }

                        })
                    }

                    R.id.item_weather -> {
                        val inflate = LayoutInflater.from(this@AddDailyActivity)
                            .inflate(R.layout.dialog_weather_layout, null)
                        val recyclerView = inflate.findViewById<RecyclerView>(R.id.recycler_view)
                        val gridLayoutManager = GridLayoutManager(this@AddDailyActivity, 3)
                        recyclerView.layoutManager = gridLayoutManager
                        val weatherAdapter = WeatherAdapter(this@AddDailyActivity)
                        recyclerView.adapter = weatherAdapter
                        val dialog: AlertDialog
                        MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                            setTitle(getString(R.string.weather))
                            setView(inflate)
                            setPositiveButton(getString(R.string.cancel), null)
                            create()
                            dialog = show()
                        }
                        weatherAdapter.setOnItemClickListener(object : OnItemClickListener {
                            override fun onItemClick(position: Int) {
                                setWeatherIcon(position)
                                dialog.dismiss()
                            }

                        })
                    }

                    R.id.item_add_image -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            setType("image/*")
                            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        addImageLauncher.launch(intent)
                    }
                }

                return true
            }
        })
    }

    /**
     * 设置心情图标
     */
    private fun setMoodIcon(position: Int) {
        if (position == ConstUtil.moodList.size - 1) {
            moodIndex = 0
            tempMoodIndex = 0
            activityAddDailyBinding.ivMood.visibility = View.GONE
        } else {
            activityAddDailyBinding.ivMood.setImageDrawable(
                ContextCompat.getDrawable(
                    this@AddDailyActivity,
                    ConstUtil.moodList[position]
                )
            )
            moodIndex = position
            tempMoodIndex = 1
            activityAddDailyBinding.ivMood.visibility = View.VISIBLE
        }
    }

    /**
     * 设置天气图标
     */
    private fun setWeatherIcon(position: Int) {
        if (position == ConstUtil.weatherList.size - 1) {
            weatherIndex = 0
            tempWeatherIndex = 0
            activityAddDailyBinding.ivWeather.visibility = View.GONE
        } else {
            activityAddDailyBinding.ivWeather.visibility = View.VISIBLE
            activityAddDailyBinding.ivWeather.setImageDrawable(
                ContextCompat.getDrawable(
                    this@AddDailyActivity,
                    ConstUtil.weatherList[position]
                )
            )
            weatherIndex = position
            tempWeatherIndex = 1
        }
    }

    /**
     * 设置日记字数
     */
    private fun setDailyCount() {
        dailyTextInputEdit.addTextChangedListener {
            "${getDailyCount()}${getString(R.string.word)}".also {
                activityAddDailyBinding.tvDailyCount.text = it
            }
        }
        activityAddDailyBinding.inputTitle.addTextChangedListener {
            "${getDailyCount()}${getString(R.string.word)}".also {
                activityAddDailyBinding.tvDailyCount.text = it
            }
        }
    }

    /**
     * 获取日记字数
     */
    private fun getDailyCount(): Int =
        activityAddDailyBinding.inputTitle.text!!.length.plus(dailyTextInputEdit.getWordCount())

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    /**
     * 保存日记
     */
    private fun saveDaily() {
            dailyViewModel.insertDaily(
                DailyEntity(
                    title = activityAddDailyBinding.inputTitle.text.toString(),
                    content = dailyTextInputEdit.text.toString(),
                    dateTime = DateUtil.dateStringToDate(
                        activityAddDailyBinding.tvDateTime.text.toString(),
                        0
                    ),
                    backgroundColorIndex = backgroundColorIndex,
                    singlePassword = HashUtil.hashSHA256(singlePassword.toString()),
                    moodIndex = if (tempMoodIndex == 0) moodIndex else moodIndex + 1,
                    weatherIndex = if (tempWeatherIndex == 0) weatherIndex else weatherIndex + 1,
                    dailyUUID = dailyUuid
                )
            )
            finish()
    }

    /**
     * 监听返回键
     */
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isDailyNull()
            }
        }

    /**
     * 设置日期时间
     */
    private fun setDateTime() {
        activityAddDailyBinding.tvDateTime.text =
            DateUtil.getDateString(0, DateUtil.getCurrentDate())
    }

    /**
     * 判断日记是否为空
     */
    private fun isDailyNull() {
        // 如果文本都为空，则直接退出
        if (contentIsNotNull()) {
            // 如果不为空，就询问是否保存
            MaterialAlertDialogBuilder(this@AddDailyActivity)
                .setMessage(getString(R.string.do_you_save_this_diary))
                .setPositiveButton(getString(R.string.sure)) { dialog, which ->
                    // 将自动保存的数据清空
                    autoSaveDailySharedPreferences(
                        this, 1, "", "", 0, 0, "", 0, 0, "", false,
                    )
                    isSystemExit = false
                    saveDaily()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                    // 将自动保存的数据清空
                    autoSaveDailySharedPreferences(
                        this, 1, "", "", 0, 0, "", 0, 0, "", false,
                    )
                    isSystemExit = false
                    notSaveToDeleteAppImage()
                    finish()
                }
                .create()
                .show()
        } else {
            isSystemExit = false
            finish()
        }

    }

    /**
     * 不保存则删除应用私有目录下对应的图片
     */
    private fun notSaveToDeleteAppImage() {
        CoroutineScope(Dispatchers.IO).launch {
            dailyViewModel.deletePathImageByDailyUuid(dailyUuid)
            imageList.forEach { path ->
                FileUtil().deleteFile(path)
            }
        }
    }

    /**
     * 判断主要内容是否不为空
     */
    private fun contentIsNotNull(): Boolean {
        return activityAddDailyBinding.inputTitle.text!!.trim()
            .isNotEmpty() || dailyTextInputEdit.text!!.trim()
            .isNotEmpty() || imageList.isNotEmpty()
    }

    /**
     * 检查标题长度
     */
    private fun checkedTitleLength() =
        activityAddDailyBinding.inputTitle.addTextChangedListener { s ->
            StringUtil.checkedEditContentLength(
                s,
                30,
                activityAddDailyBinding.inputTitle
            )
        }

    override fun onStart() {
        super.onStart()
        isSystemExit = true
    }

    override fun onStop() {
        super.onStop()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val autoSave = sharedPreferences.getBoolean("switch_preference_auto_save", true)
        if (isSystemExit && autoSave && contentIsNotNull()) {
            autoSaveDailySharedPreferences(
                this, 0,
                activityAddDailyBinding.inputTitle.text.toString(),
                dailyTextInputEdit.text.toString(),
                DateUtil.dateStringToDate(
                    activityAddDailyBinding.tvDateTime.text.toString(),
                    0
                ),
                backgroundColorIndex,
                singlePassword.toString(),
                moodIndex,
                weatherIndex,
                dailyUuid,
                imageList.isNotEmpty()
            )
        }
    }

    companion object {
        private var isSystemExit = false
    }

    /**
     * 添加图片启动器
     */
    private val addImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                try {
                    if (result.resultCode != Activity.RESULT_OK) return
                    val data = result.data ?: return
                    tempImageList.clear()
                    data.clipData?.let { clipData ->
                        for (i in 0 until clipData.itemCount) {
                            addImage(clipData.getItemAt(i).uri)
                        }
                    } ?: data.data?.let { uri ->
                        addImage(uri)
                    }
                    dailyViewModel.insertDailyImagePath(dailyUuid, tempImageList.toList())
                    dailyTextInputEdit.insertImages(tempImageList.toList())
                    tempImageList.clear()
                } catch (e: Exception) {
                    MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                        setMessage(getString(R.string.add_failed))
                        setPositiveButton(getString(R.string.sure), null)
                        create()
                        show()
                    }
                }
            }
        })

    /**
     * 添加图片
     *
     * @param uri 图片
     */
    private fun addImage(uri: Uri) {
        val copyImageToMyAppDir =
            CopyUtil.copyImageToMyAppDir(this@AddDailyActivity, uri)
        imageList.add(copyImageToMyAppDir)
        tempImageList.add(copyImageToMyAppDir)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BACK_GROUND_COLOR_INDEX, backgroundColorIndex)
        outState.putInt(MOOD_INDEX, moodIndex)
        outState.putInt(TEMP_MOOD_INDEX, tempMoodIndex)
        outState.putInt(WEATHER_INDEX, weatherIndex)
        outState.putInt(TEMP_WEATHER_INDEX, tempWeatherIndex)
    }

    /**
     * 恢复索引
     */
    private fun restoreIndex(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            backgroundColorIndex = savedInstanceState.getInt(BACK_GROUND_COLOR_INDEX)
            activityAddDailyBinding.main.setBackgroundColor(
                ContextCompat.getColor(
                    this@AddDailyActivity,
                    ConstUtil.backgroundColorList[backgroundColorIndex]
                )
            )
            activityAddDailyBinding.toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    this@AddDailyActivity,
                    ConstUtil.backgroundColorList[backgroundColorIndex]
                )
            )

            val moodIndex = savedInstanceState.getInt(MOOD_INDEX)
            val tempMoodIndex = savedInstanceState.getInt(TEMP_MOOD_INDEX)
            if (tempMoodIndex != 0) {
                setMoodIcon(moodIndex)
            }

            val weatherIndex = savedInstanceState.getInt(WEATHER_INDEX)
            val tempWeatherIndex = savedInstanceState.getInt(TEMP_WEATHER_INDEX)
            if (tempWeatherIndex != 0) {
                setWeatherIcon(weatherIndex)
            }
        }
    }
}