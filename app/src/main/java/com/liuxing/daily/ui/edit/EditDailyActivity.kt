package com.liuxing.daily.ui.edit

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.liuxing.daily.adapter.WeatherAdapter
import com.liuxing.daily.databinding.ActivityEditDailyBinding
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
import java.util.Date
import java.util.Objects

private const val BACK_GROUND_COLOR_INDEX = "backgroundColorIndex"
private const val MOOD_INDEX = "moodIndex"
private const val TEMP_MOOD_INDEX = "tempMoodIndex"
private const val WEATHER_INDEX = "weatherIndex"
private const val TEMP_WEATHER_INDEX = "tempWeatherIndex"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        activityEditDailyBinding = ActivityEditDailyBinding.inflate(layoutInflater)
        setContentView(activityEditDailyBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
        updateDailyCount()
        initMenu()
        setDailyBackgroundColor(getDailyBackgroundColorIndex())
        setDailyBackgroundColorIndex()
        checkedTitleLength()
        restoreIndex(savedInstanceState)
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
        activityEditDailyBinding.tvDateTime.text = getDailyDateTime()
    }

    /**
     * 设置日记字数
     */
    private fun setDailyCount() {
        "${activityEditDailyBinding.inputTitle.text!!.length.plus(dailyTextInputEdit.getWordCount())}字".also {
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
            if (it == 0) activityEditDailyBinding.ivMood.visibility =
                View.GONE else {
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
            if (it == 0) activityEditDailyBinding.ivWeather.visibility = View.GONE else {
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
     * 初始化菜单
     */
    private fun initMenu() {
        val menuHost = this
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_edit_daily, menu)
                when {
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
                }
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
                        colorRecycler.layoutManager = LinearLayoutManager(this@EditDailyActivity)
                        colorRecycler.adapter =
                            ChangeDailyCardColorAdapter(ConstUtil.backgroundColorList) { selectedColor, position ->
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
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(this@EditDailyActivity)
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
            activityEditDailyBinding.ivMood.visibility = View.GONE
        } else {
            activityEditDailyBinding.ivMood.setImageDrawable(
                ContextCompat.getDrawable(
                    this@EditDailyActivity,
                    ConstUtil.moodList[position]
                )
            )
            moodIndex = position
            tempMoodIndex = 1
            activityEditDailyBinding.ivMood.visibility = View.VISIBLE
        }
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
            weatherIndex = position
            tempWeatherIndex = 1
        }
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
                    tempImageList.clear()
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
     * 判断日记是否为空
     */
    private fun isDailyNullOrEquals() {
        // 如果文本都为空，则直接退出
        if (contentIsNull()) {
            isSystemExit = false
            finish()
        } else {
            if (originalAllContentEqualsCurrentContent()) {
                isSystemExit = false
                finish()
            } else {
                MaterialAlertDialogBuilder(this@EditDailyActivity)
                    .setMessage(getString(R.string.do_you_save_this_diary))
                    .setPositiveButton(getString(R.string.sure)) { dialog, which ->
                        isSystemExit = false
                        saveDaily()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, which ->
                        isSystemExit = false
                        notSaveToDeleteAppImage()
                        finish()
                    }
                    .create()
                    .show()

            }
        }

    }

    /**
     * 保存日记
     */
    private fun saveDaily() {
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
                dailyUUID = dailyUuid
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
        finish()
    }

    /**
     * 查询当前图片集合中与原始图片集合的不同元素
     */
    private fun findMissingElements(): Set<String> {
        return originalImageList.toSet().subtract(imageList.toSet())
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
     * 判断主要内容是否为空
     */
    private fun contentIsNull(): Boolean {
        return activityEditDailyBinding.inputTitle.text!!.trim()
            .isEmpty() && dailyTextInputEdit.text!!.trim()
            .isEmpty() && findMissingElements().isEmpty()
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
        ) && moodIndex == getDailyMoodIndex() && weatherIndex == getDailyWeatherIndex() && tempImageList2.isEmpty()
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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val autoSave = sharedPreferences.getBoolean("switch_preference_auto_save", true)
        if (isSystemExit && autoSave && !contentIsNull() && !originalAllContentEqualsCurrentContent()) {
            if (findMissingElements().isNotEmpty()) {
                findMissingElements().forEach {
                    dailyViewModel.deleteSelectPathImage(it)
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
                    2
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