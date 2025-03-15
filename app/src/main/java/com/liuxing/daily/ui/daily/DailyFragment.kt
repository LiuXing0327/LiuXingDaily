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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailyAdapter
import com.liuxing.daily.databinding.FragmentDailyBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.ui.look.LookDailyActivity
import com.liuxing.daily.util.FileUtil
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
    private var sharedPreferences: SharedPreferences? = null

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
        loadDailyData()
        setRecyclerViewItemOnClick()
        setRecyclerViewItemOnLongClick()
    }

    /**
     * 加载日记数据
     */
    private fun loadDailyData(){
        queryAllDaily = dailyViewModel.queryAllDaily()
        queryAllDaily.observe(viewLifecycleOwner, object : Observer<List<DailyEntity>> {
            override fun onChanged(value: List<DailyEntity>) {
                dailyAdapter.setDailyList(requireContext(), value,dailyViewModel,viewLifecycleOwner)
                dailyList = value
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
     * 设置列表长按事件
     */
    private fun setRecyclerViewItemOnLongClick(){
        dailyAdapter.setOnItemLongClickListener(object : OnItemLongClickListener{
            override fun onItemLongOnClick(position: Int) {
                val moveInRecyclerBin =
                    sharedPreferences!!.getBoolean("switch_delete_to_recycler_bin_daily", true)
                val dailyEntity = dailyList.filter { !it.isDeleted }[position]
                val neutralButtonText =
                    if (dailyEntity.isPinned) getString(R.string.cancel_pinned) else getString(R.string.pinned)
                if (moveInRecyclerBin) {
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setMessage(getString(R.string.are_you_sure_this_journal_is_moving_to_the_recycle_bin))
                        setPositiveButton(getString(R.string.sure)) { _, _ ->
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
                                    true,
                                    dailyEntity.dailyLabel,
                                    isPinned = dailyEntity.isPinned
                                )
                            )
                        }
                            .setNegativeButton(getString(R.string.cancel), null)
                            .setNeutralButton(neutralButtonText) { _, _ ->
                                val isPinned = neutralButtonText == getString(R.string.pinned)
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
                                        false,
                                        dailyEntity.dailyLabel,
                                        isPinned = isPinned
                                    )
                                )
                            }
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
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathImageByDailyUuid(dailyEntity.dailyUUID.toString())
                                }
                            dailyViewModel.queryDailyVideoByUuid(dailyEntity.dailyUUID.toString())
                                .observe(viewLifecycleOwner) { dailyVideoList ->
                                    val existingVideoPaths =
                                        dailyVideoList.map { it.videoPath }.toSet()
                                    if (existingVideoPaths.isNotEmpty()) {
                                        val list = existingVideoPaths.toList()
                                        list.forEach {
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathVideoByDailyUuid(dailyEntity.dailyUUID.toString())
                                }
                            dailyViewModel.queryDailyAudioByUuid(dailyEntity.dailyUUID.toString())
                                .observe(viewLifecycleOwner) { dailyAudioList ->
                                    val existingAudioPaths =
                                        dailyAudioList.map { it.audioPath }.toSet()
                                    if (existingAudioPaths.isNotEmpty()) {
                                        val list = existingAudioPaths.toList()
                                        list.forEach {
                                            if (fileUtil.checkFileExists(it!!)) {
                                                fileUtil.deleteFile(it)
                                            }
                                        }
                                    }
                                    dailyViewModel.deletePathAudioByDailyUuid(dailyEntity.dailyUUID.toString())
                                }
                                    dailyViewModel.deleteDaily(dailyEntity)
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
                                    true,
                                    dailyEntity.dailyLabel,
                                    isPinned = dailyEntity.isPinned
                                )
                            )
                        }

                        setNeutralButton(neutralButtonText) { _, _ ->
                            val isPinned = neutralButtonText == getString(R.string.pinned)
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
                                    false,
                                    dailyEntity.dailyLabel,
                                    isPinned = isPinned
                                )
                            )
                        }
                        create()
                        show()
                    }
                }
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
        if (headerYearMonth != dailyAdapter.headerYearMonth
        ) {
            loadDailyData()
        }
    }
}