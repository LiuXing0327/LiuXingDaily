package com.liuxing.daily.ui.about

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liuxing.daily.R
import com.liuxing.daily.data.SpecialThanksData
import com.liuxing.daily.databinding.ActivityOpenSourceBinding
import com.liuxing.daily.util.ThemeUtil

class OpenSourceActivity : BaseSpecialThanksActivity() {

    private val binding: ActivityOpenSourceBinding by lazy {
        ActivityOpenSourceBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        setContentView(binding.root)
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
        (this as BaseSpecialThanksActivity).initQRX(binding.wallpaper,binding.appBarLayout)
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = getString(R.string.open_source_libraries)
    }


    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        val specialThanksDataList = setOf(
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
                        "limitations under the License.\n",
                "https://github.com/google/gson",
                1
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
                        "limitations under the License.\n",
                "https://github.com/square/okhttp",
                1
            ),
            SpecialThanksData(
                "Glide",
                "\nBSD, part MIT and Apache 2.0.\n",
                "https://github.com/bumptech/glide",
                1
            ),
            SpecialThanksData(
                "PhotoView", "\nCopyright 2018 Chris Banes\n" +
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
                        "limitations under the License.\n",
                "https://github.com/Baseflow/PhotoView",
                1
            ),
            SpecialThanksData(
                "subsampling-scale-image-view",
                "\nCopyright 2018 David Morrissey, and licensed under the Apache License, " +
                        "Version 2.0. No attribution is necessary but it's very much appreciated. Star this project if you like it!\n",
                "https://github.com/davemorrissey/subsampling-scale-image-view",
                1
            ),

            SpecialThanksData(
                "sardine.android",
                "\nApache 2.0 License.\n",
                "https://github.com/thegrizzlylabs/sardine-android",
                1
            ),

            SpecialThanksData(
                "zip4j",
                "\nApache 2.0 License.\n",
                "https://github.com/srikanth-lingala/zip4j",
                1
            ),

            SpecialThanksData(
                "ColorPickerView",
                "\nCopyright [2025] [LiuXing]\n" +
                        "\n" +
                        "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "   you may not use this file except in compliance with the License.\n" +
                        "   You may obtain a copy of the License at\n" +
                        "\n" +
                        "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "   Unless required by applicable law or agreed to in writing, software\n" +
                        "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "   See the License for the specific language governing permissions and\n" +
                        "   limitations under the License.\n",
                "https://github.com/LiuXing0327/ColorPickerView",
                1
            )
        )
        (this as BaseSpecialThanksActivity).initRecyclerView(
            binding.recyclerView,
            specialThanksDataList.toList()
        )
    }
}