package com.liuxing.daily.ui.look

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.adapter.LookDailyPagerAdapter
import com.liuxing.daily.databinding.ActivityLookDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.ui.edit.EditDailyActivity
import com.liuxing.daily.util.CopyUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.util.Date
import kotlin.math.log


class LookDailyActivity : AppCompatActivity() {

    private lateinit var lookDailyBinding: ActivityLookDailyBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailyEntity: DailyEntity
    private var currentIndex = 0
    private var originalSignalPassword = ""
    private lateinit var originalSignalPasswordMap: MutableMap<Long, String>
    private lateinit var tempSignalPasswordMap: MutableMap<Long, String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lookDailyBinding = ActivityLookDailyBinding.inflate(layoutInflater)
        setContentView(lookDailyBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initData(savedInstanceState)
    }

    /**
     * 初始化数据
     */
    private fun initData(savedInstanceState: Bundle?) {
        setActionBar()
        initViewModel()
        loadDailyToViewPager(savedInstanceState)
        initSharedPreferences()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(lookDailyBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    /**
     * 加载日记
     */
    private fun loadDailyToViewPager(savedInstanceState: Bundle?) {
        dailyViewModel.queryAllDaily().observe(this, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                if (value.isNotEmpty()) {
                    val sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this@LookDailyActivity)
                    val currentSortIndex = sharedPreferences?.getInt("daily_sort_by", 0)
                    val sortedByDescending = when (currentSortIndex) {
                        1 -> {
                            value.sortedBy {
                                DateUtil.getDateString(2, Date(it.dateTime!!))
                            }
                        }

                        else -> {
                            value.sortedByDescending {
                                DateUtil.getDateString(2, Date(it.dateTime!!))
                            }
                        }
                    }

                    if (currentIndex == 0) {
                        if (savedInstanceState != null) currentIndex =
                            savedInstanceState.getInt(VIEW_PAGER_INDEX, 0)
                        else {
                            val position = intent.getIntExtra("POSITION", 0)
                            if (position >= 0 && position < value.size) {
                                val intentPosition = value[position]
                                currentIndex =
                                    sortedByDescending.indexOfFirst { it.id == intentPosition.id }
                            }
                        }
                    }
                    if (currentIndex >= 0 && currentIndex < value.size) {
                        dailyEntity = sortedByDescending[currentIndex]
                        val lookDailyPagerAdapter =
                            LookDailyPagerAdapter(this@LookDailyActivity, sortedByDescending)
                        lookDailyBinding.viewPagerDaily.adapter = lookDailyPagerAdapter
                        lookDailyBinding.viewPagerDaily.setCurrentItem(currentIndex, false)
                        originalSignalPassword = dailyEntity.singlePassword.toString()
                        lookDailyBinding.viewPagerDaily.registerOnPageChangeCallback(object :
                            ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                super.onPageSelected(position)
                                currentIndex = position
                                dailyEntity = sortedByDescending[currentIndex]
                                originalSignalPassword = dailyEntity.singlePassword.toString()
                                originalSignalPasswordMap[dailyEntity.id!!] = originalSignalPassword
                                tempSignalPasswordMap[dailyEntity.id!!] = originalSignalPassword
                                invalidateOptionsMenu()
                            }
                        })

                        originalSignalPasswordMap = mutableMapOf(
                            dailyEntity.id!! to originalSignalPassword
                        )
                        tempSignalPasswordMap = mutableMapOf(
                            dailyEntity.id!! to originalSignalPassword
                        )
                    }
                } else finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_look_daily, menu)
        if (::originalSignalPasswordMap.isInitialized) {
            menu?.findItem(R.id.item_unlock)?.isVisible =
                when {
                    originalSignalPasswordMap[dailyEntity.id] == "" -> {
                        false
                    }

                    else -> {
                        true
                    }
                }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        dailyViewModel.queryAllDaily().removeObservers(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_delete -> {
                when {
                    originalSignalPasswordMap[dailyEntity.id] == "" -> {
                        MaterialAlertDialogBuilder(this@LookDailyActivity)
                            .setMessage("确定永久删除这篇日记吗？")
                            .setPositiveButton(getString(R.string.sure)) { dialog, which ->
                                dailyViewModel.deleteDaily(dailyEntity)
                                finish()
                            }
                            .setNegativeButton(getString(R.string.cancel), null)
                            .create()
                            .show()
                    }

                    else -> {
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily.rootView,
                            "请先解锁以验证您的身份。"
                        )
                    }
                }
            }

            R.id.item_edit -> {
                when {
                    originalSignalPasswordMap[dailyEntity.id] == "" -> {
                        val intent = Intent()
                        intent.putExtra("daily_id", dailyEntity.id)
                        intent.putExtra("daily_title", dailyEntity.title)
                        intent.putExtra("daily_content", dailyEntity.content)
                        intent.putExtra("daily_date_time", dailyEntity.dateTime)
                        intent.putExtra(
                            "daily_backgroundColorIndex",
                            dailyEntity.backgroundColorIndex
                        )
                        intent.putExtra(
                            "single_password",
                            tempSignalPasswordMap[dailyEntity.id!!]
                        )
                        intent.setClass(this@LookDailyActivity, EditDailyActivity::class.java)
                        startActivity(intent)
                    }

                    else -> {
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily.rootView,
                            "请先解锁以验证您的身份。"
                        )
                    }
                }
            }

