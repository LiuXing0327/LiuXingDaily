package com.liuxing.daily.ui.label

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.liuxing.daily.adapter.DailyLabelAdapter
import com.liuxing.daily.databinding.FragmentDailyLabelBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.viewmodel.DailyViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DailyLabelFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DailyLabelFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var dailyLabelBinding: FragmentDailyLabelBinding
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailyLabelAdapter: DailyLabelAdapter
    private var dailyList: List<DailyEntity> = ArrayList()
    private var label: String = ""

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
        dailyLabelBinding = FragmentDailyLabelBinding.inflate(layoutInflater)
        return dailyLabelBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DailyLabelFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DailyLabelFragment().apply {
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
        initRecyclerView()
        initViewModel()
        setDailyData()
        setLabelOnItemClick()
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        dailyLabelBinding.recyclerView.layoutManager = linearLayoutManager
        dailyLabelAdapter = DailyLabelAdapter()
        dailyLabelBinding.recyclerView.adapter = dailyLabelAdapter
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(requireActivity().application)
    }

    /**
     * 设置日记数据
     */
    private fun setDailyData() {
        dailyViewModel.queryAllDaily().observe(viewLifecycleOwner) { dailyList ->
            this.dailyList = dailyList
            loadDailyData()
        }
    }

    /**
     * 加载日记数据
     */
    private fun loadDailyData() {
        dailyLabelAdapter.setDailyList(
            requireContext(),
            dailyList,
            label,
            dailyViewModel,
            viewLifecycleOwner
        )
    }
    /**
     * 设置点击事件
     */
    private fun setLabelOnItemClick() {
        dailyLabelAdapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onItemClick(position: Int) {
                val intent = Intent()
                intent.setClass(requireContext(), LookDailyActivity::class.java)
                intent.putExtra("POSITION", position)
                requireActivity().startActivity(intent)
            }
        })
    }

    /**
     * 获取日记标签
     *
     * @param label 日记标签
     */
    fun getDailyLabel(label: String) {
        this.label = label
    }
}