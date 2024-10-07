package com.liuxing.daily.ui.daily

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailyAdapter
import com.liuxing.daily.adapter.DailySearchAdapter
import com.liuxing.daily.databinding.FragmentDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.add.AddDailyActivity
import com.liuxing.daily.ui.edit.EditDailyActivity
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.ui.settings.SettingsActivity
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Collections


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DailyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DailyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var fragmentDailyBinding: FragmentDailyBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var queryAllDaily: LiveData<List<DailyEntity>>
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var dailySearchAdapter: DailySearchAdapter
    private var dailyList: List<DailyEntity> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentDailyBinding = FragmentDailyBinding.inflate(layoutInflater)
        return fragmentDailyBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DailyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = DailyFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        // 添加返回键回调
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(),
            onBackPressedCallback
        )
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        searchViewShowingStatusBarColor(false)
        searchViewFocus()
        floatingOnClick()
        initViewModel()
        initRecyclerView()
        initSearchRecyclerView()
        initSearchView()
        setSearchRecyclerViewData("")
        initSearchBar()
    }

    /**
     * 搜索视图的焦点监听
     */
    private fun searchViewFocus() {
        fragmentDailyBinding.searchView.editText.setOnFocusChangeListener { v, hasFocus ->
            searchViewShowingStatusBarColor(hasFocus)
        }
    }

    /**
     * 搜索视图显示？不显示的状态栏颜色
     *
     * @param showing 是否显示
     */
    private fun searchViewShowingStatusBarColor(showing: Boolean) {
        // 获取主题属性值
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(
            R.attr.searchViewShowingColor, typedValue, true
        )
        FollowPatternSetColor(typedValue.data)
        when {
            showing -> {
                requireActivity().window.statusBarColor = typedValue.data
            }

            else -> {
                requireActivity().window.statusBarColor =
                    ContextCompat.getColor(requireContext(), android.R.color.transparent)
            }
        }
    }

    /**
     * 是否打开搜索视图
     *
     * @return 搜索视图的开关值
     */
    private fun isOpenSearchView(): Boolean {
        return fragmentDailyBinding.searchView.isShowing
    }

    /**
     * 浅色模式：-1120012
     * 深色墨色：-13685706
     */
    private fun FollowPatternSetColor(ColorValue: Int) = if (ColorValue == -1120012) {
        requireActivity().window.getDecorView()
            .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    } else {
        requireActivity().window.getDecorView().setSystemUiVisibility(0)
    }


    /**
     * 监听返回键
     */
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    isOpenSearchView() -> fragmentDailyBinding.searchView.hide()
                    else -> requireActivity().finish()
                }
            }
        }

    /**
     * 浮动按钮点击事件
     */
    private fun floatingOnClick() {
        fragmentDailyBinding.floatingActionButton.setOnClickListener {
            IntentUtil.startActivity(
                requireContext(),
                AddDailyActivity::class.java
            )
        }
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        fragmentDailyBinding.recyclerView.layoutManager = linearLayoutManager
        dailyAdapter = DailyAdapter()
        fragmentDailyBinding.recyclerView.adapter = dailyAdapter
        setRecyclerViewData()
    }

    /**
     * 设置列表数据
     */
    private fun setRecyclerViewData() {
        queryAllDaily = dailyViewModel.queryAllDaily()
        queryAllDaily.observe(viewLifecycleOwner, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                dailyAdapter.setDailyList(requireContext(), value)
                dailyList = value
                setRecyclerViewItemOnClick()
            }
        })
    }

    /**
     * 设置列表点击事件
     */
    private fun setRecyclerViewItemOnClick() {
        dailyAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun OnItemClick(position: Int) {
                val intent = Intent()
                intent.setClass(requireContext(), LookDailyActivity::class.java)
                intent.putExtra("POSITION", position)
                requireActivity().startActivity(intent)
            }

        })
    }

    /**
     * 初始化搜索列表
     */
    private fun initSearchRecyclerView() {
        val linearLayoutManagerSearch = LinearLayoutManager(requireActivity())
        fragmentDailyBinding.searchRecyclerView.layoutManager = linearLayoutManagerSearch
        dailySearchAdapter = DailySearchAdapter()
        fragmentDailyBinding.searchRecyclerView.adapter = dailySearchAdapter
    }

    /**
     * 设置搜索列表数据
     */
    private fun setSearchRecyclerViewData(searchQuery: String) {
        val queryDaily = dailyViewModel.queryDaily("$searchQuery%")
        queryDaily.observe(viewLifecycleOwner, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                dailySearchAdapter.setDailyList(requireContext(), value)

                setSearchRecyclerViewItemOnClick()
            }
        })
    }

    /**
     * 设置搜索列表点击事件
     */
    private fun setSearchRecyclerViewItemOnClick() {
        dailySearchAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun OnItemClick(position: Int) {
                val intent = Intent()
                intent.setClass(requireContext(), LookDailyActivity::class.java)
                intent.putExtra("POSITION", position)
                requireActivity().startActivity(intent)
            }

        })
    }

    /**
     * 初始化搜索视图
     */
    private fun initSearchView() {
        fragmentDailyBinding.searchView.inflateMenu(R.menu.menu_search_daily)
        searchDaily()
    }

    /**
     * 搜索日记
     */
    private fun searchDaily() {
        // 点击键盘搜索事件
        fragmentDailyBinding.searchView.editText.setOnEditorActionListener { v, actionId, event ->
            setSearchRecyclerViewData(v.text.toString())
            true
        }

        // 点击搜索视图菜单搜索事件
        fragmentDailyBinding.searchView.setOnMenuItemClickListener { item ->
            when (item!!.itemId) {
                R.id.item_search -> setSearchRecyclerViewData(fragmentDailyBinding.searchView.text.toString())
            }
            true
        }
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(requireActivity().application)
    }

    /**
     * 初始化搜索栏
     */
    private fun initSearchBar() {
        fragmentDailyBinding.searchBar.inflateMenu(R.menu.menu_search_bar)
        searchBarMenuItemOnClick()
    }

    /**
     * 搜索栏菜单点击事件
     */
    private fun searchBarMenuItemOnClick() {
        fragmentDailyBinding.searchBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_settings -> IntentUtil.startActivity(
                    requireContext(),
                    SettingsActivity::class.java
                )

                R.id.item_import_daily -> {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.setType("text/*")
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    importDailyLauncher.launch(intent)
                }

                R.id.item_export_all_daily -> {
                    if (dailyList.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                        intent.setType("text/csv")
                        intent.putExtra(Intent.EXTRA_TITLE, "*daily*.csv")
                        exportAllDailyLauncher.launch(intent)
                    }
                }
            }
            true
        }
    }

    /**
     * 导入日记启动器
     */
    private val importDailyLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode != Activity.RESULT_OK) return
                    val data = result.data ?: return
                    val uri: Uri = data.data!!
                    CoroutineScope(Dispatchers.IO).launch {
                        val inputStream = requireActivity().contentResolver.openInputStream(uri)
                        val bufferedReader = BufferedReader(
                            InputStreamReader(inputStream)
                        )
                        val type = object : TypeToken<List<DailyEntity>>() {}.type
                        val gson = Gson()
                        val dailyList: List<DailyEntity> = gson.fromJson(bufferedReader, type)
                        bufferedReader.close()
                        Collections.reverse(dailyList)
                        for (dailyEntity in dailyList) {
                            dailyViewModel.insertDaily(dailyEntity)
                        }
                    }
                }
            }
        )

    /**
     * 导出所有日记启动器
     */
    private val exportAllDailyLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult) {
                if (result.resultCode != Activity.RESULT_OK) return
                val data = result.data ?: return
                val url = data.data!!
                dailyViewModel.queryAllDaily()
                    .observe(viewLifecycleOwner, object : Observer<List<DailyEntity>> {
                        override fun onChanged(value: List<DailyEntity>) {
                            val outputStream: OutputStream =
                                requireActivity().contentResolver.openOutputStream(url)!!
                            val bufferedWriter =
                                BufferedWriter(OutputStreamWriter(outputStream))
                            val gson = GsonBuilder()
                                .excludeFieldsWithoutExposeAnnotation()
                                .create();
                            bufferedWriter.write(gson.toJson(value))
                            bufferedWriter.close()
                        }

                    })
            }
        })

    override fun onResume() {
        super.onResume()
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (sharedPreferences.getBoolean(
                "switch_preference_header_display",
                true
            ) != dailyAdapter.headerYearMonth
        ) {
            setRecyclerViewData()
        }
    }
}