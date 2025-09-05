package com.liuxing.daily.ui.daily

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailyAdapter
import com.liuxing.daily.databinding.FragmentDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.DailyLikeFragment
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.listener.OnItemSelectedStateChangedListener
import com.liuxing.daily.ui.config.SystemBarController
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.ui.main.MainActivity
import com.liuxing.daily.util.BitmapUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DailyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DailyFragment : Fragment(),DailyLikeFragment {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var fragmentDailyBinding: FragmentDailyBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var queryAllDaily: LiveData<List<DailyEntity>>
    private lateinit var dailyAdapter: DailyAdapter
    private var dailyList: List<DailyEntity> = ArrayList()
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var mainActivity: MainActivity

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
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        initViewModel()
        initSharePreferences()
        initMainActivity()
        initRecyclerView()
    }

    private fun initMainActivity() {
        mainActivity = (requireActivity() as MainActivity)
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
        dailyAdapter.setOnItemSelectedStateChangedListener(object :
            OnItemSelectedStateChangedListener {
            override fun onSelectionChanged(position: Int) {
                val selectedItemsCount = dailyAdapter.getSelectedItemsCount()
                if (selectedItemsCount > 0 && dailyAdapter.selectMode) {
                    mainActivity.expandContextualToolbar()
                    mainActivity.setUpContextualToolbarTitle("$selectedItemsCount")
                    mainActivity.setUpContextualToolbarPinnedVisibility()
                    mainActivity.enableOnBack(true)
                } else {
                    dailyAdapter.selectMode = false
                    mainActivity.collapseContextualToolbar()
                    mainActivity.enableLightStatusBarWithAppBar()
                    mainActivity.enableOnBack(false)
                }
            }

        })

        fragmentDailyBinding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(-1) && dailyList.isNotEmpty() && SystemBarController.isLightStatusBarEnabled) {
                    val bitmap = mainActivity.getBitmap()
                    bitmap?.let {
                        mainActivity.setLightStausBarsFromBitmap(it)
                    }
                }
            }
        })
    }

    /**
     * 设置列表数据
     */
    private fun setRecyclerViewData() {
        loadDailyData()
        setRecyclerViewItemOnClick()
    }

    /**
     * 加载日记数据
     */
    private fun loadDailyData(){
        queryAllDaily = dailyViewModel.queryAllDaily()
        queryAllDaily.observe(viewLifecycleOwner, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                lifecycleScope.launch {
                    val uuids = withContext(Dispatchers.Default) {
                        value.mapNotNull { it.dailyUUID }
                    }
                    val imageMap = withContext(Dispatchers.IO) {
                        dailyViewModel.getImagePathForUuids(uuids)
                    }
                    withContext(Dispatchers.Main) {
                        dailyAdapter.setDailyList(requireContext(), value, imageMap)
                        dailyList = value
                    }
                }
            }
        })
    }

    /**
     * 设置列表点击事件
     */
    private fun setRecyclerViewItemOnClick() {
        dailyAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent()
                intent.setClass(requireContext(), LookDailyActivity::class.java)
                intent.putExtra("POSITION", position)
                requireActivity().startActivity(intent)
            }

        })
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = ViewModelProvider(this)[DailyViewModel::class.java]
    }

    /**
     * 初始化偏好
     */
    private fun initSharePreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        val headerYearMonth = sharedPreferences.getBoolean(
            "switch_preference_header_display",
            true
        )
        val textSize = sharedPreferences.getFloat(ConstUtil.TEXT_SIZE_KEY, 16F)
        val alpha = sharedPreferences.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY,0.15F)
        val imageDisplay =
            sharedPreferences.getBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, false)
        if (headerYearMonth != dailyAdapter.headerYearMonth
            || textSize != dailyAdapter.textSize || alpha != dailyAdapter.alpha
            || imageDisplay != dailyAdapter.imageDisplay
        ) {
            loadDailyData()
        }
    }

    fun getAdapter(): DailyAdapter = dailyAdapter
    override fun getDailyList(): List<DailyEntity> = dailyList
    override fun getSelectedItems(): List<String> = dailyAdapter.getSelectedItems()
    override fun clearSection() = dailyAdapter.clearSection()
    override fun selectAllItems() = dailyAdapter.selectAllItems()
    override fun getSelectMode(): Boolean = dailyAdapter.selectMode
    override fun isPinnedDisplay(): Boolean = true
}