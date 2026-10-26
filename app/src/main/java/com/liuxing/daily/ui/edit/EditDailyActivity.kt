package com.liuxing.daily.ui.edit

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
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.StringChangedListener
import com.liuxing.daily.ui.add.AddDailyScreen
import com.liuxing.daily.ui.add.AddDailyViewModel
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
import com.liuxing.library.ColorPickerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.Objects
import java.util.UUID

private const val BACK_GROUND_COLOR_INDEX = "backgroundColorIndex"
private const val MOOD_INDEX = "moodIndex"
private const val WEATHER_INDEX = "weatherIndex"
private const val DAILY_LABEL = "dailyLabel"
private const val SELECT_IMAGE = 0
private const val TAKE_IMAGE = 1

class EditDailyActivity : AppCompatActivity() {

    private lateinit var dailyViewModel: DailyViewModel
    private var dailyUuid: String = ""
    private val imageList = mutableSetOf<String>()
    private var originalImageListIndex: Int = 0
    private val originalImageList = mutableSetOf<String>()
    private val tempImageList = mutableSetOf<String>()
    private lateinit var dailyTextInputEdit: DailyTextInputEdit
    private val deleteImageList = mutableSetOf<String>()
    private var dailyLabelList = mutableListOf<String>()
    private val videoList = mutableSetOf<String>()
    private var originalVideoIndex: Int = 0
    private val originalVideoList = mutableSetOf<String>()
    private val tempVideoList = mutableSetOf<String>()
    private val deleteVideoList = mutableSetOf<String>()
    private val audioList = mutableSetOf<String>()
    private var originalAudioIndex: Int = 0
    private val originalAudioList = mutableSetOf<String>()
    private val tempAudioList = mutableSetOf<String>()
    private val deleteAudioList = mutableSetOf<String>()
    private var drawImageName = ""
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val autoSave by lazy {
        sharedPreferences.getBoolean("switch_preference_auto_save", true)
    }
    private var monthDay = ""
    private lateinit var stringChangedListener: StringChangedListener
    private lateinit var imagePath: String

