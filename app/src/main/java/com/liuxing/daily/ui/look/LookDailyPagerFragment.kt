package com.liuxing.daily.ui.look

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.liuxing.daily.R
import com.liuxing.daily.adapter.DailyImagePagerAdapter
import com.liuxing.daily.databinding.FragmentLookDailyPagerBinding
import com.liuxing.daily.entity.DailyImageEntity
import com.liuxing.daily.ui.image.LookDailyImageActivity
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.viewmodel.DailyViewModel
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TITLE = "title"
private const val DATE_TIME = "dateTime"
private const val CONTENT = "context"
private const val BACKGROUND_INDEX = "backgroundColorIndex"
private const val SINGLE_PASSWORD = "singlePassword"
private const val MOOD_INDEX = "moodIndex"
private const val WEATHER_INDEX = "weatherIndex"
private const val DAILY_UUID = "dailyUUID"

/**
 * A simple [Fragment] subclass.
 * Use the [LookDailyPagerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LookDailyPagerFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null
    private var dateTime: Long? = null
    private var content: String? = null
    private lateinit var binding: FragmentLookDailyPagerBinding
    private var singlePassword: String? = ""
    private var moodIndex: Int? = 0
    private var weatherIndex: Int? = 0
    private var dailyUuid: Int? = 0
    private lateinit var dailyViewModel: DailyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE)
            dateTime = it.getLong(DATE_TIME)
            content = it.getString(CONTENT)
            singlePassword = it.getString(SINGLE_PASSWORD)
            moodIndex = it.getInt(MOOD_INDEX)
            weatherIndex = it.getInt(WEATHER_INDEX)
            dailyUuid = it.getInt(DAILY_UUID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLookDailyPagerBinding.inflate(layoutInflater)
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param dateTime 日记日期时间
         * @param content 日记内容
         * @return A new instance of fragment LookDailyPagerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            title: String?,
            dateTime: Long?,
            content: String?,
            backgroundColorIndex: Int?,
            singlePassword: String?,
            moodInt: Int?,
            weatherIndex: Int?,
            dailyUUID: String?
        ) =
            LookDailyPagerFragment().apply {
                arguments = Bundle().apply {
                    title?.let { putString(TITLE, it) }
                    dateTime?.let { putLong(DATE_TIME, it) }
                    content?.let { putString(CONTENT, it) }
                    backgroundColorIndex?.let { putInt(BACKGROUND_INDEX, it) }
                    singlePassword?.let { putString(SINGLE_PASSWORD, it) }
                    moodInt?.let { putInt(MOOD_INDEX, it) }
                    weatherIndex?.let { putInt(WEATHER_INDEX, it) }
                    dailyUUID?.let { putString(DAILY_UUID, it) }
                }
            }
    }

    fun updateSinglePassword(singlePassword: String?) {
        this.singlePassword = singlePassword
        if (singlePassword != arguments?.getString(SINGLE_PASSWORD)) {
            binding.tvTitle.text = "***"
            binding.tvContent.text = "***"
        } else {
            arguments?.getString(TITLE).also { binding.tvTitle.text = it }
            arguments?.getString(CONTENT).also { binding.tvContent.text = it }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvTitle.visibility = if (arguments?.getString(TITLE)!!.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        when {
            arguments?.getString(SINGLE_PASSWORD) != "" -> {
                binding.tvTitle.text = "***"
                binding.tvContent.text = "***"
                binding.ivMood.visibility = View.GONE
                binding.ivWeather.visibility = View.GONE
                binding.nestedScrollableHost.visibility = View.GONE
            }
            else -> {
                arguments?.getString(TITLE).also { binding.tvTitle.text = it }
                arguments?.getString(CONTENT).also { binding.tvContent.text = it }
                binding.ivMood.visibility = arguments?.getInt(MOOD_INDEX).let {
                    if (it == 0 || it == null) View.GONE else {
                        binding.ivMood.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                ConstUtil.moodList[it.minus(1)]
                            )
                        )
                        View.VISIBLE
                    }
                }
                binding.ivWeather.visibility = arguments?.getInt(WEATHER_INDEX).let {
                    if (it == 0 || it == null) View.GONE else {
                        binding.ivWeather.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                ConstUtil.weatherList[it.minus(1)]
                            )
                        )
                        View.VISIBLE
                    }
                }
                dailyViewModel = DailyViewModel(requireActivity().application)
                arguments?.getString(DAILY_UUID).let {uuid ->
                    if (uuid == "" || uuid == null) binding.nestedScrollableHost.visibility =
                        View.GONE else {
                        dailyViewModel.queryDailyImageByUuid(uuid)
                            .observe(viewLifecycleOwner, object : Observer<List<DailyImageEntity>> {
                                override fun onChanged(value: List<DailyImageEntity>) {
                                    if (value.isNotEmpty()) {
                                        val mutableListOf = mutableSetOf<String>()
                                        value.forEach { dailyImageEntity ->
                                            if (FileUtil().checkFileExists(dailyImageEntity.imagePath.toString())) {
                                                dailyImageEntity.imagePath?.let { it1 ->
                                                    mutableListOf.add(
                                                        it1
                                                    )
                                                }
                                            }
                                        }
                                        val adapter =
                                            DailyImagePagerAdapter(mutableListOf.toList()) { position ->
                                                val intent = Intent(
                                                    requireContext(),
                                                    LookDailyImageActivity::class.java
                                                ).apply {
                                                    putExtra("look_daily_image_uuid",uuid)
                                                    putExtra("look_daily_image_position", position)
                                                }
                                                requireContext().startActivity(intent)
                                            }
                                        binding.imagePager.adapter = adapter
                                    } else {
                                        binding.nestedScrollableHost.visibility = View.GONE
                                    }
                                }
                            })
                    }
                }
            }
        }

        DateUtil.getDateString(2, Date(arguments?.getLong(DATE_TIME, 0)!!))
            .also { binding.tvDateTime.text = it }
        val cardBackgroundColor = when (arguments?.getInt(BACKGROUND_INDEX)) {
            1 -> ContextCompat.getColor(requireContext(), R.color.color_2)
            2 -> ContextCompat.getColor(requireContext(), R.color.color_3)
            3 -> ContextCompat.getColor(requireContext(), R.color.color_4)
            else -> android.R.color.transparent
        }
        binding.cardView.setCardBackgroundColor(cardBackgroundColor)

        "${arguments?.getString(TITLE)!!.length.plus(arguments?.getString(CONTENT)!!.length)}${
            getString(
                R.string.word
            )
        }".also {
            binding.tvDailyCount.text = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        arguments?.getString(DAILY_UUID)
            ?.let { dailyViewModel.queryDailyImageByUuid(it).removeObservers(this) }
    }
}