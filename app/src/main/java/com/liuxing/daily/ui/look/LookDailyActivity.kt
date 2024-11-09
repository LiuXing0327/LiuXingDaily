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
import androidx.lifecycle.ViewModelProvider
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
import com.liuxing.daily.util.SnackbarUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.util.Date


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
        dailyViewModel = ViewModelProvider(this)[DailyViewModel::class.java]
    }

    /**
     * 加载日记
     */
    private fun loadDailyToViewPager(savedInstanceState: Bundle?) {
        dailyViewModel.queryAllDaily().observe(this, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                val filter = value.filter { !it.isDeleted }
                if (filter.isNotEmpty()) {
                    val sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this@LookDailyActivity)
                    val currentSortIndex = sharedPreferences?.getInt("daily_sort_by", 0)
                    val sortedByDescending = if (currentSortIndex == 1) {
                        filter.sortedBy {
                            DateUtil.getDateString(0, Date(it.dateTime!!))
                        }
                    } else {
                        filter.sortedByDescending {
                            DateUtil.getDateString(0, Date(it.dateTime!!))
                        }
                    }

                    if (currentIndex == 0) {
                        if (savedInstanceState != null) currentIndex =
                            savedInstanceState.getInt(VIEW_PAGER_INDEX, 0)
                        else {
                            val position = intent.getIntExtra("POSITION", 0)
                            if (position in filter.indices) {
                                val intentPosition = filter[position]
                                currentIndex =
                                    sortedByDescending.indexOfFirst { it.id == intentPosition.id }
                            }
                        }
                    }
                    if (currentIndex >= sortedByDescending.size) {
                        currentIndex = sortedByDescending.size - 1
                    }
                    if (currentIndex in sortedByDescending.indices) {
                        dailyEntity = sortedByDescending[currentIndex]
                        val lookDailyPagerAdapter =
                            LookDailyPagerAdapter(this@LookDailyActivity, sortedByDescending)
                        lookDailyBinding.viewPagerDaily.adapter = lookDailyPagerAdapter
                        lookDailyBinding.viewPagerDaily.setCurrentItem(
                            currentIndex,
                            false
                        )
                        originalSignalPassword = dailyEntity.singlePassword ?: ""
                        lookDailyBinding.viewPagerDaily.registerOnPageChangeCallback(object :
                            ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                super.onPageSelected(position)
                                currentIndex = position
                                dailyEntity = sortedByDescending[currentIndex]
                                originalSignalPassword = dailyEntity.singlePassword ?: ""
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
                originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() == false
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
                    originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() -> {
                        val moveInRecyclerBin =
                            sharedPreferences!!.getBoolean(
                                "switch_delete_to_recycler_bin_daily",
                                true
                            )
                        if (moveInRecyclerBin) {
                            MaterialAlertDialogBuilder(this).apply {
                                setMessage(getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin))
                                setPositiveButton(getString(R.string.sure)) { dialog, which ->
                                    dailyViewModel.updateDaily(
                                        DailyEntity(
                                            dailyEntity.id,
                                            dailyEntity.title,
                                            dailyEntity.content,
                                            dailyEntity.dateTime,
                                            dailyEntity.backgroundColorIndex,
                                            dailyEntity.singlePassword,
                                            dailyEntity.moodIndex,
                                            dailyEntity.weatherIndex,
                                            dailyEntity.dailyUUID,
                                            true
                                        )
                                    )
                                }
                                    .setNegativeButton(getString(R.string.cancel), null)
                                    .create()
                                    .show()
                            }

                        } else {
                            MaterialAlertDialogBuilder(this).apply {
                                setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                                setPositiveButton(getString(R.string.delete)) { _, _ ->
                                    dailyEntity.dailyUUID?.let {
                                        dailyViewModel.deletePathImageByDailyUuid(
                                            it
                                        )
                                    }
                                    dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                    dailyViewModel.deleteDaily(dailyEntity)
                                }
                                setNegativeButton(getString(R.string.recycler_bin)) { _, _ ->
                                    dailyViewModel.updateDaily(
                                        DailyEntity(
                                            dailyEntity.id,
                                            dailyEntity.title,
                                            dailyEntity.content,
                                            dailyEntity.dateTime,
                                            dailyEntity.backgroundColorIndex,
                                            dailyEntity.singlePassword,
                                            dailyEntity.moodIndex,
                                            dailyEntity.weatherIndex,
                                            dailyEntity.dailyUUID,
                                            true
                                        )
                                    )
                                }
                                setNeutralButton(getString(R.string.cancel), null)
                                create()
                                show()
                            }
                        }
                    }

                    else -> {
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily.rootView,
                            getString(R.string.please_unlock_it_first_to_verify_your_identity)
                        )
                    }
                }
            }

            R.id.item_edit -> {
                when {
                    originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() -> {
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
                        intent.putExtra("mood_index", dailyEntity.moodIndex)
                        intent.putExtra("weather_index", dailyEntity.weatherIndex)
                        intent.putExtra("daily_uuid", dailyEntity.dailyUUID)
                        intent.setClass(this@LookDailyActivity, EditDailyActivity::class.java)
                        startActivity(intent)
                    }

                    else -> {
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily.rootView,
                            getString(R.string.please_unlock_it_first_to_verify_your_identity)
                        )
                    }
                }
            }

            R.id.item_copy -> {
                when {
                    originalSignalPasswordMap[dailyEntity.id].isNullOrEmpty() -> {
                        dailyEntity.content?.let { CopyUtil.copyTextToClipboard(this, it) }
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily,
                            getString(R.string.copy_successful)
                        )
                    }

                    else -> {
                        SnackbarUtil.showSnackbarShort(
                            lookDailyBinding.viewPagerDaily.rootView,
                            getString(R.string.please_unlock_it_first_to_verify_your_identity)
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
                                            getString(R.string.wrong_password)
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
                                                                    "",
                                                                    dailyEntity.moodIndex,
                                                                    dailyEntity.weatherIndex,
                                                                    dailyEntity.dailyUUID
                                                                )
                                                            )
                                                            SnackbarUtil.showSnackbarShort(
                                                                lookDailyBinding.viewPagerDaily.rootView,
                                                                getString(R.string.the_password_has_been_cleared)
                                                            )
                                                        }

                                                        else -> SnackbarUtil.showSnackbarShort(
                                                            lookDailyBinding.viewPagerDaily.rootView,
                                                            getString(R.string.the_key_is_incorrect)
                                                        )
                                                    }
                                                } else SnackbarUtil.showSnackbarShort(
                                                    lookDailyBinding.viewPagerDaily.rootView,
                                                    getString(R.string.please_set_key)
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