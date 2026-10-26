package com.liuxing.daily.ui.add

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.adapter.SelectDailyLabelAdapter
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.extension.getExternalPicturesFilesDir
import com.liuxing.daily.extension.toMonthDay
import com.liuxing.daily.listener.OnEnabledChangedListener
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.StringChangedListener
import com.liuxing.daily.ui.compose.theme.DailyTheme
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.draw.DrawImageActivity
import com.liuxing.daily.ui.settings.DailySettingsConst
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.ImageUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.SharedPreferencesUtil.autoSaveDailySharedPreferences
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
import java.util.Date
import java.util.UUID

private const val BACK_GROUND_COLOR_INDEX = "backgroundColorIndex"
private const val MOOD_INDEX = "moodIndex"
private const val WEATHER_INDEX = "weatherIndex"
private const val DAILY_LABEL = "dailyLabel"
private const val SELECT_IMAGE = 0
private const val TAKE_IMAGE = 1

class AddDailyActivity : AppCompatActivity() {

    private lateinit var dailyViewModel: DailyViewModel
    private var dailyUuid = UUID.randomUUID().toString()
    private val imageList = mutableSetOf<String>()
    private val videoList = mutableSetOf<String>()
    private val audioList = mutableSetOf<String>()
    private val tempImageList = mutableSetOf<String>()
    private val tempVideoList = mutableSetOf<String>()
    private val tempAudioList = mutableSetOf<String>()
    private lateinit var dailyTextInputEdit: DailyTextInputEdit
    private var dailyLabelList = mutableListOf<String>()
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

    private val addDailyViewModel by lazy {
        ViewModelProvider(this)[AddDailyViewModel::class.java]
    }

    private val isAutoStartDrawActivity by lazy {
        intent.getBooleanExtra("startDrawActivity", false)
    }

