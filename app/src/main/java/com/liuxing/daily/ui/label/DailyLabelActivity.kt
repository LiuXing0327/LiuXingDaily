package com.liuxing.daily.ui.label

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityDailyLabelBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.entity.DailyLabelEntity
import com.liuxing.daily.ui.add.AddDailyActivity
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.viewmodel.DailyViewModel

private const val DAILY_LABEL = "daily_label_label"

class DailyLabelActivity : QRXActivity() {

    private lateinit var dailyLabelBinding: ActivityDailyLabelBinding
    private lateinit var dailyViewModel: DailyViewModel
    private var dailyLabelList: List<DailyLabelEntity> = ArrayList()
    private var dailyList: List<DailyEntity> = ArrayList()
    private var label: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        dailyLabelBinding = ActivityDailyLabelBinding.inflate(layoutInflater)
        setContentView(dailyLabelBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, 0, navigationBars.right, navigationBars.bottom)
            insets
        }
        initData(savedInstanceState)
        (this as QRXActivity).init(
            dailyLabelBinding.wallpaper,
            dailyLabelBinding.appBarLayout
        )
    }

    /**
     * 初始化数据
     */
    private fun initData(savedInstanceState: Bundle?) {
        setDailyLabel(savedInstanceState)
        setActionBar()
        initViewModel()
        getDailyLabel()
        this.label?.let { setDailyLabel(it) }
        getDailyData()
        addDailyWithLabel()
    }

    /**
     * 设置日记标签
     *
     * @param label 标签
     */
    private fun setDailyLabel(label: String) {
        val dailyLabelFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as DailyLabelFragment
        dailyLabelFragment.getDailyLabel(label)
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(dailyLabelBinding.toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            dailyLabelBinding.toolbar.title = this@DailyLabelActivity.label
        }
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(this.application)
    }

    /**
     * 设置日记标签
     *
     * @param savedInstanceState 已保存的实例状态
     */
    private fun setDailyLabel(savedInstanceState: Bundle?) {
        label =
            if (savedInstanceState == null) intent.getStringExtra("daily_label_label") else savedInstanceState.getString(
                DAILY_LABEL
            )
    }

    /**
     * 添加带有标签的日记
     */
    private fun addDailyWithLabel() {
        dailyLabelBinding.floatingActionButton.setOnClickListener {
            IntentUtil.startActivity(this, AddDailyActivity::class.java, mapOf("dailyLabel" to  label))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_daily_label, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = intent.getLongExtra("daily_label_id", 0)
        val dailyLabelEntity = DailyLabelEntity(id, label)
        when (item.itemId) {

            R.id.item_rename -> {
                showLabelInputDialog(id)
            }

            R.id.item_delete -> {
                MaterialAlertDialogBuilder(this).apply {
                    setMessage(getString(R.string.are_you_sure_delete_label, label))
                    setPositiveButton(getString(R.string.sure)) { _, _ ->
                        dailyList.forEach { dailyEntity ->
                            if (dailyEntity.dailyLabel == this@DailyLabelActivity.label) {
                                val newDailyEntity = DailyEntity(
                                    dailyEntity.id,
                                    dailyEntity.title,
                                    dailyEntity.content,
                                    dailyEntity.dateTime,
                                    dailyEntity.backgroundColorIndex,
                                    dailyEntity.singlePassword,
                                    dailyEntity.moodIndex,
                                    dailyEntity.weatherIndex,
                                    dailyEntity.dailyUUID,
                                    dailyEntity.isDeleted,
                                    "",
                                    isPinned = dailyEntity.isPinned
                                )
                                dailyViewModel.updateDaily(newDailyEntity)
                            }
                        }
                        dailyViewModel.deleteDailyLabel(dailyLabelEntity)
                        finish()
                    }
                    setNegativeButton(getString(R.string.cancel), null)
                    create()
                    show()
                }
            }

            else -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 获取日记标签
     */
    private fun getDailyLabel() {
        dailyViewModel.queryAllDailyLabel().observe(this) { dailyLabelList ->
            this.dailyLabelList = dailyLabelList
        }
    }

    /**
     * 获取日记数据
     */
    private fun getDailyData() {
        dailyViewModel.queryAllDaily().observe(this) { dailyList ->
            this.dailyList = dailyList
        }
    }

    /**
     * 显示输入标签的对话框
     */
    private fun showLabelInputDialog(id: Long) {
        val view = layoutInflater.inflate(R.layout.dialog_input_label_layout, null)
        val inputLabel = view.findViewById<TextInputEditText>(R.id.input_label)
        val inputLabelLayout = view.findViewById<TextInputLayout>(R.id.input_label_layout)
        MaterialAlertDialogBuilder(this).apply {
            setTitle(getString(R.string.rename))
            setView(view)
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                val label = inputLabel.text.toString()
                if (label.isNotEmpty()) {
                    val dailyLabelEntity = DailyLabelEntity(id, label)
                    dailyList.forEach { dailyEntity ->
                        if (dailyEntity.dailyLabel == this@DailyLabelActivity.label) {
                            val newDailyEntity = DailyEntity(
                                dailyEntity.id,
                                dailyEntity.title,
                                dailyEntity.content,
                                dailyEntity.dateTime,
                                dailyEntity.backgroundColorIndex,
                                dailyEntity.singlePassword,
                                dailyEntity.moodIndex,
                                dailyEntity.weatherIndex,
                                dailyEntity.dailyUUID,
                                dailyEntity.isDeleted,
                                label,
                                isPinned = dailyEntity.isPinned
                            )
                            dailyViewModel.updateDaily(newDailyEntity)
                        }
                    }
                    setDailyLabel(label)
                    dailyViewModel.updateDailyLabel(dailyLabelEntity)
                    dailyLabelBinding.toolbar.title = dailyLabelEntity.label
                    this@DailyLabelActivity.label = dailyLabelEntity.label
                }
            }
            setNegativeButton(getString(R.string.cancel), null)
            val dialog = create()
            dialog.show()
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.isEnabled = false
            inputLabel.addTextChangedListener {
                val inputText = it.toString()
                val input = dailyLabelList.any { dailyLabelEntity ->
                    dailyLabelEntity.label == inputText
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(DAILY_LABEL, label)
    }
}