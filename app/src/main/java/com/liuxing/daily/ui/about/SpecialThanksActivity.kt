package com.liuxing.daily.ui.about

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liuxing.daily.R
import com.liuxing.daily.data.SpecialThanksData
import com.liuxing.daily.databinding.ActivitySpecialThanksBinding
import com.liuxing.daily.util.ThemeUtil


class SpecialThanksActivity : BaseSpecialThanksActivity() {

    private lateinit var specialThanksBinding: ActivitySpecialThanksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        specialThanksBinding = ActivitySpecialThanksBinding.inflate(layoutInflater)
        setContentView(specialThanksBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
                            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recycler_view)) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, 0, navigationBars.right, navigationBars.bottom)
            insets
        }
        initData()
        (this as BaseSpecialThanksActivity).initQRX(specialThanksBinding.wallpaper,specialThanksBinding.appBarLayout)
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        setActionBar()
        initRecyclerView()
    }

    /**
     * 设置工具栏
     */
    private fun setActionBar() {
        setSupportActionBar(specialThanksBinding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        specialThanksBinding.toolbar.title = getString(R.string.special_thanks)
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        val specialThanksDataList = setOf(
            SpecialThanksData(
                "zoyonsheng",
                "对醒悟推广的支持与帮助",
                "",
                0
            ),
            SpecialThanksData(
                "XuRuo",
                "对醒悟推广的支持与帮助",
                "",
                0
            ),
            SpecialThanksData(
                "南城双念",
                "对醒悟推广的支持与帮助",
                "",
                0
            )
        )
        (this as BaseSpecialThanksActivity).initRecyclerView(
            specialThanksBinding.recyclerView,
            specialThanksDataList.toList()
        )
    }
}