            R.id.item_copy -> {
                when {
                    originalSignalPasswordMap[dailyEntity.id] == "" -> {
                        dailyEntity.content?.let { CopyUtil.copyTextToClipboard(this, it) }
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily,
                            getString(R.string.copy_successful)
                        )
                    }

                    else -> {
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily.rootView,
                            "请先解锁以验证您的身份。"
                        )
                    }
                }

            }

            R.id.item_unlock -> {
                val inflate =
                    layoutInflater.inflate(R.layout.dialog_input_password_layout, null)
                val inputPasswordLayout =
                    inflate.findViewById<TextInputLayout>(R.id.input_password_layout)
                val inputPassword = inflate.findViewById<TextInputEditText>(R.id.input_password)
                inputPasswordLayout.hint = getString(R.string.unlocked)
                var singlePassword: String? = ""
                inputPassword.setText(singlePassword)
                MaterialAlertDialogBuilder(this@LookDailyActivity).apply {
                    setTitle(getString(R.string.unlocked))
                    setView(inflate)
                    setPositiveButton(
                        getString(R.string.sure),
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                singlePassword = inputPassword.text.toString()
                                val lookDailyPagerFragment =
                                    supportFragmentManager.findFragmentByTag("f${currentIndex}") as LookDailyPagerFragment
                                val hashSHA256 = HashUtil.hashSHA256(singlePassword.toString())
                                when (hashSHA256) {
                                    dailyEntity.singlePassword -> {
                                        originalSignalPasswordMap[dailyEntity.id!!] = ""
                                        tempSignalPasswordMap[dailyEntity.id!!] =
                                            inputPassword.text.toString()
                                        invalidateOptionsMenu()
                                        lookDailyPagerFragment.updateSinglePassword(hashSHA256)
                                    }

                                    else -> {
                                        SnackbarUtil.showSnackbarShort(
                                            lookDailyBinding.viewPagerDaily.rootView,
                                            "密码错误。"
                                        )
                                    }
                                }
                            }

                        })
                    setNegativeButton(
                        getString(R.string.forgot_password),
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                val inflate1 = LayoutInflater.from(this@LookDailyActivity)
                                    .inflate(R.layout.dialog_input_password_layout, null)
                                val inputPasswordLayout1 =
                                    inflate1.findViewById<TextInputEditText>(R.id.input_password)
                                inputPasswordLayout1.hint = getString(R.string.forgot_password)
                                MaterialAlertDialogBuilder(this@LookDailyActivity).apply {
                                    setTitle(getString(R.string.forgot_password))
                                    setView(inflate1)
                                    setPositiveButton(getString(R.string.sure),
                                        object : DialogInterface.OnClickListener {
                                            override fun onClick(
                                                dialog: DialogInterface?,
                                                which: Int
                                            ) {
                                                val key = sharedPreferences?.getString(
                                                    "forget_password_key",
                                                    ""
                                                )
                                                if (key != "") {
                                                    when {
                                                        HashUtil.hashSHA256(inputPasswordLayout1.text.toString()) == key -> {
                                                            dailyViewModel.updateDaily(
                                                                DailyEntity(
                                                                    dailyEntity.id,
                                                                    dailyEntity.title,
                                                                    dailyEntity.content,
                                                                    dailyEntity.dateTime,
                                                                    dailyEntity.backgroundColorIndex,
                                                                    ""
                                                                )
                                                            )
                                                            SnackbarUtil.showSnackbarShort(
                                                                lookDailyBinding.viewPagerDaily.rootView,
                                                                "密码已清除。"
                                                            )
                                                        }

                                                        else -> SnackbarUtil.showSnackbarShort(
                                                            lookDailyBinding.viewPagerDaily.rootView,
                                                            "密钥错误。"
                                                        )
                                                    }
                                                } else SnackbarUtil.showSnackbarShort(
                                                    lookDailyBinding.viewPagerDaily.rootView,
                                                    "请先设置密钥。"
                                                )
                                            }

                                        })
                                    setNegativeButton(getString(R.string.cancel), null)
                                    create()
                                    show()
                                }
                            }

                        })
                    setNeutralButton(getString(R.string.cancel), null)
                        .setCancelable(false)
                        .create()
                    show()
                }
            }

            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(VIEW_PAGER_INDEX, currentIndex)
    }

    override fun onPause() {
        super.onPause()
        currentIndex = lookDailyBinding.viewPagerDaily.currentItem
    }

    /**
     * 初始化偏好
     */
    private fun initSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private var sharedPreferences: SharedPreferences? = null
}

private const val VIEW_PAGER_INDEX = "viewPagerIndex"