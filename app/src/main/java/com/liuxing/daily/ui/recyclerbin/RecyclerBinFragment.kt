package com.liuxing.daily.ui.recyclerbin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liuxing.daily.R
import com.liuxing.daily.adapter.RecyclerBinAdapter
import com.liuxing.daily.databinding.FragmentRecyclerBinBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.viewmodel.DailyViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecyclerBinFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecyclerBinFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerBinBinding: FragmentRecyclerBinBinding
    private lateinit var recyclerBinAdapter: RecyclerBinAdapter
    private lateinit var dailyViewModel: DailyViewModel
    private var dailyList: List<DailyEntity> = arrayListOf()

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
        recyclerBinBinding = FragmentRecyclerBinBinding.inflate(layoutInflater)
        return recyclerBinBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecyclerBinFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecyclerBinFragment().apply {
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
        initRecyclerBinView()
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = ViewModelProvider(this)[DailyViewModel::class.java]
    }

    /**
     * 初始化回收列表
     */
    private fun initRecyclerBinView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerBinBinding.recyclerBinView.layoutManager = linearLayoutManager
        recyclerBinAdapter = RecyclerBinAdapter()
        recyclerBinBinding.recyclerBinView.adapter = recyclerBinAdapter
        setRecyclerDaily()
        setRecyclerBinOnItemClick()
    }

    /**
     * 设置回收的日记
     */
    private fun setRecyclerDaily() {
        dailyViewModel.queryAllDaily().observe(viewLifecycleOwner) { recyclerBinDailyList ->
            dailyList = recyclerBinDailyList
            recyclerBinAdapter.setDailyList(
                requireContext(),
                recyclerBinDailyList,
                dailyViewModel,
                viewLifecycleOwner
            )
        }
    }

    /**
     * 设置回收站点击事件
     */
    private fun setRecyclerBinOnItemClick() {
        recyclerBinAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val dailyEntity = dailyList.filter { it.isDeleted }[position]
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(dailyEntity.title)
                    setMessage(getString(R.string.do_you_want_to_delete_or_restore_the_daily))
                    setPositiveButton(
                        getString(R.string.delete)
                    ) { dialog, which ->
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
                                val existingVideoPaths = dailyVideoList.map { it.videoPath }.toSet()
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
                                val existingAudioPaths = dailyAudioList.map { it.audioPath }.toSet()
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
                    setNegativeButton(getString(R.string.restore)) { dialog, which ->
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
                                false
                            )
                        )
                    }
                    setNeutralButton(getString(R.string.cancel), null)
                    create()
                    show()
                }
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
        if (headerYearMonth != recyclerBinAdapter.headerYearMonth
        ) {
            setRecyclerDaily()
        }
    }
}