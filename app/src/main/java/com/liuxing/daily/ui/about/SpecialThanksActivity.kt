package com.liuxing.daily.ui.about

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.liuxing.daily.R
import com.liuxing.daily.adapter.SpecialThanksAdapter
import com.liuxing.daily.data.SpecialThanksData
import com.liuxing.daily.databinding.ActivitySpecialThanksBinding
import com.liuxing.daily.util.WindowUtil

class SpecialThanksActivity : AppCompatActivity() {

    private lateinit var specialThanksBinding: ActivitySpecialThanksBinding
    private lateinit var specialThanksAdapter: SpecialThanksAdapter
    private lateinit var typedValue: TypedValue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        specialThanksBinding = ActivitySpecialThanksBinding.inflate(layoutInflater)
        setContentView(specialThanksBinding.root)
        /*        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
                }*/
        typedValue = TypedValue()
        theme.resolveAttribute(
            R.attr.searchViewShowingColor, typedValue, true
        )
        WindowUtil.FollowPatternSetColor(window, typedValue.data)
        window.statusBarColor =
            ContextCompat.getColor(this@SpecialThanksActivity, android.R.color.transparent)
        initData()
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
        specialThanksBinding.toolbar.title = "特别鸣谢"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        specialThanksBinding.recyclerView.layoutManager = linearLayoutManager
        specialThanksAdapter = SpecialThanksAdapter()
        specialThanksBinding.recyclerView.adapter = specialThanksAdapter
        setRecyclerData()
        setScrollStatusColor()
    }

    /**
     * 设置列表数据
     */
    private fun setRecyclerData() {
        val specialThanksDataList = setOf(
            SpecialThanksData("zoyongsheng", "对醒悟推广的支持与帮助", 0),
            SpecialThanksData("XuRuo", "对醒悟推广的支持与帮助", 0),
            SpecialThanksData("南城双念", "对醒悟推广的支持与帮助", 0),
            SpecialThanksData(
                "Gson", "\nCopyright 2008 Google Inc.\n" +
                        "\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n" +
                        "You may obtain a copy of the License at\n" +
                        "\n" +
                        "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.", 1
            ),
            SpecialThanksData(
                "OkHttp", "\nCopyright 2019 Square, Inc.\n" +
                        "\n" +
                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n" +
                        "You may obtain a copy of the License at\n" +
                        "\n" +
                        "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.", 1
            )
        )
        specialThanksAdapter.setSpecialThanksList(specialThanksDataList.toList())
    }

    /**
     * 设置滚动后的颜色
     */
    private fun setScrollStatusColor() {
        specialThanksBinding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    window.statusBarColor =
                        ContextCompat.getColor(
                            this@SpecialThanksActivity,
                            android.R.color.transparent
                        )
                } else {
                    window.statusBarColor = typedValue.data
                }
            }
        })
    }

}