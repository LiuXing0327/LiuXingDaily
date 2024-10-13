package com.liuxing.daily.ui.daily

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.liuxing.daily.adapter.DailyAdapter
import com.liuxing.daily.databinding.FragmentDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.util.LogUtil
import com.liuxing.daily.viewmodel.DailyViewModel


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
        /*        requireActivity().onBackPressedDispatcher.addCallback(
                    requireActivity(),
                    onBackPressedCallback
                )*/
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        initViewModel()
        initRecyclerView()
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
        dailyViewModel = DailyViewModel(requireActivity().application)
    }

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