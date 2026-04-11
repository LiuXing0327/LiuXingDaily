package com.liuxing.daily.ui.add

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.liuxing.daily.R
import com.liuxing.daily.adapter.ChangeDailyCardColorAdapter
import com.liuxing.daily.adapter.MoodAdapter
import com.liuxing.daily.adapter.SelectDailyLabelAdapter
import com.liuxing.daily.adapter.WeatherAdapter
import com.liuxing.daily.databinding.ActivityAddDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.extension.formatDateString
import com.liuxing.daily.extension.formatDateTimeWeek
import com.liuxing.daily.extension.getExternalPicturesFilesDir
import com.liuxing.daily.extension.setVisibility
import com.liuxing.daily.extension.toMonthDay
import com.liuxing.daily.listener.OnEnabledChangedListener
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.StringChangedListener
import com.liuxing.daily.ui.draw.DrawImageActivity
import com.liuxing.daily.ui.settings.DailySettingsConst
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.SharedPreferencesUtil.autoSaveDailySharedPreferences
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import com.liuxing.daily.util.StringUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.view.DailyRichText
import com.liuxing.daily.view.DailyTextInputEdit
import com.liuxing.daily.viewmodel.DailyViewModel
import com.liuxing.daily.viewmodel.MainViewModel
import com.liuxing.library.ColorPickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

