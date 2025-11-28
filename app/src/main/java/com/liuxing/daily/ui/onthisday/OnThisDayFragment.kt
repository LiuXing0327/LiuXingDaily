package com.liuxing.daily.ui.onthisday

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liuxing.daily.adapter.OnThisDayAdapter
import com.liuxing.daily.databinding.FragmentOnThisDayBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.config.SystemBarController
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.ui.main.MainActivity
import com.liuxing.daily.ui.settings.DailySettingsConst
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.SharedPreferencesUtil
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
 * Use the [OnThisDayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnThisDayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var onThisDayBinding: FragmentOnThisDayBinding
    private lateinit var onThisDayAdapter: OnThisDayAdapter
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailyList: List<DailyEntity>

    /**
     * 当前“是否显示星期”的开关
     *
     * 在 [onResume] 中会再次获取最新设置
     * 若与当前值不同，则更新为最新值并重新加载数据
     */
    private var currentShowWeek = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        onThisDayBinding = FragmentOnThisDayBinding.inflate(layoutInflater)
        return onThisDayBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OnThisDayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OnThisDayFragment().apply {
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
        initRecyclerView()
    }

    /**
     * 初始化[dailyViewModel]
     */
    private fun initViewModel() {
        dailyViewModel = ViewModelProvider(requireActivity())[DailyViewModel::class.java]
    }

    /**
     * 初始化 RecyclerView
     */
    private fun initRecyclerView() {
        onThisDayBinding.onThisDayRecycler.layoutManager = LinearLayoutManager(requireContext())
        onThisDayAdapter = OnThisDayAdapter()
        onThisDayBinding.onThisDayRecycler.adapter = onThisDayAdapter
        setRecyclerViewData()

        val mainActivity = (requireActivity() as MainActivity)
        onThisDayBinding.onThisDayRecycler.addOnScrollListener(object :
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
     * 获取日记数据
     */
    private fun loadDailyData() {
        dailyViewModel.queryAllDaily().observe(viewLifecycleOwner) { dailyList ->
            this.dailyList = dailyList
            lifecycleScope.launch {
                val uuids = withContext(Dispatchers.Default) {
                    dailyList.mapNotNull { it.dailyUUID }
                }
                val imageMap = withContext(Dispatchers.IO) {
                    dailyViewModel.getImagePathForUuids(uuids)
                }
                withContext(Dispatchers.Main) {
                    onThisDayAdapter.setDailyList(
                        requireContext(),
                        dailyList,
                        DateUtil.getDateString(0, DateUtil.getCurrentDate()).substring(5, 10),
                        imageMap
                    )
                }
            }
        }
    }

    /**
     * 设置列表的点击事件
     */
    private fun setRecyclerViewItemOnClick() {
        onThisDayAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent()
                intent.setClass(requireContext(), LookDailyActivity::class.java)
                intent.putExtra("POSITION", position)
                requireActivity().startActivity(intent)
            }

        })
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
        if (headerYearMonth != onThisDayAdapter.headerYearMonth
            || textSize != onThisDayAdapter.textSize || alpha != onThisDayAdapter.alpha
            || imageDisplay != onThisDayAdapter.imageDisplay
        ) {
            loadDailyData()
        }

        val showWeek = SharedPreferencesUtil.getBoolean(
            requireContext(),
            DailySettingsConst.WEEK_SWITCH_KEY,
            true
        )
        if (showWeek != currentShowWeek) {
            currentShowWeek = showWeek
            loadDailyData()
        }
    }

}