    private var isFirstDraw = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)

        DailyThemeManager.syncFromPreferences(this)

        initViewModel()
        if (savedInstanceState == null) {
            setDateTime()
            loadLabel()
        }

        if (savedInstanceState == null && isAutoStartDrawActivity) {
            startDrawActivity()
        }

        setContent {
            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                AddDailyScreen(
                    uiState = addDailyViewModel.uiState,
                    onBack = { isDailyNull() },
                    onSave = { saveDaily() },
                    onTitleChange = {
                        val limitedTitle = checkedTitleLength(it)
                        addDailyViewModel.updateTitle(limitedTitle)
                        val totalCount =
                            limitedTitle.length + (if (::dailyTextInputEdit.isInitialized) dailyTextInputEdit.getWordCount() else 0)
                        addDailyViewModel.updateWordCount(totalCount)
                        val hasChanges = contentIsNotNull()
                        addDailyViewModel.updateCanSave(hasChanges)
                        onBackPressedCallback.isEnabled = hasChanges
                    },
                    onBoldClick = { dailyTextInputEdit.toggleBoldText() },
                    onItalicClick = { dailyTextInputEdit.toggleItalicText() },
                    onListClick = { dailyTextInputEdit.toggleBulletList() },
                    onOrderedListClick = { dailyTextInputEdit.toggleOrderedList() },
                    onTodoListClick = { dailyTextInputEdit.toggleTodoList() },
                    onDateTimeClick = { selectDateTime() },
                    onMoodClick = { addDailyViewModel.showMoodSheet(true) },
                    onWeatherClick = { addDailyViewModel.showWeatherSheet(true) },
                    onLabelClick = { showLabelDialog() },
                    onMoreFormattingClick = { addDailyViewModel.toggleFormattingBar() },
                    onLockClick = { showLockDialog() },
                    onDrawingClick = {
                        startDrawActivity()
                    },
                    onColorClick = { addDailyViewModel.showColorSheet(true) },
                    onUnderlineClick = { dailyTextInputEdit.toggleUnderlineText() },
                    onStrikethroughClick = { dailyTextInputEdit.toggleStrikethroughText() },
                    onTextColorClick = { showColorPickerDialog(false) },
                    onHighlightColorClick = { showColorPickerDialog(true) },
                    onTextSizeClick = { showFontPresetDialog() },
                    onHeadingClick = { showHeadingDialog() },
                    onQuoteClick = { dailyTextInputEdit.applyBlockQuote() },
                    onAlignLeftClick = { dailyTextInputEdit.applyNormalAlignment() },
                    onAlignCenterClick = { dailyTextInputEdit.applyCenterAlignment() },
                    onRuleClick = { dailyTextInputEdit.insertHorizontalRule() },
                    onLinkClick = { showLinkDialog() },
                    onAddImageClick = { showAddImageDialog() },
                    onAddVideoClick = { openVideoPicker() },
                    onAddAudioClick = { openAudioPicker() },
                    onTitleVisibilityClick = {
                        addDailyViewModel.updateTitleVisibility(!addDailyViewModel.uiState.isTitleVisible)
                    },
                    onColorDismiss = { addDailyViewModel.showColorSheet(false) },
                    onColorSelect = { position ->
                        addDailyViewModel.updateBackgroundColor(position)
                        onBackPressedCallback.isEnabled = contentIsNotNull()
                    },
                    onMoodDismiss = { addDailyViewModel.showMoodSheet(false) },
                    onMoodSelect = { position ->
                        setMoodIcon(position)
                        onBackPressedCallback.isEnabled = contentIsNotNull()
                    },
                    onWeatherDismiss = { addDailyViewModel.showWeatherSheet(false) },
                    onWeatherSelect = { position ->
                        setWeatherIcon(position)
                        onBackPressedCallback.isEnabled = contentIsNotNull()
                    },
                    editorFactory = { editor ->
                        dailyTextInputEdit = editor
                        setupEditor(editor)
                        editor.setEditContent(addDailyViewModel.uiState.content)
                    })
            }
        }

        // 默认隐藏标题输入框
        val showTitle =
            SharedPreferencesUtil.getBoolean(this, DailySettingsConst.TITLE_SWITCH_KEY, false)
        addDailyViewModel.updateTitleVisibility(showTitle)

        initData(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    /**
     * 设置日记编辑器
     *
     * @param editor 日记编辑器
     */
    private fun setupEditor(editor: DailyTextInputEdit) {
        editor.setImageDeletionListener(object :
            DailyTextInputEdit.ImageDeletionListener {
            override fun onImageDeleted(imagePath: String) {
                dailyViewModel.deleteSelectPathImage(imagePath)
                if (FileUtil().checkFileExists(imagePath)) {
                    FileUtil().deleteFile(imagePath)
                    imageList.remove(imagePath)
                }
            }
        })
        editor.setVideoDeletionListener(object :
            DailyTextInputEdit.VideoDeletionListener {
            override fun onVideoDeleted(videoPath: String) {
                if (FileUtil().checkFileExists(videoPath)) {
                    FileUtil().deleteFile(videoPath)
                    videoList.remove(videoPath)
                }
            }
        })
        editor.setAudioDeletionListener(object :
            DailyTextInputEdit.AudioDeletionListener {
            override fun onAudioDeleted(audioPath: String) {
                if (FileUtil().checkFileExists(audioPath)) {
                    FileUtil().deleteFile(audioPath)
                    audioList.remove(audioPath)
                }
            }
        })

        val textLineSpacingValue = sharedPreferences.getFloat("text_line_spacing_preference", 0F)
        editor.setLineSpacing(textLineSpacingValue, 1F)

        val textSize = sharedPreferences.getFloat(ConstUtil.TEXT_SIZE_KEY, 16F)
        editor.textSize = textSize
        addDailyViewModel.updateTitleFontSize(textSize + 4)
        addDailyViewModel.updateContentFontSize(textSize)

        editor.addTextChangedListener { _: android.text.Editable? ->
            addDailyViewModel.updateContent(editor.getExportText())
            // 计算标题 + 正文的总字数
            val totalCount = addDailyViewModel.uiState.title.length + editor.getWordCount()
            addDailyViewModel.updateWordCount(totalCount)
            val hasChanges = contentIsNotNull()
            addDailyViewModel.updateCanSave(hasChanges)
            onBackPressedCallback.isEnabled = hasChanges
        }

        // 初始状态更新
        val initialCount = addDailyViewModel.uiState.title.length + editor.getWordCount()
        addDailyViewModel.updateWordCount(initialCount)
        addDailyViewModel.updateCanSave(contentIsNotNull())
    }

    /**
     * 加载标签
     */
    private fun loadLabel() {
        val label = intent.getStringExtra(DAILY_LABEL) ?: ""
        if (label.isEmpty()) return
        addDailyViewModel.updateLabel(label)
        addDailyViewModel.showLabel(true)
    }

    /**
     * 初始化数据
     */
    private fun initData(savedInstanceState: Bundle?) {
        setDailyLabel()
        restoreIndex(savedInstanceState)
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    /**
     * 打开绘图 Activity.
     */
    private fun startDrawActivity() {
        val intent = Intent(this@AddDailyActivity, DrawImageActivity::class.java)
        addDrawImageLauncher.launch(intent)
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

        val uiState = addDailyViewModel.uiState
        dailyViewModel.insertDaily(
                DailyEntity(
                    title = uiState.title,
                    content = dailyTextInputEdit.getExportText(),
                    dateTime = DateUtil.dateStringToDate(uiState.dateTimeText, 0),
                    backgroundColorIndex = uiState.backgroundColorIndex,
                    singlePassword = if (uiState.singlePassword.isEmpty()) "" else HashUtil.hashSHA256(
                        uiState.singlePassword
                    ),
                    moodIndex = if (uiState.moodIndex == ConstUtil.moodList.size - 1) 0 else uiState.moodIndex + 1,
                    weatherIndex = if (uiState.weatherIndex == ConstUtil.weatherList.size - 1) 0 else uiState.weatherIndex + 1,
                    dailyUUID = dailyUuid,
                    dailyLabel = uiState.label,
                    monthDay = DateUtil.dateStringToDate(uiState.dateTimeText, 0).toMonthDay()
                )
            )
            finish()
    }

    /**
     * 监听返回键
     */
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
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
                addDailyViewModel.updateDateTime(newString)
            }
        })

        val dateString = DateUtil.getDateString(0, DateUtil.getCurrentDate())
        val selectedDateString =
            "$selectedYearMonthDay ${DateUtil.getDateString(2, DateUtil.getCurrentDate())}"

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
        val dateTimeString = addDailyViewModel.uiState.dateTimeText
        val initialDate = DateUtil.dateStringToDate(dateTimeString, 0)
        DateUtil.selectDateTime(this, Date(initialDate)) { newDateTime ->
            addDailyViewModel.updateDateTime(newDateTime)
            addDailyViewModel.updateCanSave(contentIsNotNull())
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
        val uiState = addDailyViewModel.uiState
        return uiState.title.trim().isNotEmpty() || dailyTextInputEdit.text!!.trim()
            .isNotEmpty() || imageList.isNotEmpty() || videoList.isNotEmpty() || audioList.isNotEmpty()
    }

    override fun onStart() {
        super.onStart()
        isSystemExit = true
    }

    override fun onStop() {
        super.onStop()
        if (isSystemExit && autoSave && contentIsNotNull()) {
            val uiState = addDailyViewModel.uiState
            autoSaveDailySharedPreferences(
                this,
                0,
                uiState.title,
                dailyTextInputEdit.getExportText(),
                DateUtil.dateStringToDate(uiState.dateTimeText, 0),
                uiState.backgroundColorIndex,
                uiState.singlePassword,
                if (uiState.moodIndex == ConstUtil.moodList.size - 1) 0 else uiState.moodIndex + 1,
                if (uiState.weatherIndex == ConstUtil.weatherList.size - 1) 0 else uiState.weatherIndex + 1,
                dailyUuid,
                imageList.isNotEmpty(),
                uiState.label,
                videoList.isNotEmpty(),
                audioList.isNotEmpty(),
                false
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val uiState = addDailyViewModel.uiState
        outState.putString("daily_uuid", dailyUuid)
        outState.putInt(BACK_GROUND_COLOR_INDEX, uiState.backgroundColorIndex)
        outState.putInt(MOOD_INDEX, uiState.moodIndex)
        outState.putInt(WEATHER_INDEX, uiState.weatherIndex)
        outState.putString(DAILY_LABEL, uiState.label)
    }

    /**
     * 恢复索引
     */
    private fun restoreIndex(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val restoredUuid = savedInstanceState.getString("daily_uuid", "")
            if (restoredUuid.isNotEmpty()) dailyUuid = restoredUuid

            val backgroundColorIndex = savedInstanceState.getInt(BACK_GROUND_COLOR_INDEX)
            addDailyViewModel.updateBackgroundColor(backgroundColorIndex)

            val moodIndex = savedInstanceState.getInt(MOOD_INDEX, -1)
            if (moodIndex != -1) addDailyViewModel.updateMood(moodIndex)

            val weatherIndex = savedInstanceState.getInt(WEATHER_INDEX, -1)
            if (weatherIndex != -1) addDailyViewModel.updateWeather(weatherIndex)

            val label = savedInstanceState.getString(DAILY_LABEL) ?: ""
            addDailyViewModel.updateLabel(label)
            addDailyViewModel.showLabel(label.isNotEmpty())
        }
    }

    /**
     * 开启摄像头
     */
    private fun openCamera() {
        val externalPicturesFilesDir = getExternalPicturesFilesDir()
        val imageName = UUID.randomUUID().toString() + ".jpg"
        val file = File(externalPicturesFilesDir, imageName)
        imagePath = file.absolutePath
        val imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

    internal val addDrawImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val wasFirstDraw = isFirstDraw
            isFirstDraw = false
            if (result.resultCode == RESULT_OK) {
                drawImageName = result.data?.getStringExtra(ImageUtil.DRAW_IMAGE) ?: ""
                if (drawImageName.isNotEmpty()) {
                    val imageDir =
                        "${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/$drawImageName"
                    tempImageList.clear()
                    imageList.add(imageDir)
                    tempImageList.add(imageDir)
                    if (tempImageList.isNotEmpty()) {
                        dailyViewModel.insertDailyImagePath(dailyUuid, tempImageList.toList())
                        dailyTextInputEdit.insertImages(tempImageList.toList())
                    }
                    drawImageName = ""
                }
            } else if (isAutoStartDrawActivity && wasFirstDraw) {
                // 如果是自动启动绘画且在第一次绘画时点取消，则直接退出添加界面
                finish()
            }
        }

    /** 显示颜色选择器对话框 */
    private fun showColorPickerDialog(isHighlight: Boolean) {
        var selectedColor = if (isHighlight) "#FFF59D".toColorInt() else Color.BLACK
        val colorPickerLayout = layoutInflater.inflate(R.layout.color_picker_layout, null)
        val colorPickerView =
            colorPickerLayout.findViewById<ColorPickerView>(R.id.color_picker_view)
        colorPickerView.onColorChanged = { color -> selectedColor = color }
        MaterialAlertDialogBuilder(this).setTitle(
            if (isHighlight) getString(R.string.highlight) else getString(
                R.string.text_color
            )
        )
            .setView(colorPickerLayout).setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                if (isHighlight) dailyTextInputEdit.applyHighlightColor(selectedColor)
                else dailyTextInputEdit.applyTextColor(selectedColor)
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
                if (url.isNotEmpty()) dailyTextInputEdit.applyLink(url)
            }.show()
    }

    /**
     * 显示上锁对话框
     */
    private fun showLockDialog() {
        val currentPassword = addDailyViewModel.uiState.singlePassword
        // 如果还没有设置过找回密码的 Key，先引导设置 Key
        if (sharedPreferences.getString("forget_password_key", "") == "") {
            val inflate = layoutInflater.inflate(R.layout.dialog_input_password_layout, null)
            val inputPasswordLayout =
                inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
            val inputPassword = inflate.findViewById<TextInputEditText>(R.id.input_password)
            inputPasswordLayout.hint = getString(R.string.key)
            MaterialAlertDialogBuilder(this).apply {
                setTitle(getString(R.string.key))
                setView(inflate)
                setPositiveButton(getString(R.string.sure)) { _, _ ->
                    val key = inputPassword.text.toString()
                    if (key.isNotEmpty()) {
                        sharedPreferences.edit {
                            putString("forget_password_key", HashUtil.hashSHA256(key))
                        }
                        // 设置完 Key 后再次弹出设置密码的窗口
                        showLockDialog()
                    }
                }
                setNeutralButton(getString(R.string.cancel), null)
                setCancelable(false)
                create()
                show()
            }
            return
        }

        val inflate = layoutInflater.inflate(R.layout.dialog_input_password_layout, null)
        val inputPasswordLayout = inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
        val inputPassword = inflate.findViewById<TextInputEditText>(R.id.input_password)
        inputPasswordLayout.hint = getString(R.string.locked)
        inputPassword.setText(currentPassword)
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.locked))
            setView(inflate)
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                addDailyViewModel.updateSinglePassword(inputPassword.text.toString())
                onBackPressedCallback.isEnabled = contentIsNotNull()
            }
            setNeutralButton(getString(R.string.cancel), null)
            setCancelable(false)
            create()
            show()
        }
    }

    /**
     * 显示所有标签对话框
     */
    private fun showLabelDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_select_daily_label_layout, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val selectDailyLabelAdapter = SelectDailyLabelAdapter(dailyLabelList)
        recyclerView.adapter = selectDailyLabelAdapter
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.label))
            setView(view)
            setPositiveButton(getString(R.string.not_add)) { _, _ ->
                addDailyViewModel.updateLabel("")
                addDailyViewModel.showLabel(false)
                onBackPressedCallback.isEnabled = contentIsNotNull()
            }
            setNegativeButton(getString(R.string.new_label)) { _, _ -> showLabelInputDialog() }
            setNeutralButton(getString(R.string.cancel), null)
            val dialog = create()
            dialog.show()
            selectDailyLabelAdapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(position: Int) {
                    addDailyViewModel.updateLabel(dailyLabelList[position])
                    addDailyViewModel.showLabel(true)
                    onBackPressedCallback.isEnabled = contentIsNotNull()
                    dialog.dismiss()
                }
            })
        }
    }

    /**
     * 显示输入标签的对话框
     */
    private fun showLabelInputDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_input_label_layout, null)
        val inputLabel = view.findViewById<TextInputEditText>(R.id.input_label)
        val inputLabelLayout = view.findViewById<TextInputLayout>(R.id.input_label_layout)
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog).apply {
            setTitle(getString(R.string.create_label))
            setView(view)
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                val label = inputLabel.text.toString()
                if (label.isNotEmpty()) addDailyLabel(label)
            }
            setNegativeButton(getString(R.string.cancel), null)
            val dialog = create()
            dialog.show()
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
            inputLabel.addTextChangedListener { s: android.text.Editable? ->
                val inputText = s.toString()
                val exists = dailyLabelList.contains(inputText)
                inputLabelLayout.error = when {
                    inputText.isEmpty() -> getString(R.string.the_label_is_empty)
                    exists -> getString(R.string.the_label_already_exists)
                    else -> null
                }
                positiveButton.isEnabled = inputText.isNotEmpty() && !exists
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
        dailyViewModel.queryAllDailyLabel().observe(this) { list ->
            this.dailyLabelList.clear()
            this.dailyLabelList.addAll(list.mapNotNull { it.label }.sortedBy { it.lowercase() })
        }
    }

    /**
     * 设置心情图标
     */
    private fun setMoodIcon(position: Int) {
        addDailyViewModel.updateMood(position)
    }

    /**
     * 设置天气图标
     */
    private fun setWeatherIcon(position: Int) {
        addDailyViewModel.updateWeather(position)
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
                            selectUri(clipData.getItemAt(i).uri)
                        }
                    } ?: data.data?.let { selectUri(it) }
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
                } catch (e: Exception) {
                    MaterialAlertDialogBuilder(this@AddDailyActivity).setMessage(getString(R.string.add_failed))
                        .setPositiveButton(getString(R.string.sure), null).show()
                }
            }
        })

    /**
     * 选择 uri
     *
     * @param uri 文件
     */
    private fun selectUri(uri: Uri) {
        val type = contentResolver.getType(uri) ?: return
        when {
            type.startsWith("image/") -> {
                val path = CopyUtil.copyImageToMyAppDir(this, uri)
                imageList.add(path)
                tempImageList.add(path)
            }

            type.startsWith("video/") -> {
                val path = CopyUtil.copyVideoToMyAppDir(this, uri)
                videoList.add(path)
                tempVideoList.add(path)
            }

            type.startsWith("audio/") -> {
                val path = CopyUtil.copyAudioToMyAppDir(this, uri)
                audioList.add(path)
                tempAudioList.add(path)
            }
        }
    }

    /** 打开视频选择器 */
    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        addLauncher.launch(intent)
    }

    /** 打开音频选择器 */
    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        addLauncher.launch(intent)
    }

    /**
     * 显示添加图片对话框
     *
     * [SELECT_IMAGE] 选择图片,
     * [TAKE_IMAGE] 拍摄图片
     */
    private fun showAddImageDialog() {
        val items = arrayOf(getString(R.string.select_image), getString(R.string.take_image))
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.add_image))
            .setItems(items) { _, which ->
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

                    TAKE_IMAGE -> openCamera()
                }
            }.setPositiveButton(getString(R.string.cancel), null).show()
    }

    /**
     * 检查标题长度
     *
     * @param title 标题
     *
     * @return 检查后的标题
     */
    private fun checkedTitleLength(title: String): String {
        return StringUtil.checkStringLength(title, 30)
    }

    companion object {
        private var isSystemExit = true
    }
}
