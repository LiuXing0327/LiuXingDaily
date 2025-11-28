package com.liuxing.daily.ui.edit

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
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
import com.liuxing.daily.adapter.SelectDailyLabelAdapter
import com.liuxing.daily.adapter.WeatherAdapter
import com.liuxing.daily.databinding.ActivityEditDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.extension.setVisibility
import com.liuxing.daily.listener.OnEnabledChangedListener
import com.liuxing.daily.listener.OnItemClickListener
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
import com.liuxing.daily.view.DailyTextInputEdit
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Objects

private const val BACK_GROUND_COLOR_INDEX = "backgroundColorIndex"
private const val MOOD_INDEX = "moodIndex"
private const val TEMP_MOOD_INDEX = "tempMoodIndex"
private const val WEATHER_INDEX = "weatherIndex"
private const val TEMP_WEATHER_INDEX = "tempWeatherIndex"
private const val DAILY_LABEL = "dailyLabel"

class EditDailyActivity : AppCompatActivity() {

    private lateinit var activityEditDailyBinding: ActivityEditDailyBinding
    private var backgroundColorIndex: Int = 0
    private lateinit var dailyViewModel: DailyViewModel
    private var singlePassword: String? = ""
    private var moodIndex = 0
    private var tempMoodIndex = 0
    private var weatherIndex = 0
    private var tempWeatherIndex = 0
    private var dailyUuid: String = ""
    private val imageList = mutableSetOf<String>()
    private var originalImageListIndex: Int = 0
    private val originalImageList = mutableSetOf<String>()
    private val tempImageList = mutableSetOf<String>()
    private val tempImageList2 = mutableSetOf<String>()
    private lateinit var dailyTextInputEdit: DailyTextInputEdit
    private val deleteImageList = mutableSetOf<String>()
    private var dailyLabel: String = ""
    private var dailyLabelList = mutableListOf<String>()
    private val videoList = mutableSetOf<String>()
    private var originalVideoIndex: Int = 0
    private val originalVideoList = mutableSetOf<String>()
    private val tempVideoList = mutableSetOf<String>()
    private val tempVideoList2 = mutableSetOf<String>()
    private val deleteVideoList = mutableSetOf<String>()
    private val audioList = mutableSetOf<String>()
    private var originalAudioIndex: Int = 0
    private val originalAudioList = mutableSetOf<String>()
    private val tempAudioList = mutableSetOf<String>()
    private val tempAudioList2 = mutableSetOf<String>()
    private val deleteAudioList = mutableSetOf<String>()
    private var drawImageName = ""
    private lateinit var onEnabledChangedListener: OnEnabledChangedListener
    private var isPinned = false
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val autoSave by lazy {
        sharedPreferences.getBoolean("switch_preference_auto_save", true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        activityEditDailyBinding = ActivityEditDailyBinding.inflate(layoutInflater)
        setContentView(activityEditDailyBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        initView()
        initData(savedInstanceState)
        // 添加返回键回调
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        SoftHideKeyBoardUtil(this)
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        dailyTextInputEdit = findViewById(R.id.input_content)
        dailyTextInputEdit.setImageDeletionListener(object :
            DailyTextInputEdit.ImageDeletionListener {
            override fun onImageDeleted(imagePath: String) {
                deleteImageList.add(imagePath)
            }
        })
        dailyTextInputEdit.setVideoDeletionListener(object  : DailyTextInputEdit.VideoDeletionListener{
            override fun onVideoDeleted(videoPath: String) {
                deleteVideoList.add(videoPath)
            }
        })
        dailyTextInputEdit.setAudioDeletionListener(object : DailyTextInputEdit.AudioDeletionListener{
            override fun onAudioDeleted(audioPath: String) {
                deleteAudioList.add(audioPath)
            }
        })

        val textLineSpacingValue = sharedPreferences.getFloat("text_line_spacing_preference", 0F)
        dailyTextInputEdit.setLineSpacing(textLineSpacingValue, 1F)

        val textSize = sharedPreferences.getFloat(ConstUtil.TEXT_SIZE_KEY, 16F)
        activityEditDailyBinding.inputTitle.textSize = textSize + 4
        dailyTextInputEdit.textSize = textSize

        enableOnBack()
        onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
        activityEditDailyBinding.inputTitle.addTextChangedListener {
            onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
        }
        activityEditDailyBinding.inputContent.addTextChangedListener {
            onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
        }

        // 默认隐藏标题输入框
        val showTitle =
            SharedPreferencesUtil.getBoolean(
                this,
                DailySettingsConst.TITLE_SWITCH_KEY,
                false
            ) || !getDailyTitle().isNullOrEmpty()
        activityEditDailyBinding.inputTitleContainer?.setVisibility(showTitle)
    }

    /**
     * 初始化数据
     */
    private fun initData(savedInstanceState: Bundle?) {
        setActionBar()
        initViewModel()
        setDailyTitle()
        setDailyContent()
        setDailyDateTime()
        setDailyCount()
        setDailySinglePassword()
        setDailyMoodIndex()
        setDailyWeatherIndex()
        setDailyUuid()
        getDailyImage()
        getDailyVideo()
        getDailyAudio()
        updateDailyCount()
        initMenu()
        setDailyBackgroundColor(getDailyBackgroundColorIndex())
        setDailyBackgroundColorIndex()
        checkedTitleLength()
        restoreIndex(savedInstanceState)
        setDailyLabel()
        getDailyLabels()
        setIsPinned()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(activityEditDailyBinding.toolbar)
        this.supportActionBar?.setDisplayShowTitleEnabled(false)
        this.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * 获取日记ID
     */
    private fun getDailyId(): Long = intent.getLongExtra("daily_id", 0)

    /**
     * 获取日记标题
     */
    private fun getDailyTitle(): String? = intent.getStringExtra("daily_title")

    /**
     * 设置日记标题
     */
    private fun setDailyTitle() = activityEditDailyBinding.inputTitle.setText(getDailyTitle())

    /**
     * 获取日记内容
     */
    private fun getDailyContent(): String? = intent.getStringExtra("daily_content")

    /**
     * 设置日记内容
     */
    private fun setDailyContent() {
        dailyTextInputEdit.setText(getDailyContent())
        dailyTextInputEdit.setEditContent(getDailyContent().toString())
        dailyTextInputEdit.setSelection(dailyTextInputEdit.text.toString().length)
    }

    /**
     * 获取日记日期时间
     */
    private fun getDailyDateTime(): String =
        DateUtil.getDateString(0, Date(intent.getLongExtra("daily_date_time", 0)))

    /**
     * 设置日记日期时间
     */
    private fun setDailyDateTime() {
        val showWeek =
            SharedPreferencesUtil.getBoolean(this, DailySettingsConst.WEEK_SWITCH_KEY, true)
        val dailyDateTime = getDailyDateTime()
        activityEditDailyBinding.tvDateTime.text = if (showWeek) "$dailyDateTime ${
            DateUtil.getWeek(
                this,
                dailyDateTime
            )
        }" else dailyDateTime
    }

    /**
     * 设置日记字数
     */
    private fun setDailyCount() {
        "${activityEditDailyBinding.inputTitle.text!!.length.plus(dailyTextInputEdit.getWordCount())}${getString(R.string.word)}".also {
            activityEditDailyBinding.tvDailyCount.text = it
        }
    }

    /**
     * 更新日记字数
     */
    private fun updateDailyCount() {
        activityEditDailyBinding.inputTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setDailyCount()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
        dailyTextInputEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setDailyCount()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    /**
     * 获取日记背景颜色索引
     */
    private fun getDailyBackgroundColorIndex(): Int =
        intent.getIntExtra("daily_backgroundColorIndex", 0)

    /**
     * 设置日记背景颜色索引
     */
    private fun setDailyBackgroundColorIndex() {
        backgroundColorIndex = getDailyBackgroundColorIndex()
    }

    /**
     * 设置日记背景颜色
     */
    private fun setDailyBackgroundColor(index: Int) {
        activityEditDailyBinding.main.setBackgroundColor(
            ContextCompat.getColor(
                this,
                ConstUtil.backgroundColorList[index]
            )
        )
        activityEditDailyBinding.toolbar.setBackgroundColor(
            ContextCompat.getColor(
                this,
                ConstUtil.backgroundColorList[index]
            )
        )

    }

    /**
     * 获取日记密码
     */
    private fun getDailySinglePassword(): String =
        intent.getStringExtra("single_password").toString()

    /**
     * 设置日记密码
     */
    private fun setDailySinglePassword() =
        getDailySinglePassword().also { this.singlePassword = it }

    /**
     * 获取日记心情
     */
    private fun getDailyMoodIndex() = intent.getIntExtra("mood_index", 0)

    /**
     * 设置日记心情
     */
    private fun setDailyMoodIndex() {
        moodIndex = getDailyMoodIndex()
        getDailyMoodIndex().let {
            if (it == 0) {
                tempMoodIndex = 0
                activityEditDailyBinding.ivMood.visibility =
                    View.GONE
            } else {
                tempMoodIndex = 1
                activityEditDailyBinding.ivMood.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        ConstUtil.moodList[getDailyMoodIndex().minus(1)]
                    )
                )
                activityEditDailyBinding.ivMood.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 获取日记心情
     */
    private fun getDailyWeatherIndex() = intent.getIntExtra("weather_index", 0)

    /**
     * 设置日记心情
     */
    private fun setDailyWeatherIndex() {
        weatherIndex = getDailyWeatherIndex()
        getDailyWeatherIndex().let {
            if (it == 0) {
                tempWeatherIndex = 0
                activityEditDailyBinding.ivWeather.visibility = View.GONE
            } else {
                tempWeatherIndex = 1
                activityEditDailyBinding.ivWeather.visibility = View.VISIBLE
                activityEditDailyBinding.ivWeather.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        ConstUtil.weatherList[getDailyWeatherIndex().minus(1)]
                    )
                )
            }
        }
    }

    /**
     * 获取日记Uuid
     */
    private fun getDailyUuid() = intent.getStringExtra("daily_uuid")

    /**
     * 设置日记Uuid
     */
    private fun setDailyUuid() = getDailyUuid()?.let { dailyUuid = it }

    /**
     * 获取日记图片路径
     */
    private fun getDailyImage() {
        dailyViewModel.queryDailyImageByUuid(getDailyUuid().toString()).observe(this) { dailyImageList ->
            val newImageList = mutableListOf<String>()
            dailyImageList.forEach { dailyImageEntity ->
                if (FileUtil().checkFileExists(dailyImageEntity.imagePath.toString())) {
                    newImageList.add(dailyImageEntity.imagePath.toString())
                    if (originalImageListIndex == 0) {
                        originalImageList.add(dailyImageEntity.imagePath.toString())
                        imageList.add(dailyImageEntity.imagePath.toString())
                    }
                } else {
                    dailyViewModel.deleteSelectPathImage(dailyImageEntity.imagePath.toString())
                }
            }
            if (dailyTextInputEdit.getOldImageList() != newImageList) {
                dailyTextInputEdit.setOldImageList(newImageList)
            }
        }
    }

    /**
     * 获取日记标签
     */
    private fun getDailyLabel() = intent.getStringExtra("daily_label")

    /**
     * 设置日记标签
     */
    private fun setDailyLabel() {
        dailyLabel = getDailyLabel() ?: ""
        setDailyLabel(dailyLabel)
    }

    /**
     * 获取日记视频
     */
    private fun getDailyVideo() {
        dailyViewModel.queryDailyVideoByUuid(getDailyUuid().toString())
            .observe(this) { dailyVideoList ->
                val newVideoList = mutableSetOf<String>()
                dailyVideoList.forEach { dailyVideoEntity ->
                    if (FileUtil().checkFileExists(dailyVideoEntity.videoPath.toString())) {
                        newVideoList.add(dailyVideoEntity.videoPath.toString())
                        if (originalVideoIndex == 0) {
                            originalVideoList.add(dailyVideoEntity.videoPath.toString())
                            videoList.add(dailyVideoEntity.videoPath.toString())
                        }
                    } else {
                        dailyViewModel.deleteSelectPathVideo(dailyVideoEntity.videoPath.toString())
                    }
                }
                if (dailyTextInputEdit.getOldVideoList() != newVideoList) {
                    dailyTextInputEdit.setOldVideoList(newVideoList.toList())
                }
            }
    }

    /**
     * 获取日记音频
     */
    private fun getDailyAudio() {
        dailyViewModel.queryDailyAudioByUuid(getDailyUuid().toString())
            .observe(this) { dailyAudioList ->
                val newAudioList = mutableSetOf<String>()
                dailyAudioList.forEach { dailyAudioEntity ->
                    if (FileUtil().checkFileExists(dailyAudioEntity.audioPath.toString())) {
                        newAudioList.add(dailyAudioEntity.audioPath.toString())
                        if (originalAudioIndex == 0) {
                            originalAudioList.add(dailyAudioEntity.audioPath.toString())
                            audioList.add(dailyAudioEntity.audioPath.toString())
                        }
                    } else {
                        dailyViewModel.deleteSelectPathVideo(dailyAudioEntity.audioPath.toString())
                    }
                }
                if (dailyTextInputEdit.getOldAudioList() != newAudioList) {
                    dailyTextInputEdit.setOldAudioList(newAudioList.toList())
                }
            }
    }

    /**
     * 获取置顶
     */
    private fun getIsPinned() = intent.getBooleanExtra("is_pinned", false)

    /**
     * 设置置顶
     */
    private fun setIsPinned() {
        isPinned = getIsPinned()
    }


    /**
     * 初始化菜单
     */
    private fun initMenu() {
        val menuHost = this
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_edit_daily, menu)
/*                when {
                    activityEditDailyBinding.inputTitle.text!!.trim()
                        .isEmpty() && dailyTextInputEdit.text!!.trim()
                        .isEmpty() -> {
                        menu.findItem(R.id.item_save).setVisible(false)
                        invalidateOptionsMenu()
                    }

                    else -> {
                        menu.findItem(R.id.item_save).setVisible(true)
                        invalidateOptionsMenu()
                    }
                }*/
                menu.findItem(R.id.item_lock_to_on_and_un_ed).title = when {
                    singlePassword == "" -> {
                        getString(R.string.locked)
                    }

                    else -> getString(R.string.unlocked)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> isDailyNullOrEquals()

                    R.id.item_change_background_color -> {
                        val view =
                            LayoutInflater.from(this@EditDailyActivity)
                                .inflate(R.layout.dialog_change_daily_card_color, null)
                        val materialAlertDialogBuilder =
                            MaterialAlertDialogBuilder(this@EditDailyActivity)
                        materialAlertDialogBuilder.setView(view)
                        val dialog = materialAlertDialogBuilder.create()
                        dialog.show()
                        val colorRecycler = view.findViewById<RecyclerView>(R.id.color_recycler)
                        colorRecycler.layoutManager = GridLayoutManager(this@EditDailyActivity, 3)
                        colorRecycler.adapter =
                            ChangeDailyCardColorAdapter(ConstUtil.backgroundColorList,backgroundColorIndex) { selectedColor, position ->
                                backgroundColorIndex = position
                                activityEditDailyBinding.main.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@EditDailyActivity,
                                        selectedColor
                                    )
                                )
                                activityEditDailyBinding.toolbar.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this@EditDailyActivity,
                                        selectedColor
                                    )
                                )

                                onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
                                dialog.dismiss()
                            }
                    }

                    R.id.item_save -> isDailyNullOrEquals()

                    R.id.item_delete -> {
                        MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
                            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                            setPositiveButton(getString(R.string.sure)) { dialog, which ->
                                val fileUtil = FileUtil()
                                dailyViewModel.queryDailyImageByUuid(dailyUuid)
                                    .observe(this@EditDailyActivity) { dailyImageList ->
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
                                    }
                                dailyViewModel.deletePathImageByDailyUuid(dailyUuid)
                                dailyViewModel.queryDailyVideoByUuid(dailyUuid).observe(this@EditDailyActivity) { dailyVideoList ->
                                    val existingVideoPaths = dailyVideoList.map { it.videoPath }.toSet()
                                    if (existingVideoPaths.isNotEmpty()) {
                                        val videoList = existingVideoPaths.toList()
                                        videoList.forEach {
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathVideoByDailyUuid(dailyUuid)
                                }
                                dailyViewModel.queryDailyAudioByUuid(dailyUuid).observe(this@EditDailyActivity) { dailyAudioList ->
                                    val existingAudioPaths = dailyAudioList.map { it.audioPath }.toSet()
                                    if (existingAudioPaths.isNotEmpty()) {
                                        val audioList = existingAudioPaths.toList()
                                        audioList.forEach {
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathAudioByDailyUuid(dailyUuid)
                                }
                                dailyViewModel.deleteDaily(
                                    DailyEntity(
                                        id = getDailyId(),
                                        title = activityEditDailyBinding.inputTitle.text.toString(),
                                        content = dailyTextInputEdit.text.toString(),
                                        dateTime = DateUtil.dateStringToDate(getDailyDateTime(), 0),
                                        backgroundColorIndex = backgroundColorIndex,
                                        dailyUUID = dailyUuid
                                    )
                                )
                                isSystemExit = false
                                finish()
                            }
                            setNegativeButton(getString(R.string.cancel), null)
                            create()
                            show()
                        }
                    }

                    R.id.item_lock_to_on_and_un_ed -> {

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
                                MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
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
                                                            activityEditDailyBinding.inputContent.rootView,
                                                            getString(R.string.please_enter_the_key_to_reset_your_password_if_you_forget_it)
                                                        )
                                                    }

                                                    else -> {
                                                        MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
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
                                            activityEditDailyBinding.inputContent.rootView,
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
                                if (singlePassword == "") inputPasswordLayout.hint =
                                    getString(R.string.locked) else inputPasswordLayout.hint =
                                    getString(R.string.unlocked)
                                inputPassword.setText(singlePassword)
                                MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
                                    if (singlePassword == "") setTitle(getString(R.string.locked)) else setTitle(
                                        getString(R.string.unlocked)
                                    )
                                    setView(inflate)
                                    setPositiveButton(
                                        getString(R.string.sure),
                                        object : DialogInterface.OnClickListener {
                                            override fun onClick(
                                                dialog: DialogInterface?,
                                                which: Int
                                            ) {
                                                onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
                                                singlePassword = inputPassword.text.toString()
                                            }

                                        })
                                    setNegativeButton(getString(R.string.forgot_password)) { dialog, which ->
                                        singlePassword = ""
                                    }
                                    setNeutralButton(getString(R.string.cancel), null)
                                    setCancelable(false)
                                    create()
                                    show()
                                }
                            }
                        }
                    }

                    R.id.item_mood -> {
                        val inflate =
                            LayoutInflater.from(this@EditDailyActivity)
                                .inflate(R.layout.dialog_mood_layout, null)
                        val recyclerView = inflate.findViewById<RecyclerView>(R.id.recycler_view)
                        val gridLayoutManager = GridLayoutManager(this@EditDailyActivity, 3)
                        recyclerView.layoutManager = gridLayoutManager
                        val moodAdapter = MoodAdapter(this@EditDailyActivity)
                        recyclerView.adapter = moodAdapter
                        val dialog: AlertDialog?
                        MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
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
                        val inflate = LayoutInflater.from(this@EditDailyActivity)
                            .inflate(R.layout.dialog_weather_layout, null)
                        val recyclerView = inflate.findViewById<RecyclerView>(R.id.recycler_view)
                        val gridLayoutManager = GridLayoutManager(this@EditDailyActivity, 3)
                        recyclerView.layoutManager = gridLayoutManager
                        val weatherAdapter = WeatherAdapter(this@EditDailyActivity)
                        recyclerView.adapter = weatherAdapter
                        val dialog: AlertDialog
                        MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
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
                        addLauncher.launch(intent)
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
                        val linearLayoutManager = LinearLayoutManager(this@EditDailyActivity)
                        recyclerView.layoutManager = linearLayoutManager
                        val selectDailyLabelAdapter = SelectDailyLabelAdapter(dailyLabelList)
                        recyclerView.adapter = selectDailyLabelAdapter
                        MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
                            setTitle(getString(R.string.label))
                            setView(view)
                            setPositiveButton(getString(R.string.not_add)) { _, _ ->
                                dailyLabel = ""
                                activityEditDailyBinding.lLabel?.visibility = View.GONE
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
                                    activityEditDailyBinding.tvLabel?.text = dailyLabel
                                    activityEditDailyBinding.lLabel?.visibility = View.VISIBLE
                                    dialog.dismiss()
                                    onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
                                }

                            })
                        }
                    }

                    R.id.item_drawing -> {
                        val intent = Intent(this@EditDailyActivity, DrawImageActivity::class.java)
                        addDrawImageLauncher.launch(intent)
                    }
                }

                return true
            }

        })
    }

    private val addDrawImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                drawImageName = result.data?.getStringExtra("draw_image_name") ?: ""
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
     * 获取日记标签
     */
    private fun getDailyLabels() {
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
            activityEditDailyBinding.ivMood.visibility = View.GONE
        } else {
            activityEditDailyBinding.ivMood.setImageDrawable(
                ContextCompat.getDrawable(
                    this@EditDailyActivity,
                    ConstUtil.moodList[position]
                )
            )
            activityEditDailyBinding.ivMood.contentDescription = getString(ConstUtil.moodLabelList[position])
            moodIndex = position + 1
            tempMoodIndex = 1
            activityEditDailyBinding.ivMood.visibility = View.VISIBLE
        }
        onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
    }

    /**
     * 设置天气图标
     */
    private fun setWeatherIcon(position: Int) {
        if (position == ConstUtil.weatherList.size - 1) {
            weatherIndex = 0
            tempWeatherIndex = 0
            activityEditDailyBinding.ivWeather.visibility = View.GONE
        } else {
            activityEditDailyBinding.ivWeather.visibility = View.VISIBLE
            activityEditDailyBinding.ivWeather.setImageDrawable(
                ContextCompat.getDrawable(
                    this@EditDailyActivity,
                    ConstUtil.weatherList[position]
                )
            )
            activityEditDailyBinding.ivWeather.contentDescription = getString(ConstUtil.weatherLabelList[position])
            weatherIndex = position + 1
            tempWeatherIndex = 1
        }
        onEnabledChangedListener?.onEnableChanged(!originalAllContentEqualsCurrentContent())
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
                    MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
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
            CopyUtil.copyImageToMyAppDir(this@EditDailyActivity, uri)
        imageList.add(copyImageToMyAppDir)
        tempImageList.add(copyImageToMyAppDir)
        tempImageList2.add(copyImageToMyAppDir)
    }

    /**
     * 添加视频
     *
     * @param uri 视频
     */
    private fun addVideo(uri: Uri) {
        val copyVideoToMyAppDir =
            CopyUtil.copyVideoToMyAppDir(this@EditDailyActivity, uri)
        videoList.add(copyVideoToMyAppDir)
        tempVideoList.add(copyVideoToMyAppDir)
        tempVideoList2.add(copyVideoToMyAppDir)
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
        tempAudioList2.add(copyAudioToMyAppDir)
    }


    /**
     * 判断日记是否为空
     */
    private fun isDailyNullOrEquals() {
        // 如果文本都为空或未发生变化，则直接退出
        if (contentIsNull() || originalAllContentEqualsCurrentContent()) {
            isSystemExit = false
            finish()
            return
        }

        if (autoSave) {
            saveDaily()
            return
        }

        MaterialAlertDialogBuilder(this@EditDailyActivity)
            .setMessage(getString(R.string.do_you_save_this_diary))
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                saveDaily()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                isSystemExit = false
                notSaveToDeleteAppImage()
                notSaveToDeleteAppVideos()
                notSaveToDeleteAppAudios()
                finish()
            }
            .create()
            .show()
    }

    /**
     * 保存日记
     */
    private fun saveDaily() {
        // 保存更新后日记内容到SharedPreferences
        getSharedPreferences("DAILY_CONTENT_UPDATE", Context.MODE_PRIVATE).edit {
            putString("daily_update_content_$dailyUuid", dailyTextInputEdit.text.toString())
        }
        isSystemExit = false
        dailyViewModel.updateDaily(
            DailyEntity(
                id = getDailyId(),
                title = activityEditDailyBinding.inputTitle.text.toString(),
                content = dailyTextInputEdit.text.toString(),
                dateTime = DateUtil.dateStringToDate(getDailyDateTime(), 0),
                backgroundColorIndex = backgroundColorIndex,
                singlePassword = HashUtil.hashSHA256(singlePassword.toString()),
                moodIndex = moodIndex,
                weatherIndex = weatherIndex,
                dailyUUID = dailyUuid,
                dailyLabel = dailyLabel,
                isPinned = isPinned
            )
        )

        // 循环去除被删除的图片
        if (deleteImageList.isNotEmpty()) {
            deleteImageList.forEach { imagePath ->
                dailyViewModel.deleteSelectPathImage(imagePath)
                if (FileUtil().checkFileExists(imagePath)) {
                    FileUtil().deleteFile(imagePath)
                    imageList.remove(imagePath)
                }
            }
        }
        // 循环去除被删除的视频
        if (deleteVideoList.isNotEmpty()) {
            deleteVideoList.forEach { videoPath ->
                dailyViewModel.deleteSelectPathVideo(videoPath)
                if (FileUtil().checkFileExists(videoPath)) {
                    FileUtil().deleteFile(videoPath)
                    videoList.remove(videoPath)
                }
            }
        }
        // 循环去除被删除的音频
        if (deleteAudioList.isNotEmpty()) {
            deleteAudioList.forEach { audioPath ->
                dailyViewModel.deleteSelectPathAudio(audioPath)
                if (FileUtil().checkFileExists(audioPath)) {
                    FileUtil().deleteFile(audioPath)
                    audioList.remove(audioPath)
                }
            }
        }

        finish()
    }

    /**
     * 查询当前图片集合中与原始图片集合的不同元素
     */
    private fun findMissingElements(): Set<String> {
        return originalImageList.toSet().subtract(imageList.toSet())
    }

    /**
     * 查询当前视频集合中与原始视频集合的不同元素
     */
    private fun findMissingVideos(): Set<String> {
        return originalVideoList.toSet().subtract(videoList.toSet())
    }

    /**
     * 查询当前音频集合中与原始音频集合的不同元素
     */
    private fun findMissingAudios(): Set<String> {
        return originalAudioList.toSet().subtract(audioList.toSet())
    }

    /**
     * 不保存则删除应用私有目录下对应的图片
     */
    private fun notSaveToDeleteAppImage() {
        tempImageList2.forEach { notList ->
            CoroutineScope(Dispatchers.IO).launch {
                dailyViewModel.deleteSelectPathImage(notList)
                FileUtil().deleteFile(notList)
            }
        }
    }

    /**
     * 删除未保存的视频
     */
    private fun notSaveToDeleteAppVideos() {
        tempVideoList2.forEach { notList ->
            CoroutineScope(Dispatchers.IO).launch {
                dailyViewModel.deleteSelectPathVideo(notList)
                FileUtil().deleteFile(notList)
            }
        }
    }

    /**
     * 删除未保存的音频
     */
    private fun notSaveToDeleteAppAudios() {
        tempAudioList2.forEach { notList ->
            CoroutineScope(Dispatchers.IO).launch {
                dailyViewModel.deleteSelectPathAudio(notList)
                FileUtil().deleteFile(notList)
            }
        }
    }



    /**
     * 判断主要内容是否为空
     */
    private fun contentIsNull(): Boolean {
        return activityEditDailyBinding.inputTitle.text!!.trim()
            .isEmpty() && dailyTextInputEdit.text!!.trim()
            .isEmpty() && findMissingElements().isEmpty() && findMissingVideos().isEmpty() && findMissingAudios().isEmpty()
    }

    /**
     * 判断所有原始与当前所有内容相同
     */
    private fun originalAllContentEqualsCurrentContent(): Boolean {
        return Objects.equals(
            activityEditDailyBinding.inputTitle.text.toString(),
            getDailyTitle()
        ) && Objects.equals(
            dailyTextInputEdit.text.toString(),
            getDailyContent()
        ) && backgroundColorIndex == getDailyBackgroundColorIndex() && Objects.equals(
            singlePassword,
            getDailySinglePassword()
        ) && moodIndex == getDailyMoodIndex() && weatherIndex == getDailyWeatherIndex() && tempImageList2.isEmpty() && tempAudioList2.isEmpty() && tempVideoList2.isEmpty() && dailyLabel == getDailyLabel()
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    /**
     * 监听返回键
     */
    private var onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isDailyNullOrEquals()
            }

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
     * 检查标题长度
     */
    private fun checkedTitleLength() =
        activityEditDailyBinding.inputTitle.addTextChangedListener { s ->
            StringUtil.checkedEditContentLength(
                s,
                30,
                activityEditDailyBinding.inputTitle
            )
        }

    override fun onStart() {
        super.onStart()
        isSystemExit = true
    }

    override fun onStop() {
        super.onStop()
        if (isSystemExit && autoSave && !contentIsNull() && !originalAllContentEqualsCurrentContent()) {
            val fileUtil = FileUtil()
            if (findMissingElements().isNotEmpty()) {
                findMissingElements().forEach {
                    if (fileUtil.checkFileExists(it)) fileUtil.deleteFile(it)
                    dailyViewModel.deleteSelectPathImage(it)
                }
            }
            if(findMissingVideos().isNotEmpty()){
                findMissingVideos().forEach{
                    if(fileUtil.checkFileExists(it)) fileUtil.deleteFile(it)
                    dailyViewModel.deleteSelectPathVideo(it)
                }
            }
            if(findMissingAudios().isNotEmpty()){
                findMissingAudios().forEach{
                    if(fileUtil.checkFileExists(it)) fileUtil.deleteFile(it)
                    dailyViewModel.deleteSelectPathAudio(it)
                }
            }
            sharedPreferences.edit {
                putLong("switch_preference_auto_save_id", getDailyId())
            }
            autoSaveDailySharedPreferences(
                this, 1,
                activityEditDailyBinding.inputTitle.text.toString(),
                dailyTextInputEdit.text.toString(),
                DateUtil.dateStringToDate(
                    activityEditDailyBinding.tvDateTime.text.toString(),
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
                audioList.isNotEmpty(),
                isPinned
            )
        }
    }

    companion object {
        private var isSystemExit = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BACK_GROUND_COLOR_INDEX, backgroundColorIndex)
        outState.putInt(MOOD_INDEX, moodIndex)
        outState.putInt(TEMP_MOOD_INDEX, tempMoodIndex)
        outState.putInt(WEATHER_INDEX, weatherIndex)
        outState.putInt(TEMP_WEATHER_INDEX, tempWeatherIndex)
        outState.putString(dailyLabel, dailyLabel)
    }

    /**
     * 恢复索引
     */
    private fun restoreIndex(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            backgroundColorIndex = savedInstanceState.getInt(BACK_GROUND_COLOR_INDEX)
            activityEditDailyBinding.main.setBackgroundColor(
                ContextCompat.getColor(
                    this@EditDailyActivity,
                    ConstUtil.backgroundColorList[backgroundColorIndex]
                )
            )
            activityEditDailyBinding.toolbar.setBackgroundColor(
                ContextCompat.getColor(
                    this@EditDailyActivity,
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
            setDailyLabel(dailyLabel)
        }
    }

    /**
     * 设置日记标签
     *
     * @param label 日记标签
     */
    private fun setDailyLabel(label: String) {
        activityEditDailyBinding.tvLabel.text = label
        activityEditDailyBinding.lLabel.visibility = if (label.isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
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