package com.liuxing.daily.ui.calendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.adapter.CalendarToDailyAdapter
import com.liuxing.daily.databinding.FragmentCalendarQueryDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import com.liuxing.daily.viewmodel.MainViewModel
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
    private lateinit var mainViewModel: MainViewModel
    private var yearMonthDay: String = ""

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
        dailyViewModel = ViewModelProvider(requireActivity())[DailyViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
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
        loadDailyData()
        setRecyclerViewItemOnClick()
        setRecyclerViewItemOnLongClick()
    }

    /**
     * 加载日记数据
     */
    private fun loadDailyData() {
        dailyViewModel.queryAllDaily()
            .observe(viewLifecycleOwner, object : Observer<List<DailyEntity>> {
                override fun onChanged(value: List<DailyEntity>) {
                    dailyList = value

                    calendarToDailyAdapter.setDailyList(
                        requireContext(),
                        dailyList,
                        yearMonthDay.ifEmpty {
                            DateUtil.getDateString(
                                0,
                                Date(fragmentCalendarQueryDailyBinding.calendarView.date)
                            ).substring(0, 10)
                        }, dailyViewModel, viewLifecycleOwner
                    )

                }
            })
    }

    /**
     * 跟随日历切换日记
     */
    private fun followCalendarChangeDaily() {
        fragmentCalendarQueryDailyBinding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val dateFormat =
                if (getString(R.string.daily) == "日记") "%04d/%02d/%02d" else "%04d-%02d-%02d"
            yearMonthDay = String.format(dateFormat, year, month + 1, dayOfMonth)

            mainViewModel.setYearMonthDay(yearMonthDay)

            calendarToDailyAdapter.setDailyList(
                requireContext(),
                dailyList,
                yearMonthDay,
                dailyViewModel,
                viewLifecycleOwner
            )
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        val headerYearMonth = sharedPreferences.getBoolean(
            "switch_preference_header_display",
            true
        )
        if (calendarToDailyAdapter.headerYearMonth != headerYearMonth
        ) {
            loadDailyData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dailyViewModel.queryAllDaily().removeObservers(viewLifecycleOwner)
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
                startActivity(intent)
            }

        })
    }

    /**
     * 设置列表长按事件
     */
    private fun setRecyclerViewItemOnLongClick(){
        calendarToDailyAdapter.setOnItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongOnClick(position: Int) {
                val dailyEntity = dailyList[position]
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                val moveInRecyclerBin =
                    sharedPreferences!!.getBoolean("switch_delete_to_recycler_bin_daily", true)
                if (moveInRecyclerBin) {
                    MaterialAlertDialogBuilder(requireContext()).apply {
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
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_journal_permanently))
                        setPositiveButton(getString(R.string.delete)) { _, _ ->
                            val fileUtil = FileUtil()
                            dailyViewModel.queryDailyImageByUuid(dailyEntity.dailyUUID.toString())
                                .observe(viewLifecycleOwner) { dailyImageList ->
                                    val existingImagePaths = dailyImageList.map { it.imagePath }.toSet()
                                    if (existingImagePaths.isNotEmpty()) {
                                        val list = existingImagePaths.toList()
                                        list.forEach {
                                            if (fileUtil.checkFileExists(it!!)) fileUtil.deleteFile(
                                                it
                                            )
                                        }
                                    }
                                    dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                    dailyViewModel.deleteDaily(dailyEntity)
                                }
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
        })
    }
}