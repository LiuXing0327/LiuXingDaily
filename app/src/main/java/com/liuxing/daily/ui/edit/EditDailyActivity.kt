package com.liuxing.daily.ui.edit

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailyImagePagerAdapter
import com.liuxing.daily.adapter.MoodAdapter
import com.liuxing.daily.adapter.WeatherAdapter
import com.liuxing.daily.databinding.ActivityEditDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.image.LookDailyImageActivity
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.SharedPreferencesUtil.autoSaveDailySharedPreferences
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import com.liuxing.daily.util.StringUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Objects

class EditDailyActivity : AppCompatActivity() {

    private lateinit var activityEditDailyBinding: ActivityEditDailyBinding
    private var backgroundColorIndex: Int = 0
    private lateinit var dailyViewModel: DailyViewModel
    private var singlePassword: String? = ""
    private var moodIndex: Int = 0
    private var weatherIndex: Int = 0
    private var dailyUuid: String = ""
    private val imageList = mutableSetOf<String>()
    private var tempImageListIndex: Int = 0
    private val tempImageList = mutableSetOf<String>()

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
        initData()
        // 添加返回键回调
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        SoftHideKeyBoardUtil(this)
    }

    /**
     * 初始化数据
     */
    private fun initData() {
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
    private fun setDailyContent() = activityEditDailyBinding.inputContent.setText(getDailyContent())

    /**
     * 获取日记日期时间
     */
    private fun getDailyDateTime(): String =
        DateUtil.getDateString(2, Date(intent.getLongExtra("daily_date_time", 0)))

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
        "${activityEditDailyBinding.inputTitle.text!!.length.plus(activityEditDailyBinding.inputContent.text!!.length)}字".also {
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
        activityEditDailyBinding.inputContent.addTextChangedListener(object : TextWatcher {
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
        when (index) {
            1 -> {
                activityEditDailyBinding.main.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.color_2
                    )
                )
                activityEditDailyBinding.toolbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.color_2
                    )
                )
            }

            2 -> {
                activityEditDailyBinding.main.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.color_3
                    )
                )
                activityEditDailyBinding.toolbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.color_3
                    )
                )
            }

            3 -> {
                activityEditDailyBinding.main.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.color_4
                    )
                )
                activityEditDailyBinding.toolbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.color_4
                    )
                )
            }

            else -> {
                activityEditDailyBinding.main.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.transparent
                    )
                )
                activityEditDailyBinding.toolbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.transparent
                    )
                )
            }
        }

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
        dailyViewModel.queryDailyImageByUuid(getDailyUuid().toString()).observe(
            this
        ) { dailyImageList ->
            dailyImageList.forEach { dailyImageEntity ->
                if (FileUtil().checkFileExists(dailyImageEntity.imagePath.toString())) {
                    if (tempImageListIndex == 0) {
                        tempImageList.add(dailyImageEntity.imagePath.toString())
                    }
                    imageList.add(dailyImageEntity.imagePath.toString())
                }
                displayImage()
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
                        .isEmpty() && activityEditDailyBinding.inputContent.text!!.trim()
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
                        view.findViewById<MaterialCardView>(R.id.color_1).setOnClickListener {
                            backgroundColorIndex = 0
                            setDailyBackgroundColor(backgroundColorIndex)
                            dialog.dismiss()
                        }
                        view.findViewById<MaterialCardView>(R.id.color_2).setOnClickListener {
                            backgroundColorIndex = 1
                            setDailyBackgroundColor(backgroundColorIndex)
                            dialog.dismiss()
                        }
                        view.findViewById<MaterialCardView>(R.id.color_3).setOnClickListener {
                            backgroundColorIndex = 2
                            setDailyBackgroundColor(backgroundColorIndex)
                            dialog.dismiss()
                        }
                        view.findViewById<MaterialCardView>(R.id.color_4).setOnClickListener {
                            backgroundColorIndex = 3
                            setDailyBackgroundColor(backgroundColorIndex)
                            dialog.dismiss()
                        }
                    }

                    R.id.item_save -> isDailyNullOrEquals()

                    R.id.item_delete -> {
                        MaterialAlertDialogBuilder(this@EditDailyActivity)
                            .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                            .setPositiveButton(getString(R.string.sure)) { dialog, which ->
                                dailyViewModel.deletePathImageByDailyUuid(dailyUuid)
                                dailyViewModel.deleteDaily(
                                    DailyEntity(
                                        id = getDailyId(),
                                        title = activityEditDailyBinding.inputTitle.text.toString(),
                                        content = activityEditDailyBinding.inputContent.text.toString(),
                                        dateTime = DateUtil.dateStringToDate(getDailyDateTime(), 2),
                                        backgroundColorIndex = backgroundColorIndex,
                                        dailyUUID = dailyUuid
                                    )
                                )
                                isSystemExit = false
                                finish()
                            }
                            .setNegativeButton(getString(R.string.cancel), null)
                            .create()
                            .show()
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
                                if (position == ConstUtil.moodList.size - 1) {
                                    moodIndex = 0
                                    activityEditDailyBinding.ivMood.visibility = View.GONE
                                } else {
                                    activityEditDailyBinding.ivMood.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@EditDailyActivity,
                                            ConstUtil.moodList[position]
                                        )
                                    )
                                    moodIndex = position + 1
                                    activityEditDailyBinding.ivMood.visibility = View.VISIBLE
                                }
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
                                if (position == ConstUtil.weatherList.size - 1) {
                                    activityEditDailyBinding.ivWeather.visibility = View.GONE
                                } else {
                                    activityEditDailyBinding.ivWeather.visibility = View.VISIBLE
                                    activityEditDailyBinding.ivWeather.setImageDrawable(
                                        ContextCompat.getDrawable(
                                            this@EditDailyActivity,
                                            ConstUtil.weatherList[position]
                                        )
                                    )
                                    weatherIndex = position.plus(1)
                                }
                                dialog.dismiss()
                            }

                        })
                    }

                    R.id.item_add_image -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            setType("image/*")
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
     * 添加图片启动器
     */
    private val addImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode != Activity.RESULT_OK) return
                val data = result.data ?: return
                val uri = data.data ?: return
                val copyImageToMyAppDir = CopyUtil.copyImageToMyAppDir(this@EditDailyActivity, uri)
                imageList.add(copyImageToMyAppDir)
            }
        })

    /**
     * 判断日记是否为空
     */
    private fun isDailyNullOrEquals() {
        // 如果文本都为空，则直接退出
        if (contentIsNull()) {
            isSystemExit = false
            LogUtil.d("", "inputTitle")
            finish()
        } else {
            if (originalAllContentEqualsCurrentContent()) {
                LogUtil.d("", "weatherIndex")
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
     * 判断原始图片集合是否与当前图片集合相同
     *
     * @return 判断结果
     */
    private fun originalImageEqualsCurrentImage(): Boolean {
        if (imageList.size != tempImageList.size) return false
        imageList.toList().forEachIndexed { index, item ->
            if (item != tempImageList.toList()[index]) return false
        }
        return true
    }

    /**
     * 保存日记
     */
    private fun saveDaily() {
        dailyViewModel.updateDaily(
            DailyEntity(
                id = getDailyId(),
                title = activityEditDailyBinding.inputTitle.text.toString(),
                content = activityEditDailyBinding.inputContent.text.toString(),
                dateTime = DateUtil.dateStringToDate(getDailyDateTime(), 2),
                backgroundColorIndex = backgroundColorIndex,
                singlePassword = HashUtil.hashSHA256(singlePassword.toString()),
                moodIndex = moodIndex,
                weatherIndex = weatherIndex,
                dailyUUID = dailyUuid
            )
        )
        if (findMissingElements().isNotEmpty()) {
            findMissingElements().forEach {
                dailyViewModel.deleteSelectPathImage(it)
            }
        }

        dailyViewModel.queryDailyImageByUuid(dailyUuid).observe(this) { dailyImageList ->
            val existingImagePaths = dailyImageList.map { it.imagePath }.toSet()
            val imagesToInsert = imageList.filter { it !in existingImagePaths }
            if (imagesToInsert.isNotEmpty()) {
                dailyViewModel.insertDailyImagePath(dailyUuid, imagesToInsert)
            }
        }
        dailyViewModel.queryDailyImageByUuid(dailyUuid).removeObservers(this)
        finish()
    }

    /**
     * 查询当前图片集合中与原始图片集合的不同元素
     */
    private fun findMissingElements(): Set<String> {
        return tempImageList.toSet().subtract(imageList.toSet())
    }

    /**
     * 不保存则删除应用私有目录下对应的图片
     */
    private fun notSaveToDeleteAppImage() {
        val notSaveImage = imageList.filterNot { it in tempImageList }
        notSaveImage.forEach { notList ->
            CoroutineScope(Dispatchers.IO).launch {
                FileUtil().deleteFile(notList)
            }
        }
    }


    /**
     * 判断主要内容是否为空
     */
    private fun contentIsNull(): Boolean {
        return activityEditDailyBinding.inputTitle.text!!.trim()
            .isEmpty() && activityEditDailyBinding.inputContent.text!!.trim()
            .isEmpty() && imageList.isEmpty()
    }

    /**
     * 判断所有原始与当前所有内容相同
     */
    private fun originalAllContentEqualsCurrentContent(): Boolean {
        return Objects.equals(
            activityEditDailyBinding.inputTitle.text.toString(),
            getDailyTitle()
        ) && Objects.equals(
            activityEditDailyBinding.inputContent.text.toString(),
            getDailyContent()
        ) && backgroundColorIndex == getDailyBackgroundColorIndex() && Objects.equals(
            singlePassword,
            getDailySinglePassword()
        ) && moodIndex == getDailyMoodIndex() && weatherIndex == getDailyWeatherIndex() && originalImageEqualsCurrentImage()
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
            sharedPreferences.edit {
                putLong("switch_preference_auto_save_id", getDailyId())
            }
            autoSaveDailySharedPreferences(
                this, 1,
                activityEditDailyBinding.inputTitle.text.toString(),
                activityEditDailyBinding.inputContent.text.toString(),
                DateUtil.dateStringToDate(
                    activityEditDailyBinding.tvDateTime.text.toString(),
                    2
                ),
                backgroundColorIndex,
                singlePassword.toString(), moodIndex, weatherIndex, dailyUuid, imageList
            )
        }
    }

    companion object {
        var isSystemExit = false
    }

    /**
     * 显示图片
     */
    private fun displayImage() {
        if (imageList.isNotEmpty()) {
            val adapter =
                DailyImagePagerAdapter(imageList.toList()) { position ->
                    val intent = Intent(
                        this,
                        LookDailyImageActivity::class.java
                    ).apply {
                        putExtra("look_daily_image_position", position)
                        putExtra("look_daily_image_uuid", dailyUuid)
                    }
                    startActivity(intent)
                }
            activityEditDailyBinding.viewPager.adapter = adapter
            activityEditDailyBinding.viewPager.visibility = View.VISIBLE
        } else {
            activityEditDailyBinding.viewPager.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        displayImage()
    }
}