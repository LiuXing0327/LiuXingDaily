package com.liuxing.daily.ui.edit

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityEditDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import com.liuxing.daily.util.StringUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.util.Date
import java.util.Objects

class EditDailyActivity : AppCompatActivity() {

    private lateinit var activityEditDailyBinding: ActivityEditDailyBinding
    private var backgroundColorIndex: Int = 0
    private lateinit var dailyViewModel: DailyViewModel
    private var singlePassword: String? = ""

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
        setDailyTitle()
        setDailyContent()
        setDailyDateTime()
        setDailyCount()
        setDailySinglePassword()
        updateDailyCount()
        initMenu()
        setDailyBackgroundColor(getDailyBackgroundColorIndex())
        setDailyBackgroundColorIndex()
        initViewModel()
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
                            .setMessage("确定永久删除这篇日记吗？")
                            .setPositiveButton(getString(R.string.sure)) { dialog, which ->
                                dailyViewModel.deleteDaily(
                                    DailyEntity(
                                        id = getDailyId(),
                                        title = activityEditDailyBinding.inputTitle.text.toString(),
                                        content = activityEditDailyBinding.inputContent.text.toString(),
                                        dateTime = DateUtil.dateStringToDate(getDailyDateTime(), 2),
                                        backgroundColorIndex = backgroundColorIndex
                                    )
                                )
                                isSystemExit = false
                                finish()
                            }
                            .setNegativeButton("取消", null)
                            .create()
                            .show()
                    }

                    R.id.item_lock_to_on_and_un_ed -> {
                        val sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(this@EditDailyActivity)
                        sharedPreferences.getString("forget_password_key", "")
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
                                inputPasswordLayout.hint = "密钥"
                                MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
                                    setTitle("密钥")
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
                                                            "请先输入忘记密码时，重置密码的密钥！"
                                                        )
                                                    }

                                                    else -> {
                                                        MaterialAlertDialogBuilder(this@EditDailyActivity).apply {
                                                            setMessage("密钥设置成功，请牢记！")
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
                                            "请先输入忘记密码时，重置密码的密钥！"
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
                }

                return true
            }

        })
    }

    /**
     * 判断日记是否为空
     */
    private fun isDailyNullOrEquals() {
        // 如果文本都为空，则直接退出
        if (activityEditDailyBinding.inputTitle.text!!.trim()
                .isEmpty() && activityEditDailyBinding.inputContent.text!!.trim()
                .isEmpty()
        ) {
            isSystemExit = false
            finish()
        } else {
            // 当前内容与原内容进行对比
            if (Objects.equals(
                    activityEditDailyBinding.inputTitle.text.toString(),
                    getDailyTitle()
                ) && Objects.equals(
                    activityEditDailyBinding.inputContent.text.toString(),
                    getDailyContent()
                ) && backgroundColorIndex == getDailyBackgroundColorIndex()
                && Objects.equals(singlePassword, getDailySinglePassword())
            ) {
                finish()
            } else {
                MaterialAlertDialogBuilder(this@EditDailyActivity)
                    .setMessage("是否保存这篇日记？")
                    .setPositiveButton("保存") { dialog, which ->
                        isSystemExit = false
                        saveDaily()
                    }
                    .setNegativeButton("取消") { dialog, which ->
                        isSystemExit = false
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
        when {
            activityEditDailyBinding.inputTitle.text!!.trim()
                .isNotEmpty() || activityEditDailyBinding.inputContent.text!!.trim()
                .isNotEmpty() -> {
                when {
                    Objects.equals(
                        activityEditDailyBinding.inputTitle.text.toString(),
                        getDailyTitle()
                    ) || Objects.equals(
                        activityEditDailyBinding.inputContent.text.toString(),
                        getDailyContent()
                    ) || backgroundColorIndex == getDailyBackgroundColorIndex() || Objects.equals(
                        singlePassword,
                        getDailySinglePassword()
                    ) -> {
                        dailyViewModel.updateDaily(
                            DailyEntity(
                                id = getDailyId(),
                                title = activityEditDailyBinding.inputTitle.text.toString(),
                                content = activityEditDailyBinding.inputContent.text.toString(),
                                dateTime = DateUtil.dateStringToDate(getDailyDateTime(), 2),
                                backgroundColorIndex = backgroundColorIndex,
                                singlePassword = HashUtil.hashSHA256(singlePassword.toString())
                            )
                        )
                        finish()
                    }
                }
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val autoSave = sharedPreferences.getBoolean("switch_preference_auto_save", true)
        if (isSystemExit) {
            if (autoSave) {
                saveDaily()
            }
        }
    }

    companion object {
        var isSystemExit = false
    }
}