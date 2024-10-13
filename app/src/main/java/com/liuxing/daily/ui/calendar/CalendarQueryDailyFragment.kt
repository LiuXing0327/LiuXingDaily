package com.liuxing.daily.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.liuxing.daily.adapter.CalendarToDailyAdapter
import com.liuxing.daily.databinding.FragmentCalendarQueryDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarQueryDailyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarQueryDailyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var fragmentCalendarQueryDailyBinding: FragmentCalendarQueryDailyBinding
    private var dailyList: List<DailyEntity> = ArrayList()
    private lateinit var calendarToDailyAdapter: CalendarToDailyAdapter
    private lateinit var dailyViewModel: DailyViewModel

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
        fragmentCalendarQueryDailyBinding =
            FragmentCalendarQueryDailyBinding.inflate(layoutInflater)
        return fragmentCalendarQueryDailyBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalendarQueryDailyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarQueryDailyFragment().apply {
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
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(requireActivity().application)
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        fragmentCalendarQueryDailyBinding.recyclerView.layoutManager = linearLayoutManager
        calendarToDailyAdapter = CalendarToDailyAdapter()
        fragmentCalendarQueryDailyBinding.recyclerView.adapter = calendarToDailyAdapter
        setRecyclerViewData()
        followCalendarChangeDaily()
    }

    /**
     * 设置列表的数据
     */
    private fun setRecyclerViewData() {
        dailyViewModel.queryAllDaily()
            .observe(viewLifecycleOwner, object : Observer<List<DailyEntity>> {
                override fun onChanged(value: List<DailyEntity>) {
                    dailyList = value

                    calendarToDailyAdapter.setDailyList(
                        requireContext(),
                        dailyList,
                        DateUtil.getDateString(
                            2,
                            Date(fragmentCalendarQueryDailyBinding.calendarView.date)
                        ).substring(0, 10)
                    )
                    setRecyclerViewItemOnClick()
                }
            })
    }

    /**
     * 跟随日历切换日记
     */
    private fun followCalendarChangeDaily() {
        fragmentCalendarQueryDailyBinding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val yearMonthDay = String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth)
            calendarToDailyAdapter.setDailyList(requireContext(), dailyList, yearMonthDay)
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (calendarToDailyAdapter.headerYearMonth != sharedPreferences.getBoolean(
                "switch_preference_header_display",
                true
            )
        ) {
            setRecyclerViewData()
        }
    }

    /**
     * 设置列表点击事件
     */
    private fun setRecyclerViewItemOnClick() {
        calendarToDailyAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent()
                intent.setClass(requireContext(), LookDailyActivity::class.java)
                intent.putExtra("POSITION", position)
                LogUtil.d("$position")
                startActivity(intent)
            }

        })
    }
}