    private val editDailyViewModel by lazy {
        ViewModelProvider(this)[AddDailyViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)

        DailyThemeManager.syncFromPreferences(this)

        initViewModel()
        if (savedInstanceState == null) {
            setDailyUuid()
            // 初始化
            editDailyViewModel.initEditState(
                title = getDailyTitle() ?: "",
                content = getDailyContent() ?: "",
                dateTime = getDailyDateTime(),
                moodIndex = if (getDailyMoodIndex() > 0) getDailyMoodIndex() - 1 else ConstUtil.moodList.size - 1,
                weatherIndex = if (getDailyWeatherIndex() > 0) getDailyWeatherIndex() - 1 else ConstUtil.weatherList.size - 1,
                label = getDailyLabel() ?: "",
                backgroundColorIndex = getDailyBackgroundColorIndex(),
                isLocked = getDailySinglePassword().isNotEmpty(),
                isPinned = intent.getBooleanExtra("is_pinned", false),
                singlePassword = getDailySinglePassword()
            )
        }

        setContent {
            DailyTheme(
                themeType = DailyThemeManager.currentThemeType,
                themeMode = DailyThemeManager.themeMode,
                isAmoled = DailyThemeManager.isAmoled,
                dynamicColor = DailyThemeManager.isDynamicColor,
            ) {
                AddDailyScreen(
                    uiState = editDailyViewModel.uiState,
                    onBack = { isDailyNullOrEquals() },
                    onSave = { saveDaily() },
                    onTitleChange = {
                        // 检查标题长度并截断
                        val limitedTitle = checkedTitleLength(it)
                        editDailyViewModel.updateTitle(limitedTitle)
                        // 实时计算标题 + 正文的总字数
                        val totalCount =
                            limitedTitle.length + (if (::dailyTextInputEdit.isInitialized) dailyTextInputEdit.getWordCount() else 0)
                        editDailyViewModel.updateWordCount(totalCount)
                        val hasChanges = !originalAllContentEqualsCurrentContent()
                        editDailyViewModel.updateCanSave(hasChanges)
                        onBackPressedCallback.isEnabled = hasChanges
                    },
                    onBoldClick = { dailyTextInputEdit.toggleBoldText() },
                    onItalicClick = { dailyTextInputEdit.toggleItalicText() },
                    onListClick = { dailyTextInputEdit.toggleBulletList() },
                    onOrderedListClick = { dailyTextInputEdit.toggleOrderedList() },
                    onTodoListClick = { dailyTextInputEdit.toggleTodoList() },
                    onDateTimeClick = { selectDateTime() },
                    onMoodClick = { editDailyViewModel.showMoodSheet(true) },
                    onWeatherClick = { editDailyViewModel.showWeatherSheet(true) },
                    onLabelClick = { showLabelDialog() },
                    onMoreFormattingClick = { editDailyViewModel.toggleFormattingBar() },
                    onLockClick = { showLockDialog() },
                    onDrawingClick = {
                        val intent = Intent(this@EditDailyActivity, DrawImageActivity::class.java)
                        addDrawImageLauncher.launch(intent)
                    },
                    onColorClick = { editDailyViewModel.showColorSheet(true) },
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
                    onColorDismiss = { editDailyViewModel.showColorSheet(false) },
                    onColorSelect = { position ->
                        editDailyViewModel.updateBackgroundColor(position)
                        onBackPressedCallback.isEnabled = !originalAllContentEqualsCurrentContent()
                    },
                    onMoodDismiss = { editDailyViewModel.showMoodSheet(false) },
                    onMoodSelect = { position ->
                        setMoodIcon(position)
                        onBackPressedCallback.isEnabled = !originalAllContentEqualsCurrentContent()
                    },
                    onWeatherDismiss = { editDailyViewModel.showWeatherSheet(false) },
                    onWeatherSelect = { position ->
                        setWeatherIcon(position)
                        onBackPressedCallback.isEnabled = !originalAllContentEqualsCurrentContent()
                    },
                    onTitleVisibilityClick = {
                        editDailyViewModel.updateTitleVisibility(!editDailyViewModel.uiState.isTitleVisible)
                    },
                    onDeleteClick = { showDeleteConfirmationDialog() },
                    editorFactory = { editor ->
                        dailyTextInputEdit = editor
                        setupEditor(editor)
                        editor.setEditContent(editDailyViewModel.uiState.content)
                        getDailyImage()
                        getDailyVideo()
                        getDailyAudio()
                    })
            }
        }

        val showTitle = SharedPreferencesUtil.getBoolean(
            this, DailySettingsConst.TITLE_SWITCH_KEY, false
        ) || !getDailyTitle().isNullOrEmpty()
        editDailyViewModel.updateTitleVisibility(showTitle)

        setDailyDateTime()
        setDailySinglePassword()
        setDailyMoodIndex()
        setDailyWeatherIndex()
        setDailyBackgroundColorIndex()
        setDailyLabel()
        getDailyLabels()
        setIsPinned()
        setMonthDay()
        restoreIndex(savedInstanceState)

        // 添加返回键回调
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
                imageList.remove(imagePath)
                deleteImageList.add(imagePath)
                val hasChanges = !originalAllContentEqualsCurrentContent()
                editDailyViewModel.updateCanSave(hasChanges)
                onBackPressedCallback.isEnabled = hasChanges
            }
        })
        editor.setVideoDeletionListener(object : DailyTextInputEdit.VideoDeletionListener {
            override fun onVideoDeleted(videoPath: String) {
                videoList.remove(videoPath)
                deleteVideoList.add(videoPath)
                val hasChanges = !originalAllContentEqualsCurrentContent()
                editDailyViewModel.updateCanSave(hasChanges)
                onBackPressedCallback.isEnabled = hasChanges
            }
        })
        editor.setAudioDeletionListener(object : DailyTextInputEdit.AudioDeletionListener {
            override fun onAudioDeleted(audioPath: String) {
                audioList.remove(audioPath)
                deleteAudioList.add(audioPath)
                val hasChanges = !originalAllContentEqualsCurrentContent()
                editDailyViewModel.updateCanSave(hasChanges)
                onBackPressedCallback.isEnabled = hasChanges
            }
        })

        val textLineSpacingValue = sharedPreferences.getFloat("text_line_spacing_preference", 0F)
        editor.setLineSpacing(textLineSpacingValue, 1F)

        val textSize = sharedPreferences.getFloat(ConstUtil.TEXT_SIZE_KEY, 16F)
        editor.textSize = textSize
        editDailyViewModel.updateTitleFontSize(textSize + 4)
        editDailyViewModel.updateContentFontSize(textSize)

        editor.addTextChangedListener { _: android.text.Editable? ->
            editDailyViewModel.updateContent(editor.getExportText())
            // 计算标题 + 正文的总字数
            val totalCount = editDailyViewModel.uiState.title.length + editor.getWordCount()
            editDailyViewModel.updateWordCount(totalCount)
            val hasChanges = !originalAllContentEqualsCurrentContent()
            editDailyViewModel.updateCanSave(hasChanges)
            onBackPressedCallback.isEnabled = hasChanges
        }

        // 初始状态更新
        val initialChanges = !originalAllContentEqualsCurrentContent()
        val initialWordCount = editDailyViewModel.uiState.title.length + editor.getWordCount()
        editDailyViewModel.updateWordCount(initialWordCount)
        editDailyViewModel.updateCanSave(initialChanges)
        onBackPressedCallback.isEnabled = initialChanges
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
     * 获取日记内容
     */
    private fun getDailyContent(): String? = intent.getStringExtra("daily_content")

    /**
     * 获取日记日期时间
     */
    private fun getDailyDateTime(): String =
        DateUtil.getDateString(0, Date(intent.getLongExtra("daily_date_time", 0)))

    /**
     * 设置日记日期时间
     */
    private fun setDailyDateTime() {
        editDailyViewModel.updateDateTime(getDailyDateTime())
    }

    /**
     * 选择日期时间
     */
    private fun selectDateTime() {
        val dateTimeString = editDailyViewModel.uiState.dateTimeText
        val initialDate = DateUtil.dateStringToDate(dateTimeString, 0)
        DateUtil.selectDateTime(this, Date(initialDate)) { newDateTime ->
            editDailyViewModel.updateDateTime(newDateTime)
            editDailyViewModel.updateCanSave(!originalAllContentEqualsCurrentContent())
        }
    }

    /**
     * 设置 [stringChangedListener].
     */
    fun setStringChangedListener(stringChangedListener: StringChangedListener) {
        this.stringChangedListener = stringChangedListener
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
        editDailyViewModel.updateBackgroundColor(getDailyBackgroundColorIndex())
    }

    /**
     * 设置日记背景颜色
     */
    private fun setDailyBackgroundColor(index: Int) {
        editDailyViewModel.updateBackgroundColor(index)
    }

    /**
     * 获取日记密码
     */
    private fun getDailySinglePassword(): String =
        intent.getStringExtra("single_password") ?: "" // 避免编辑时传入 null

    /**
     * 设置日记密码
     */
    private fun setDailySinglePassword() =
        getDailySinglePassword().also { editDailyViewModel.updateSinglePassword(it) }

    /**
     * 获取日记心情
     */
    private fun getDailyMoodIndex() = intent.getIntExtra("mood_index", 0)

    /**
     * 设置日记心情
     */
    private fun setDailyMoodIndex() {
        val moodIndex = getDailyMoodIndex()
        editDailyViewModel.updateMood(if (moodIndex > 0) moodIndex - 1 else ConstUtil.moodList.size - 1)
    }

    /**
     * 获取日记心情
     */
    private fun getDailyWeatherIndex() = intent.getIntExtra("weather_index", 0)

    /**
     * 设置日记天气
     */
    private fun setDailyWeatherIndex() {
        val weatherIndex = getDailyWeatherIndex()
        editDailyViewModel.updateWeather(if (weatherIndex > 0) weatherIndex - 1 else ConstUtil.weatherList.size - 1)
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
     * 获取日记图片
     */
    private fun getDailyImage() {
        dailyViewModel.queryDailyImageByUuid(dailyUuid).observe(this) { dailyImageList ->
            val images = dailyImageList.mapNotNull { it.imagePath }.toSet()
            if (originalImageList.isEmpty()) {
                originalImageList.addAll(images)
            }
            imageList.clear()
            imageList.addAll(images)
            dailyTextInputEdit.setOldImageList(images.toList())
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
        val label = getDailyLabel() ?: ""
        editDailyViewModel.updateLabel(label)
        editDailyViewModel.showLabel(label.isNotEmpty())
    }

    /**
     * 获取日记视频
     */
    private fun getDailyVideo() {
        dailyViewModel.queryDailyVideoByUuid(dailyUuid).observe(this) { dailyVideoList ->
            val videos = dailyVideoList.mapNotNull { it.videoPath }.toSet()
            if (originalVideoList.isEmpty()) {
                originalVideoList.addAll(videos)
            }
            videoList.clear()
            videoList.addAll(videos)
            dailyTextInputEdit.setOldVideoList(videos.toList())
        }
    }

    /**
     * 获取日记音频
     */
    private fun getDailyAudio() {
        dailyViewModel.queryDailyAudioByUuid(dailyUuid).observe(this) { dailyAudioList ->
            val audios = dailyAudioList.mapNotNull { it.audioPath }.toSet()
            if (originalAudioList.isEmpty()) {
                originalAudioList.addAll(audios)
            }
            audioList.clear()
            audioList.addAll(audios)
            dailyTextInputEdit.setOldAudioList(audios.toList())
        }
    }

    /**
     * 设置置顶
     */
    private fun setIsPinned() {
        editDailyViewModel.updatePinnedStatus(intent.getBooleanExtra("is_pinned", false))
    }

    /**
     * 获取日记月日
     */
    private fun getMonthDay(): String = intent.getLongExtra("daily_date_time", 0).toMonthDay()

    /**
     * 设置日记月日
     */
    private fun setMonthDay() {
        monthDay = getMonthDay()
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

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        takeImageLauncher.launch(intent)
    }

    private val takeImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            tempImageList.clear()
            imageList.add(imagePath)
            tempImageList.add(imagePath)
            if (tempImageList.isNotEmpty()) {
                dailyTextInputEdit.insertImages(tempImageList.toList())
            }
            tempImageList.clear()
            val hasChanges = !originalAllContentEqualsCurrentContent()
            editDailyViewModel.updateCanSave(hasChanges)
            onBackPressedCallback.isEnabled = hasChanges
        }
    }

    private val addDrawImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            drawImageName = data.getStringExtra(ImageUtil.DRAW_IMAGE) ?: ""
            if (drawImageName.isNotEmpty()) {
                val imageDir =
                    "${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/$drawImageName"
                tempImageList.clear()
                imageList.add(imageDir)
                tempImageList.add(imageDir)
                if (tempImageList.isNotEmpty()) {
                    dailyTextInputEdit.insertImages(tempImageList.toList())
                }
                drawImageName = ""
                tempImageList.clear()
            }
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
                if (label.isNotEmpty()) {
                    addDailyLabel(label)
                }
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
        editDailyViewModel.updateMood(position)
        editDailyViewModel.updateCanSave(!originalAllContentEqualsCurrentContent())
    }

    /**
     * 设置天气图标
     */
    private fun setWeatherIcon(position: Int) {
        editDailyViewModel.updateWeather(position)
        editDailyViewModel.updateCanSave(!originalAllContentEqualsCurrentContent())
    }

    /**
     * 添加启动器
     */
    private val addLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                try {
                    if (result.resultCode != RESULT_OK) return
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
                        dailyTextInputEdit.insertImages(tempImageList.toList())
                    }
                    if (tempVideoList.isNotEmpty()) {
                        dailyTextInputEdit.insertVideos(tempVideoList.toList())
                    }
                    if (tempAudioList.isNotEmpty()) {
                        dailyTextInputEdit.insertAudio(tempAudioList.toList())
                    }
                } catch (_: Exception) {
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
                it.contains("image") -> {
                    val path = CopyUtil.copyImageToMyAppDir(this, uri)
                    if (path.isNotEmpty()) {
                        imageList.add(path)
                        tempImageList.add(path)
                    }
                }

                it.contains("video") -> {
                    val path = CopyUtil.copyVideoToMyAppDir(this, uri)
                    if (path.isNotEmpty()) {
                        videoList.add(path)
                        tempVideoList.add(path)
                    }
                }

                it.contains("audio") -> {
                    val path = CopyUtil.copyAudioToMyAppDir(this, uri)
                    if (path.isNotEmpty()) {
                        audioList.add(path)
                        tempAudioList.add(path)
                    }
                }
            }
        }
    }

    /**
     * 显示确认删除对话框
     */
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                if (imageList.isNotEmpty()) {
                    imageList.forEach {
                        if (FileUtil().checkFileExists(it)) FileUtil().deleteFile(it)
                    }
                    dailyViewModel.deletePathImageByDailyUuid(dailyUuid)
                }
                if (videoList.isNotEmpty()) {
                    videoList.forEach {
                        if (FileUtil().checkFileExists(it)) FileUtil().deleteFile(it)
                    }
                    dailyViewModel.deletePathVideoByDailyUuid(dailyUuid)
                }
                if (audioList.isNotEmpty()) {
                    audioList.forEach {
                        if (FileUtil().checkFileExists(it)) FileUtil().deleteFile(it)
                    }
                    dailyViewModel.deletePathAudioByDailyUuid(dailyUuid)
                }
                dailyViewModel.deleteDaily(
                    DailyEntity(
                        id = getDailyId(),
                        title = editDailyViewModel.uiState.title,
                        content = dailyTextInputEdit.text.toString(),
                        dateTime = DateUtil.dateStringToDate(getDailyDateTime(), 0),
                        backgroundColorIndex = editDailyViewModel.uiState.backgroundColorIndex,
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

    /**
     * 显示添加图片对话框
     *
     * [SELECT_IMAGE] 选择图片,
     * [TAKE_IMAGE] 拍摄图片
     */
    private fun showAddImageDialog() {
        val addImageOptions = arrayOf(
            getString(R.string.select_image), getString(R.string.take_image)
        )
        MaterialAlertDialogBuilder(this).apply {
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

                    TAKE_IMAGE -> openCamera()
                }
            }
            setPositiveButton(getString(R.string.cancel), null)
            create()
            show()
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

    /** 显示字体对话框 */
    private fun showFontPresetDialog() {
        val items =
            arrayOf(getString(R.string.small), getString(R.string.body), getString(R.string.title))
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.text_font_size))
            .setItems(items) { _, which ->
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
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.heading_level))
            .setItems(items) { _, which ->
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

        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.insert_hyperlink))
            .setView(dialogView)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                val url = input.text?.toString()?.trim().orEmpty()
                if (url.isNotEmpty()) {
                    dailyTextInputEdit.applyLink(url)
                }
            }.show()
    }

    /**
     * 显示上锁对话框
     */
    private fun showLockDialog() {
        val currentPassword = editDailyViewModel.uiState.singlePassword
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
        inputPasswordLayout.hint =
            if (currentPassword == "") getString(R.string.locked) else getString(R.string.unlocked)
        inputPassword.setText(currentPassword)
        MaterialAlertDialogBuilder(this).apply {
            if (currentPassword == "") setTitle(getString(R.string.locked)) else setTitle(
                getString(
                    R.string.unlocked
                )
            )
            setView(inflate)
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                editDailyViewModel.updateSinglePassword(inputPassword.text.toString())
                val hasChanges = !originalAllContentEqualsCurrentContent()
                editDailyViewModel.updateCanSave(hasChanges)
                onBackPressedCallback.isEnabled = hasChanges
            }
            if (currentPassword != "") {
                setNegativeButton(getString(R.string.clear_password)) { _, _ ->
                    editDailyViewModel.updateSinglePassword("")
                    val hasChanges = !originalAllContentEqualsCurrentContent()
                    editDailyViewModel.updateCanSave(hasChanges)
                    onBackPressedCallback.isEnabled = hasChanges
                }
            }
            setNeutralButton(getString(R.string.cancel), null)
            setCancelable(false)
            create()
            show()
        }
    }

    /** 显示标签对话框 */
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
                editDailyViewModel.updateLabel("")
                editDailyViewModel.showLabel(false)
                val hasChanges = !originalAllContentEqualsCurrentContent()
                editDailyViewModel.updateCanSave(hasChanges)
                onBackPressedCallback.isEnabled = hasChanges
            }
            setNegativeButton(getString(R.string.new_label)) { _, _ ->
                showLabelInputDialog()
            }
            setNeutralButton(getString(R.string.cancel), null)
            val dialog = create()
            dialog.show()
            selectDailyLabelAdapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(position: Int) {
                    val selectedLabel = dailyLabelList[position]
                    editDailyViewModel.updateLabel(selectedLabel)
                    editDailyViewModel.showLabel(true)
                    val hasChanges = !originalAllContentEqualsCurrentContent()
                    editDailyViewModel.updateCanSave(hasChanges)
                    onBackPressedCallback.isEnabled = hasChanges
                    dialog.dismiss()
                }
            })
        }
    }

    /**
     * 保存日记
     */
    private fun saveDaily() {
        val uiState = editDailyViewModel.uiState
        val singlePasswordSha256 =
            if (uiState.singlePassword.isEmpty()) "" else HashUtil.hashSHA256(uiState.singlePassword)
        getSharedPreferences("DAILY_CONTENT_UPDATE", MODE_PRIVATE).edit {
            putString("daily_update_content_$dailyUuid", dailyTextInputEdit.getExportText())
            if (getDailySinglePassword() != singlePasswordSha256) putString(
                "daily_update_single_password_$dailyUuid",
                singlePasswordSha256
            )
        }
        isSystemExit = false
        dailyViewModel.updateDaily(
            DailyEntity(
                id = getDailyId(),
                title = uiState.title,
                content = dailyTextInputEdit.getExportText(),
                dateTime = DateUtil.dateStringToDate(uiState.dateTimeText, 0),
                backgroundColorIndex = uiState.backgroundColorIndex,
                singlePassword = singlePasswordSha256,
                moodIndex = if (uiState.moodIndex == ConstUtil.moodList.size - 1) 0 else uiState.moodIndex + 1,
                weatherIndex = if (uiState.weatherIndex == ConstUtil.weatherList.size - 1) 0 else uiState.weatherIndex + 1,
                dailyUUID = dailyUuid,
                dailyLabel = uiState.label,
                isPinned = uiState.isPinned,
                monthDay = monthDay
            )
        )

        val newImages = imageList.filter { !originalImageList.contains(it) }
        if (newImages.isNotEmpty()) dailyViewModel.insertDailyImagePath(dailyUuid, newImages)

        val newVideos = videoList.filter { !originalVideoList.contains(it) }
        if (newVideos.isNotEmpty()) dailyViewModel.insertDailyVideoPath(dailyUuid, newVideos)

        val newAudios = audioList.filter { !originalAudioList.contains(it) }
        if (newAudios.isNotEmpty()) dailyViewModel.insertDailyAudioPath(dailyUuid, newAudios)

        autoSaveDailySharedPreferences(this, 1, "", "", 0, 0, "", 0, 0, "", false, "", false, false)
        sharedPreferences.edit { putLong("switch_preference_auto_save_id", 0) }

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
     * 监听返回键
     */
    private var onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                isDailyNullOrEquals()
            }

    }

    /**
     * 判断日记是否为空
     */
    private fun isDailyNullOrEquals() {
        // 如果文本都为空或未发生变化，则直接退出
        if (originalAllContentEqualsCurrentContent()) {
            isSystemExit = false
            finish()
            return
        }

        if (autoSave) {
            saveDaily()
            return
        }

        MaterialAlertDialogBuilder(this).setMessage(getString(R.string.do_you_save_this_diary))
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                saveDaily()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                autoSaveDailySharedPreferences(
                    this, 1, "", "", 0, 0, "", 0, 0, "", false, "", false, false
                )
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
     * 不保存则删除应用私有目录下对应的图片
     */
    private fun notSaveToDeleteAppImage() {
        CoroutineScope(Dispatchers.IO).launch {
            val newImages = imageList.filter { !originalImageList.contains(it) }
            newImages.forEach {
                dailyViewModel.deleteSelectPathImage(it)
                FileUtil().deleteFile(it)
            }
        }
    }

    /**
     * 删除未保存的视频
     */
    private fun notSaveToDeleteAppVideos() {
        CoroutineScope(Dispatchers.IO).launch {
            val newVideos = videoList.filter { !originalVideoList.contains(it) }
            newVideos.forEach {
                dailyViewModel.deleteSelectPathVideo(it)
                FileUtil().deleteFile(it)
            }
        }
    }

    /**
     * 删除未保存的音频
     */
    private fun notSaveToDeleteAppAudios() {
        CoroutineScope(Dispatchers.IO).launch {
            val newAudios = audioList.filter { !originalAudioList.contains(it) }
            newAudios.forEach {
                dailyViewModel.deleteSelectPathAudio(it)
                FileUtil().deleteFile(it)
            }
        }
    }

    /**
     * 判断主要内容是否为空
     */
    private fun contentIsNotNull(): Boolean {
        val uiState = editDailyViewModel.uiState
        return uiState.title.trim().isNotEmpty() || dailyTextInputEdit.text!!.trim()
            .isNotEmpty() || imageList.isNotEmpty() || videoList.isNotEmpty() || audioList.isNotEmpty()
    }

    /**
     * 判断所有原始与当前所有内容相同
     */
    private fun originalAllContentEqualsCurrentContent(): Boolean {
        val uiState = editDailyViewModel.uiState
        val currentPasswordHash =
            if (uiState.singlePassword.isEmpty()) "" else HashUtil.hashSHA256(uiState.singlePassword)

        return Objects.equals(uiState.title, getDailyTitle() ?: "") && Objects.equals(
            dailyTextInputEdit.getExportText(), getDailyContent() ?: ""
        ) && uiState.backgroundColorIndex == getDailyBackgroundColorIndex() && Objects.equals(
            currentPasswordHash, getDailySinglePassword()
        ) && (if (uiState.moodIndex == ConstUtil.moodList.size - 1) 0 else uiState.moodIndex + 1) == getDailyMoodIndex() && (if (uiState.weatherIndex == ConstUtil.weatherList.size - 1) 0 else uiState.weatherIndex + 1) == getDailyWeatherIndex() &&
                imageList == originalImageList &&
                videoList == originalVideoList &&
                audioList == originalAudioList &&
                uiState.label == (getDailyLabel() ?: "")
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    override fun onStop() {
        super.onStop()
        if (isSystemExit && autoSave && contentIsNotNull()) {
            val uiState = editDailyViewModel.uiState
            sharedPreferences.edit { putLong("switch_preference_auto_save_id", getDailyId()) }
            autoSaveDailySharedPreferences(
                this, 1,
                uiState.title,
                dailyTextInputEdit.getExportText(),
                DateUtil.dateStringToDate(uiState.dateTimeText, 0),
                uiState.backgroundColorIndex,
                uiState.singlePassword,
                if (uiState.moodIndex == ConstUtil.moodList.size - 1 || uiState.moodIndex == -1) 0 else uiState.moodIndex + 1,
                if (uiState.weatherIndex == ConstUtil.weatherList.size - 1 || uiState.weatherIndex == -1) 0 else uiState.weatherIndex + 1,
                dailyUuid,
                imageList.isNotEmpty(),
                uiState.label,
                videoList.isNotEmpty(),
                audioList.isNotEmpty(),
                uiState.isPinned
            )
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
        MaterialAlertDialogBuilder(this).setTitle(
            if (isHighlight) getString(R.string.highlight) else getString(
                R.string.text_color
            )
        ).setView(colorPickerLayout).setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.sure)) { _, _ ->
                if (isHighlight) {
                    dailyTextInputEdit.applyHighlightColor(selectedColor)
                } else {
                    dailyTextInputEdit.applyTextColor(selectedColor)
                }
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val uiState = editDailyViewModel.uiState
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
            dailyUuid = savedInstanceState.getString("daily_uuid", "")
            val backgroundColorIndex = savedInstanceState.getInt(BACK_GROUND_COLOR_INDEX)
            editDailyViewModel.updateBackgroundColor(backgroundColorIndex)

            val moodIndex = savedInstanceState.getInt(MOOD_INDEX, -1)
            if (moodIndex != -1) {
                editDailyViewModel.updateMood(moodIndex)
            }

            val weatherIndex = savedInstanceState.getInt(WEATHER_INDEX, -1)
            if (weatherIndex != -1) {
                editDailyViewModel.updateWeather(weatherIndex)
            }

            val label = savedInstanceState.getString(DAILY_LABEL) ?: ""
            editDailyViewModel.updateLabel(label)
            editDailyViewModel.showLabel(label.isNotEmpty())
        }
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * 检查标题长度
     */
    private fun checkedTitleLength(title: String): String {
        return StringUtil.checkStringLength(title, 30)
    }

    companion object {
        private var isSystemExit = true
    }
}