private const val BACK_GROUND_COLOR_INDEX = "backgroundColorIndex"
private const val MOOD_INDEX = "moodIndex"
private const val TEMP_MOOD_INDEX = "tempMoodIndex"
private const val WEATHER_INDEX = "weatherIndex"
private const val TEMP_WEATHER_INDEX = "tempWeatherIndex"
private const val DAILY_LABEL = "dailyLabel"
private const val SELECT_IMAGE = 0
private const val TAKE_IMAGE = 1

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
    private val videoList = mutableSetOf<String>()
    private val audioList = mutableSetOf<String>()
    private val tempImageList = mutableSetOf<String>()
    private val tempVideoList = mutableSetOf<String>()
    private val tempAudioList = mutableSetOf<String>()
    private lateinit var dailyTextInputEdit: DailyTextInputEdit
    private var dailyLabelList = mutableListOf<String>()
    private var dailyLabel: String = ""
    private lateinit var mainViewModel: MainViewModel
    private var drawImageName = ""
    private lateinit var onEnabledChangedListener: OnEnabledChangedListener
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }
    private val autoSave by lazy {
        sharedPreferences.getBoolean("switch_preference_auto_save", true)
    }

    private lateinit var stringChangedListener: StringChangedListener
    private lateinit var imagePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        activityAddDailyBinding = ActivityAddDailyBinding.inflate(layoutInflater)
        setContentView(activityAddDailyBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
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
        dailyTextInputEdit.setVideoDeletionListener(object :
            DailyTextInputEdit.VideoDeletionListener {
            override fun onVideoDeleted(videoPath: String) {
                if (FileUtil().checkFileExists(videoPath)) {
                    FileUtil().deleteFile(videoPath)
                    videoList.remove(videoPath)
                }
            }

        })
        dailyTextInputEdit.setAudioDeletionListener(object :
            DailyTextInputEdit.AudioDeletionListener {
            override fun onAudioDeleted(audioPath: String) {
                if (FileUtil().checkFileExists(audioPath)) {
                    FileUtil().deleteFile(audioPath)
                    audioList.remove(audioPath)
                }
            }

        })

        val textLineSpacingValue = sharedPreferences.getFloat("text_line_spacing_preference", 0F)
        dailyTextInputEdit.setLineSpacing(textLineSpacingValue,1F)

        val textSize = sharedPreferences.getFloat(ConstUtil.TEXT_SIZE_KEY, 16F)
        activityAddDailyBinding.inputTitle.textSize = textSize + 4
        dailyTextInputEdit.textSize = textSize

        enableOnBack()
        onEnabledChangedListener.onEnableChanged(contentIsNotNull())
        activityAddDailyBinding.inputTitle.addTextChangedListener {
            onEnabledChangedListener.onEnableChanged(contentIsNotNull())
        }
        activityAddDailyBinding.inputContent.addTextChangedListener {
            onEnabledChangedListener.onEnableChanged(contentIsNotNull())
        }

        // 默认隐藏标题输入框
        val showTitle =
            SharedPreferencesUtil.getBoolean(this, DailySettingsConst.TITLE_SWITCH_KEY, false)
        activityAddDailyBinding.inputTitleContainer?.setVisibility(showTitle)

        loadLabel()

        activityAddDailyBinding.actionBtnSave.setOnClickListener {
            isDailyNull()
        }
    }

    /**
     * 加载标签
     */
    private fun loadLabel() {
        dailyLabel = intent.getStringExtra(DAILY_LABEL) ?: ""

        if (dailyLabel.isEmpty()) return

        activityAddDailyBinding.tvLabel.text = dailyLabel
        activityAddDailyBinding.lLabel.visibility = View.VISIBLE
    }

    /**
     * 初始化数据
     */
    private fun initData(savedInstanceState: Bundle?) {
        setActionBar()
        setupFloatingRichTextToolbar()
        initMenu()
        setDailyCount()
        initViewModel()
        setDateTime()
        selectDateTime()
        checkedTitleLength()
        restoreIndex(savedInstanceState)
        setDailyLabel()
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
                menu.findItem(R.id.item_save).isVisible = false
/*                when {
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
                }*/
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
                        materialAlertDialogBuilder.setTitle(getString(R.string.change_color))
                        materialAlertDialogBuilder.setView(view)
                        materialAlertDialogBuilder.setPositiveButton(
                            getString(R.string.close),
                            null
                        )
                        val dialog = materialAlertDialogBuilder.create()
                        dialog.show()
                        val colorRecycler = view.findViewById<RecyclerView>(R.id.color_recycler)
                        colorRecycler.layoutManager = GridLayoutManager(this@AddDailyActivity, 3)
                        colorRecycler.adapter =
                            ChangeDailyCardColorAdapter(
                                ConstUtil.backgroundColorList,
                                backgroundColorIndex
                            ) { selectedColor, position ->
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
                        val addImageOptions = arrayOf(getString(R.string.select_image),
                            getString(R.string.take_image))
                        MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                            setTitle(getString(R.string.add_image))
                            setItems(addImageOptions) { _, which ->
                                when (which) {
                                    SELECT_IMAGE -> {
                                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                            type = "image/*"
                                            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                            addCategory(Intent.CATEGORY_OPENABLE)
                                        }
                                        addLauncher.launch(intent)
                                    }

                                    TAKE_IMAGE -> {
                                        openCamera()
                                    }
                                }
                            }
                            setPositiveButton(getString(R.string.cancel), null)
                            create()
                            show()
                        }

                    }

                    R.id.item_add_video -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            setType("video/*")
                            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        addLauncher.launch(intent)
                    }

                    R.id.item_add_audio -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            setType("audio/*")
                            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        addLauncher.launch(intent)
                    }

                    R.id.item_label -> {
                        val view =
                            layoutInflater.inflate(R.layout.dialog_select_daily_label_layout, null)
                        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
                        val linearLayoutManager = LinearLayoutManager(this@AddDailyActivity)
                        recyclerView.layoutManager = linearLayoutManager
                        val selectDailyLabelAdapter = SelectDailyLabelAdapter(dailyLabelList)
                        recyclerView.adapter = selectDailyLabelAdapter
                        MaterialAlertDialogBuilder(this@AddDailyActivity).apply {
                            setTitle(getString(R.string.label))
                            setView(view)
                            setPositiveButton(getString(R.string.not_add)) { _, _ ->
                                dailyLabel = ""
                                activityAddDailyBinding.lLabel?.visibility = View.GONE
                            }
                            setNegativeButton(getString(R.string.new_label)) { _, _ ->
                                showLabelInputDialog()
                            }
                            setNeutralButton(getString(R.string.cancel), null)
                            val dialog = create()
                            dialog.show()
                            selectDailyLabelAdapter.setOnItemClickListener(object :
                                OnItemClickListener {
                                override fun onItemClick(position: Int) {
                                    dailyLabel = dailyLabelList[position]
                                    activityAddDailyBinding.tvLabel?.text = dailyLabel
                                    activityAddDailyBinding.lLabel?.visibility = View.VISIBLE
                                    dialog.dismiss()
                                }

                            })
                        }
                    }

                    R.id.item_drawing -> {
                        val intent = Intent(this@AddDailyActivity, DrawImageActivity::class.java)
                        addDrawImageLauncher.launch(intent)
                    }
                }

                return true
            }
        })
    }

    /**
     * 开启摄像头
     */
    private fun openCamera() {
        val externalPicturesFilesDir = getExternalPicturesFilesDir()
        val imageName = UUID.randomUUID().toString() + ".jpg"
        val file = File(externalPicturesFilesDir, imageName)

        imagePath = file.absolutePath

        val imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        takeImageLauncher.launch(intent)
    }

    private val takeImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                tempImageList.clear()
                imageList.add(imagePath)
                tempImageList.add(imagePath)
                if (tempImageList.isNotEmpty()) {
                    dailyViewModel.insertDailyImagePath(dailyUuid, tempImageList.toList())
                    dailyTextInputEdit.insertImages(tempImageList.toList())
                }
                tempImageList.clear()
            }
        }

    private val addDrawImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                drawImageName =
                    result.data?.getStringExtra("draw_image_name") ?: ""
            }
        }

    /**
     * 显示输入标签的对话框
     */
    private fun showLabelInputDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input_label_layout, null)
        val inputLabel = view.findViewById<TextInputEditText>(R.id.input_label)
        val inputLabelLayout = view.findViewById<TextInputLayout>(R.id.input_label_layout)
        MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog).apply {
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
                val input = dailyLabelList.any { labels ->
                    labels == inputText
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
     * 创建日记标签
     *
     * @param label 标签
     */
    private fun addDailyLabel(label: String) {
        dailyViewModel.insertDailyLabel(DailyLabelEntity(label = label))
    }

    /**
     * 设置日记标签
     */
    private fun setDailyLabel() {
        dailyViewModel.queryAllDailyLabel().observe(this) { dailyLabelList ->
            this.dailyLabelList.clear()
            val sortedBy = dailyLabelList.sortedBy { it.label?.lowercase() }
            sortedBy.forEach {
                it.label?.let { it1 -> this.dailyLabelList.add(it1) }
            }
        }
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
            activityAddDailyBinding.ivMood.contentDescription = getString(ConstUtil.moodLabelList[position])
            moodIndex = position + 1
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
            activityAddDailyBinding.ivWeather.contentDescription = getString(ConstUtil.weatherLabelList[position])
            weatherIndex = position + 1
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
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    /**
     * 保存日记
     */
    private fun saveDaily() {
        // 将自动保存的数据清空
        autoSaveDailySharedPreferences(
            this, 1, "", "", 0, 0, "", 0, 0, "", false, "", false, false
        )
        sharedPreferences.edit { putLong("switch_preference_auto_save_id", 0) }
        isSystemExit = false

        dailyViewModel.insertDaily(
                DailyEntity(
                    title = activityAddDailyBinding.inputTitle.text.toString(),
                    content = dailyTextInputEdit.getExportText(),
                    dateTime = DateUtil.dateStringToDate(
                        activityAddDailyBinding.tvDateTime.text.toString(),
                        0
                    ),
                    backgroundColorIndex = backgroundColorIndex,
                    singlePassword = HashUtil.hashSHA256(singlePassword.toString()),
                    moodIndex = moodIndex,
                    weatherIndex = weatherIndex,
                    dailyUUID = dailyUuid,
                    dailyLabel = dailyLabel,
                    monthDay = DateUtil.dateStringToDate(
                        activityAddDailyBinding.tvDateTime.text.toString(), 0
                    ).toMonthDay()
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
        val isDailyFragment = intent.getBooleanExtra("isDailyFragment", true)
        val selectedYearMonthDay = intent.getStringExtra("selectedYearMonthDay")

        setStringChangedListener(object : StringChangedListener {
            override fun onStringChanged(newString: String) {
                activityAddDailyBinding.tvDateTime.text = newString
            }

        })

        val dateString = DateUtil.getDateString(
            0,
            DateUtil.getCurrentDate()
        )

        val selectedDateString = "$selectedYearMonthDay ${
            DateUtil.getDateString(
                2,
                DateUtil.getCurrentDate()
            )
        }"

        val defaultDateTime =
            if (isDailyFragment || selectedYearMonthDay.isNullOrEmpty()) "$dateString ${
                DateUtil.getWeek(
                    this,
                    dateString
                )
            }"
            else "$selectedYearMonthDay ${
                DateUtil.getDateString(
                    2,
                    DateUtil.getCurrentDate()
                )
            }  ${DateUtil.getWeek(this, selectedDateString)}"

        stringChangedListener.onStringChanged(defaultDateTime)
    }

    /**
     * 选择日期时间
     */
    private fun selectDateTime() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.select_date))
                .setSelection(
                    MaterialDatePicker.todayInUtcMilliseconds()
                ).build()

        val time = DateUtil.getDateString(2, DateUtil.getCurrentDate())
        val hour = time.split(":")[0].toInt()
        val minute = time.split(":")[1].toInt()
        val timePicker =
            MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(hour)
                .setMinute(minute)
                .setTitleText(getString(R.string.select_time)).build()

        activityAddDailyBinding.tvDateTime.setOnClickListener {
            datePicker.show(supportFragmentManager, datePicker.tag)
        }

        var newSelectedDateString = ""
        var newSelectedTime = ""
        datePicker.addOnPositiveButtonClickListener {
            newSelectedDateString = datePicker.headerText.replace(
                Regex("[年月]"), if (getString(R.string.daily) == "日记") "/" else "-"
            )
            newSelectedDateString = newSelectedDateString.replace("日", "")
            newSelectedDateString = newSelectedDateString.formatDateString()

            timePicker.show(supportFragmentManager, timePicker.tag)
        }

        timePicker.addOnPositiveButtonClickListener { _ ->
            val hour = timePicker.hour
            val minute = timePicker.minute
            newSelectedTime = "%02d:%02d".format(hour, minute)

            val week = DateUtil.getWeek(this, "$newSelectedDateString $newSelectedTime")

            stringChangedListener.onStringChanged(
                formatDateTimeWeek(
                    newSelectedDateString,
                    newSelectedTime,
                    week
                )
            )
        }
    }

    /**
     * 设置 [stringChangedListener].
     */
    private fun setStringChangedListener(stringChangedListener: StringChangedListener) {
        this.stringChangedListener = stringChangedListener
    }

    /**
     * 判断日记是否为空
     */
    private fun isDailyNull() {
        // 如果文本都为空，则直接退出
        val contentIsNotNull = contentIsNotNull()
        if (!contentIsNotNull) {
            isSystemExit = false
            finish()
            return
        }
        if (autoSave) {
            saveDaily()
            return
        }

        MaterialAlertDialogBuilder(this@AddDailyActivity).setMessage(getString(R.string.do_you_save_this_diary))
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                saveDaily()
            }.setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // 将自动保存的数据清空
                autoSaveDailySharedPreferences(
                    this, 1, "", "", 0, 0, "", 0, 0, "", false, "", false, false
                )
                isSystemExit = false
                notSaveToDeleteAppImage()
                notSaveToDeleteAppVideo()
                notSaveToDeleteAppAudio()
                finish()
            }.create().show()

    }

    private fun setOnEnabledChangedListener(onEnabledChangedListener: OnEnabledChangedListener) {
        this.onEnabledChangedListener = onEnabledChangedListener
    }

    private fun enableOnBack() {
        setOnEnabledChangedListener(object : OnEnabledChangedListener {
            override fun onEnableChanged(enable: Boolean) {
                onBackPressedCallback.isEnabled = enable
            }

        })
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
     * 不保存则删除应用私有目录下对应的视频
     */
    private fun notSaveToDeleteAppVideo() {
        CoroutineScope(Dispatchers.IO).launch {
            dailyViewModel.deletePathVideoByDailyUuid(dailyUuid)
            videoList.forEach { path ->
                FileUtil().deleteFile(path)
            }
        }
    }

    /**
     * 不保存则删除应用私有目录下对应的音频
     */
    private fun notSaveToDeleteAppAudio() {
        CoroutineScope(Dispatchers.IO).launch {
            dailyViewModel.deletePathAudioByDailyUuid(dailyUuid)
            audioList.forEach { path ->
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
            .isNotEmpty() || imageList.isNotEmpty() || videoList.isNotEmpty() || audioList.isNotEmpty()
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
        if (isSystemExit && autoSave && contentIsNotNull()) {
            autoSaveDailySharedPreferences(
                this, 0,
                activityAddDailyBinding.inputTitle.text.toString(),
                dailyTextInputEdit.getExportText(),
                DateUtil.dateStringToDate(
                    activityAddDailyBinding.tvDateTime.text.toString(),
                    0
                ),
                backgroundColorIndex,
                singlePassword.toString(),
                moodIndex,
                weatherIndex,
                dailyUuid,
                imageList.isNotEmpty(),
                dailyLabel,
                videoList.isNotEmpty(),
                audioList.isNotEmpty()
            )
        }
    }

    companion object {
        private var isSystemExit = false
    }

    /**
     * 添加启动器
     */
    private val addLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                try {
                    if (result.resultCode != Activity.RESULT_OK) return
                    val data = result.data ?: return
                    tempImageList.clear()
                    tempVideoList.clear()
                    tempAudioList.clear()
                    data.clipData?.let { clipData ->
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            selectUri(uri)
                        }
                    } ?: data.data?.let { uri ->
                        selectUri(uri)
                    }
                    if (tempImageList.isNotEmpty()) {
                        dailyViewModel.insertDailyImagePath(dailyUuid, tempImageList.toList())
                        dailyTextInputEdit.insertImages(tempImageList.toList())
                    }
                    if (tempVideoList.isNotEmpty()) {
                        dailyViewModel.insertDailyVideoPath(dailyUuid, tempVideoList.toList())
                        dailyTextInputEdit.insertVideos(tempVideoList.toList())
                    }
                    if (tempAudioList.isNotEmpty()) {
                        dailyViewModel.insertDailyAudioPath(dailyUuid, tempAudioList.toList())
                        dailyTextInputEdit.insertAudio(tempAudioList.toList())
                    }
                    tempImageList.clear()
                    tempVideoList.clear()
                    tempAudioList.clear()
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
     * 选择 uri
     *
     * @param uri 文件
     */
    private fun selectUri(uri: Uri) {
        val type = contentResolver.getType(uri)
        type?.let {
            when {
                it.startsWith("image/") -> {
                    addImage(uri)
                }

                it.startsWith("video/") -> {
                    addVideo(uri)
                }

                it.startsWith("audio/") -> {
                    addAudio(uri)
                }
            }
        }
    }

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

    /**
     * 添加视频
     *
     * @param uri 视频
     */
    private fun addVideo(uri: Uri) {
        val copyImageToMyAppDir =
            CopyUtil.copyVideoToMyAppDir(this@AddDailyActivity, uri)
        videoList.add(copyImageToMyAppDir)
        tempVideoList.add(copyImageToMyAppDir)
    }

    /**
     * 添加音频
     *
     * @param uri 音频
     */
    private fun addAudio(uri: Uri) {
        val copyAudioToMyAppDir = CopyUtil.copyAudioToMyAppDir(this, uri)
        audioList.add(copyAudioToMyAppDir)
        tempAudioList.add(copyAudioToMyAppDir)
    }

    /** 设置浮动富文本工具栏 */
    private fun setupFloatingRichTextToolbar() {
        val buttonActions =
            mapOf(
                R.id.action_btn_b to { dailyTextInputEdit.toggleBoldText() }, // 粗体
                R.id.action_btn_i to { dailyTextInputEdit.toggleItalicText() }, // 斜体
                R.id.action_btn_c to { toggleRichTextToolbarVisibility() }, // 切换富文本工具栏可见性
                R.id.action_btn_u to { dailyTextInputEdit.toggleUnderlineText() }, // 下划线
                R.id.action_btn_s to { dailyTextInputEdit.toggleStrikethroughText() }, // 删除线
                R.id.action_btn_tc to { showColorPickerDialog(false) }, // 文字颜色
                R.id.action_btn_bc to { showColorPickerDialog(true) }, // 背景高亮
                R.id.action_btn_size to { showFontPresetDialog() }, // 字号
                R.id.action_btn_title to { showHeadingDialog() }, // 标题样式
                R.id.action_btn_list to { showListDialog() }, // 列表与待办
                R.id.action_btn_quote to { dailyTextInputEdit.applyBlockQuote() }, // 引用
                R.id.action_btn_align_left to { dailyTextInputEdit.applyNormalAlignment() }, // 左对齐
                R.id.action_btn_align_center to { dailyTextInputEdit.applyCenterAlignment() }, // 居中对齐
                R.id.action_btn_h to { dailyTextInputEdit.insertHorizontalRule() }, // 分割线
                R.id.action_btn_link to { showLinkDialog() }) // 超链接

        buttonActions.forEach { (id, action) ->
            findViewById<MaterialButton>(id).setOnClickListener {
                action.invoke()
            }
        }
    }

    /** 切换富文本工具栏可见性 */
    private fun toggleRichTextToolbarVisibility() {
        with(activityAddDailyBinding.rtaContainer.floatingToolbarLayout) {
            if (isGone) {
                setVisibility(true)
            } else {
                setVisibility(false)
            }
        }
    }

    /** 显示颜色选择器对话框 */
    private fun showColorPickerDialog(isHighlight: Boolean) {
        var selectedColor = if (isHighlight) "#FFF59D".toColorInt() else Color.BLACK
        val colorPickerLayout = layoutInflater.inflate(R.layout.color_picker_layout, null)
        val colorPickerView =
            colorPickerLayout.findViewById<ColorPickerView>(R.id.color_picker_view)
        colorPickerView.onColorChanged = { color ->
            selectedColor = color
        }
        MaterialAlertDialogBuilder(this).setTitle(if (isHighlight) getString(R.string.highlight) else getString(
            R.string.text_color
        ))
            .setView(colorPickerLayout).setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                if (isHighlight) {
                    dailyTextInputEdit.applyHighlightColor(selectedColor)
                } else {
                    dailyTextInputEdit.applyTextColor(selectedColor)
                }
            }.show()
    }

    /** 显示字体对话框 */
    private fun showFontPresetDialog() {
        val items = arrayOf(getString(R.string.small), getString(R.string.body), getString(R.string.title))
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.text_font_size)).setItems(items) { _, which ->
            val preset = when (which) {
                0 -> DailyRichText.FontPreset.SMALL
                1 -> DailyRichText.FontPreset.BODY
                else -> DailyRichText.FontPreset.TITLE
            }
            dailyTextInputEdit.applyFontPreset(preset)
        }.show()
    }

    /** 显示标题层级对话框 */
    private fun showHeadingDialog() {
        val items = arrayOf("H1", "H2", "H3")
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.heading_level)).setItems(items) { _, which ->
            dailyTextInputEdit.applyHeading(which + 1)
        }.show()
    }

    /** 显示列表与待办对话框 */
    private fun showListDialog() {
        val items = arrayOf(getString(R.string.bullet_list),
            getString(R.string.ordered_list), getString(R.string.todo_list))
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.list_and_todo)).setItems(items) { _, which ->
            when (which) {
                0 -> dailyTextInputEdit.toggleBulletList()
                1 -> dailyTextInputEdit.toggleOrderedList()
                2 -> dailyTextInputEdit.toggleTodoList()
            }
        }.show()
    }

    /** 显示超链接对话框 */
    private fun showLinkDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_label_layout, null)
        val inputLayout = dialogView.findViewById<TextInputLayout>(R.id.input_label_layout)
        val input = dialogView.findViewById<TextInputEditText>(R.id.input_label)
        inputLayout.hint = getString(R.string.hyperlink)
        input.setText("https://")

        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.insert_hyperlink)).setView(dialogView)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                val url = input.text?.toString()?.trim().orEmpty()
                if (url.isNotEmpty()) {
                    dailyTextInputEdit.applyLink(url)
                }
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BACK_GROUND_COLOR_INDEX, backgroundColorIndex)
        outState.putInt(MOOD_INDEX, moodIndex)
        outState.putInt(TEMP_MOOD_INDEX, tempMoodIndex)
        outState.putInt(WEATHER_INDEX, weatherIndex)
        outState.putInt(TEMP_WEATHER_INDEX, tempWeatherIndex)
        outState.putString(DAILY_LABEL, dailyLabel)
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
                if (moodIndex == 0) setMoodIcon(ConstUtil.moodList.size - 1) else setMoodIcon(
                    moodIndex - 1
                )
            }

            val weatherIndex = savedInstanceState.getInt(WEATHER_INDEX)
            val tempWeatherIndex = savedInstanceState.getInt(TEMP_WEATHER_INDEX)
            if (tempWeatherIndex != 0) {
                if (weatherIndex == 0) setWeatherIcon(ConstUtil.weatherList.size - 1) else setWeatherIcon(
                    weatherIndex - 1
                )
            }

            dailyLabel = savedInstanceState.getString(DAILY_LABEL) ?: ""
            activityAddDailyBinding.tvLabel.text = dailyLabel
            activityAddDailyBinding.lLabel.visibility = if (dailyLabel.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (drawImageName.isEmpty()) return
        tempImageList.clear()
        val imageDir = "${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/$drawImageName"
        imageList.add(imageDir)
        tempImageList.add(imageDir)
        if (tempImageList.isNotEmpty()) {
            dailyViewModel.insertDailyImagePath(dailyUuid, tempImageList.toList())
            dailyTextInputEdit.insertImages(tempImageList.toList())
        }
        drawImageName = ""
        tempImageList.clear()
    }
}
