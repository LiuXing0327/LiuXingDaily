package com.liuxing.daily.ui.look

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.liuxing.daily.R
import com.liuxing.daily.databinding.FragmentLookDailyPagerBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.view.DailyTextView
import com.liuxing.daily.viewmodel.DailyViewModel
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ID = "ID"
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
    private var id: Long? = null
    private var title: String? = null
    private var dateTime: Long? = null
    private var content: String? = null
    private lateinit var binding: FragmentLookDailyPagerBinding
    private var backgroundColorIndex: Int? = 0
    private var singlePassword: String? = ""
    private var moodIndex: Int? = 0
    private var weatherIndex: Int? = 0
    private var dailyUuid: String? = ""
    private lateinit var dailyViewModel: DailyViewModel
    private lateinit var dailyTextView: DailyTextView
    private var mutableListOf: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getLong(ID)
            title = it.getString(TITLE)
            dateTime = it.getLong(DATE_TIME)
            content = it.getString(CONTENT)
            backgroundColorIndex = it.getInt(BACKGROUND_INDEX)
            singlePassword = it.getString(SINGLE_PASSWORD)
            moodIndex = it.getInt(MOOD_INDEX)
            weatherIndex = it.getInt(WEATHER_INDEX)
            dailyUuid = it.getString(DAILY_UUID)
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
            id: Long?,
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
                    id?.let { putLong(ID, it) }
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

    /**
     * 更新密码
     */
    fun updateSinglePassword(singlePassword: String?) {
        this.singlePassword = singlePassword
        if (singlePassword != this.singlePassword) {
            binding.tvTitle.text = "***"
            binding.tvContent.text = "***"
        } else {
            arguments?.getString(TITLE).also { binding.tvTitle.text = it }
            arguments?.getString(CONTENT).also { binding.tvContent.text = it }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dailyTextView = view.findViewById(R.id.tv_content)
        binding.tvTitle.visibility = if (title!!.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        when {
            !singlePassword.isNullOrEmpty() -> {
                binding.tvTitle.text = "***"
                dailyTextView.text = "***"
                binding.ivMood.visibility = View.GONE
                binding.ivWeather.visibility = View.GONE
            }
            else -> {
                binding.tvTitle.text = title
                dailyTextView.text = content
                binding.ivMood.visibility = moodIndex.let {
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
                binding.ivWeather.visibility = weatherIndex.let {
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
                dailyViewModel.queryDailyImageByUuid(dailyUuid!!)
                    .observe(
                        viewLifecycleOwner
                    ) { imageList ->
                        if (imageList.isNotEmpty()) {
                            mutableListOf = mutableSetOf()
                            imageList.forEach { dailyImageEntity ->
                                if (FileUtil().checkFileExists(dailyImageEntity.imagePath.toString())) {
                                    dailyImageEntity.imagePath?.let { imagePath ->
                                        mutableListOf.add(
                                            imagePath
                                        )
                                    }
                                }
                            }

                            dailyTextView.setImagePathList(
                                content!!,
                                mutableListOf.toList()
                            )

                            val currentContent = content ?: ""
                            val updatedContent = StringBuilder(currentContent)
                            val existingImagePaths = mutableListOf<String>()
                            imageList.forEach { dailyImageEntity ->
                                val imagePath = dailyImageEntity.imagePath.toString()
                                val imgTag = "<img src=\"$imagePath\"/>"
                                if (FileUtil().checkFileExists(imagePath)) {
                                    existingImagePaths.add(imagePath)
                                    if (!currentContent.contains(imgTag)) {
                                        if (updatedContent.isNotEmpty()) {
                                            updatedContent.append("\n")
                                        }
                                        updatedContent.append(imgTag)
                                    }
                                } else {
                                    dailyViewModel.deleteSelectPathImage(imagePath)
                                }
                            }
                            if (updatedContent.toString() != currentContent) {
                                dailyTextView.text = updatedContent.toString()
                                dailyViewModel.updateDaily(
                                    DailyEntity(
                                        id,
                                        title,
                                        updatedContent.toString(),
                                        dateTime,
                                        backgroundColorIndex,
                                        singlePassword,
                                        moodIndex,
                                        weatherIndex,
                                        dailyUuid,
                                        false
                                    )
                                )
                            }
                        }
                    }

            }
        }
        dailyTextView.setDailyUuid(dailyUuid!!)
        binding.tvDateTime.text = DateUtil.getDateString(2, Date(dateTime!!))
        val cardBackgroundColor = when (backgroundColorIndex) {
            1 -> ContextCompat.getColor(requireContext(), R.color.color_2)
            2 -> ContextCompat.getColor(requireContext(), R.color.color_3)
            3 -> ContextCompat.getColor(requireContext(), R.color.color_4)
            else -> android.R.color.transparent
        }
        binding.cardView.setCardBackgroundColor(cardBackgroundColor)

        "${title!!.length.plus(TextUtil.getWordCount(content!!))}${
            getString(
                R.string.word
            )
        }".also {
            binding.tvDailyCount.text = it
        }
    }

    override fun onResume() {
        super.onResume()
        val updatedContent = dailyTextView.checkImage(content.toString(), mutableListOf.toList())
        if (updatedContent != content) {
            dailyViewModel.updateDaily(
                DailyEntity(
                    id,
                    title,
                    updatedContent,
                    dateTime,
                    backgroundColorIndex,
                    singlePassword,
                    moodIndex,
                    weatherIndex,
                    dailyUuid,
                    false
                )
            )
        }
    }
}