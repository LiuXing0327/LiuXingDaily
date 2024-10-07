package com.liuxing.daily.ui.add

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityAddDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.SoftHideKeyBoardUtil
import com.liuxing.daily.util.StringUtil
import com.liuxing.daily.viewmodel.DailyViewModel

class AddDailyActivity : AppCompatActivity() {

    private lateinit var activityAddDailyBinding: ActivityAddDailyBinding
    private var backgroundColorIndex = 0
    private lateinit var dailyViewModel: DailyViewModel

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
        initMenu()
        setDailyCount()
        initViewModel()
        setDateTime()
        checkedTitleLength()
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
                if (activityAddDailyBinding.inputTitle.text!!.trim()
                        .isEmpty() && activityAddDailyBinding.inputContent.text!!.trim()
                        .isEmpty()
                ) {
                    menu.findItem(R.id.item_save).setVisible(false)
                    invalidateOptionsMenu()
                } else {
                    menu.findItem(R.id.item_save).setVisible(true)
                    invalidateOptionsMenu()
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
                        view.findViewById<MaterialCardView>(R.id.color_1).setOnClickListener {
                            backgroundColorIndex = 0
                            activityAddDailyBinding.main.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    android.R.color.transparent
                                )
                            )
                            activityAddDailyBinding.toolbar.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    android.R.color.transparent
                                )
                            )
                            dialog.dismiss()
                        }
                        view.findViewById<MaterialCardView>(R.id.color_2).setOnClickListener {
                            backgroundColorIndex = 1
                            activityAddDailyBinding.main.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    R.color.color_2
                                )
                            )
                            activityAddDailyBinding.toolbar.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    R.color.color_2
                                )
                            )
                            dialog.dismiss()
                        }
                        view.findViewById<MaterialCardView>(R.id.color_3).setOnClickListener {
                            backgroundColorIndex = 2
                            activityAddDailyBinding.main.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    R.color.color_3
                                )
                            )
                            activityAddDailyBinding.toolbar.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    R.color.color_3
                                )
                            )
                            dialog.dismiss()
                        }
                        view.findViewById<MaterialCardView>(R.id.color_4).setOnClickListener {
                            backgroundColorIndex = 3
                            activityAddDailyBinding.main.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    R.color.color_4
                                )
                            )
                            activityAddDailyBinding.toolbar.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@AddDailyActivity,
                                    R.color.color_4
                                )
                            )
                            dialog.dismiss()
                        }
                    }

                    R.id.item_save -> isDailyNull()
                }

                return true
            }
        })
    }

    /**
     * 设置日记字数
     */
    private fun setDailyCount() {
        activityAddDailyBinding.inputContent.addTextChangedListener {
            "${getDailyCount()}字".also { activityAddDailyBinding.tvDailyCount.text = it }
        }
        activityAddDailyBinding.inputTitle.addTextChangedListener {
            "${getDailyCount()}字".also { activityAddDailyBinding.tvDailyCount.text = it }
        }
    }

    /**
     * 获取日记字数
     */
    private fun getDailyCount(): Int =
        activityAddDailyBinding.inputTitle.text!!.length.plus(activityAddDailyBinding.inputContent.text!!.length)

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
                content = activityAddDailyBinding.inputContent.text.toString(),
                dateTime =/* DateUtil.dateStringToDate(
                    activityAddDailyBinding.tvDateTime.text.toString(),
                    2
                )*/1725887489000,
                backgroundColorIndex = backgroundColorIndex
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
            DateUtil.getDateString(2, DateUtil.getCurrentDate())
    }

    /**
     * 判断日记是否为空
     */
    private fun isDailyNull() {
        // 如果文本都为空，则直接退出
        if (activityAddDailyBinding.inputTitle.text!!.trim()
                .isEmpty() && activityAddDailyBinding.inputContent.text!!.trim()
                .isEmpty()
        ) {
            finish()
        } else {
            // 如果不为空，就询问是否保存
            MaterialAlertDialogBuilder(this@AddDailyActivity)
                .setMessage("是否保存这篇日记？")
                .setPositiveButton("保存") { dialog, which -> saveDaily() }
                .setNegativeButton("取消") { dialog, which -> finish() }
                .create()
                .show()
        }

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
}