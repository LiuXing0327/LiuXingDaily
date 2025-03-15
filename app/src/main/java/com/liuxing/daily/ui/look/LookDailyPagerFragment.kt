package com.liuxing.daily.ui.look

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.liuxing.daily.R
import com.liuxing.daily.databinding.FragmentLookDailyPagerBinding
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.ConstUtil.audioRegex
import com.liuxing.daily.util.ConstUtil.imageRegex
import com.liuxing.daily.util.ConstUtil.videoRegex
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.TextUtil
import com.liuxing.daily.view.DailyTextView
import com.liuxing.daily.viewmodel.DailyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
private const val DAILY_LABEL = "dailyLabel"
private const val IS_PINNED = "isPinned"

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
    private var imageList: MutableSet<String> = mutableSetOf()
    private var videoList: MutableSet<String> = mutableSetOf()
    private var audioList: MutableSet<String> = mutableSetOf()
    private var dailyLabel: String? = null
    private var isPinned: Boolean = false

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
            dailyLabel = it.getString(DAILY_LABEL)
            isPinned = it.getBoolean(IS_PINNED)
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
            dailyUUID: String?,
            dailyLabel: String?,
            pinned: Boolean
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
                    dailyLabel?.let { putString(DAILY_LABEL, it) }
                    putBoolean(IS_PINNED, pinned)
                }
            }
    }

    /**
     * 更新密码
     */
    fun updateSinglePassword(singlePassword: String?) {
        if (singlePassword != this.singlePassword) {
            this.singlePassword = singlePassword
            binding.tvTitle.text = "***"
            binding.tvContent.text = "***"
        } else {
            binding.tvTitle.text = title
            binding.tvContent.text = content
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dailyTextView = view.findViewById(R.id.tv_content)
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        initViewModel()
        setTitleVisibility()
        setDailyContent()
        setDailyBackgroundColor()
        setDailyUuid()
        setDailyDateTime()
        setDailyWords()
        setDailyData()
        setLabelVisibility()
    }

    /**
     * 初始化视图模型
     */
    private fun initViewModel() {
        dailyViewModel = DailyViewModel(requireActivity().application)
    }

    /**
     * 设置标题显示
     */
    private fun setTitleVisibility() {
        binding.tvTitle.visibility = if (title!!.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    /**
     * 设置日记内容
     */
    private fun setDailyContent() {
        if (!singlePassword.isNullOrEmpty()) {
            binding.tvTitle.text = "***"
            dailyTextView.text = "***"
            binding.ivMood.visibility = View.GONE
            binding.ivWeather.visibility = View.GONE
        } else {
            binding.tvTitle.text = title


            val updatedContent = StringBuilder(content ?: "")
            CoroutineScope(Dispatchers.IO).launch {
                val dailyImages =
                    dailyViewModel.queryDailyImageByUuidToList(dailyUuid!!)
                val dailyVideos =
                    dailyViewModel.queryDailyVideoByUuidToList(dailyUuid!!)
                val dailyAudios =
                    dailyViewModel.queryDailyAudioByUuidToList(dailyUuid!!)

                val validImagePaths = dailyImages.map { it.imagePath }.toSet()
                val validVideoPaths = dailyVideos.map { it.videoPath }.toSet()
                val validAudioPaths = dailyAudios.map { it.audioPath }.toSet()

                removeUselessTags(imageRegex,validImagePaths,updatedContent)
                removeUselessTags(videoRegex,validVideoPaths,updatedContent)
                removeUselessTags(audioRegex,validAudioPaths,updatedContent)

                // 更新content内容并设置标志，避免重复更新
                if (content != updatedContent.toString()) {
                    content = updatedContent.toString()
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
                            false,
                            dailyLabel,
                            isPinned = isPinned
                        )
                    )

                }
            }
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
        }
        binding.tvLabel.text = dailyLabel
    }

    private fun removeUselessTags(
        regex: Regex,
        validPaths: Set<String?>,
        updatedContent: StringBuilder
    ) {
        regex.findAll(updatedContent).forEach { matchResult ->
            val tagPath = matchResult.groupValues[1]
            if (!validPaths.contains(tagPath)) {
                val tagToRemove = matchResult.value
                val startIndex = updatedContent.indexOf(tagToRemove)
                if (startIndex != -1) {
                    updatedContent.replace(startIndex, startIndex + tagToRemove.length, "")
                }
            }
        }
    }

    /**
     * 设置日记背景颜色
     */
    private fun setDailyBackgroundColor() {
        val cardBackgroundColor = when (backgroundColorIndex) {
            1 -> ContextCompat.getColor(requireContext(), R.color.color_2)
            2 -> ContextCompat.getColor(requireContext(), R.color.color_3)
            3 -> ContextCompat.getColor(requireContext(), R.color.color_4)
            4 -> ContextCompat.getColor(requireContext(), R.color.color_5)
            5 -> ContextCompat.getColor(requireContext(), R.color.color_6)
            6 -> ContextCompat.getColor(requireContext(), R.color.color_7)
            else -> android.R.color.transparent
        }
        binding.cardView.setCardBackgroundColor(cardBackgroundColor)
    }

    /**
     * 设置日记Uuid
     */
    private fun setDailyUuid() {
        dailyTextView.setDailyUuid(dailyUuid!!)
    }

    /**
     * 设置日记的日期时间
     */
    private fun setDailyDateTime() {
        binding.tvDateTime.text = DateUtil.getDateString(0, Date(dateTime!!))
    }

    /**
     * 设置日记字数
     */
    private fun setDailyWords() {
        "${title!!.length.plus(TextUtil.getWordCount(content!!))}${
            getString(
                R.string.word
            )
        }".also {
            binding.tvDailyCount.text = it
        }
    }

    /**
     * 设置日记数据
     */
    private fun setDailyData() {
        fun <T> setDailyList(
            dailyList: List<T>,
            getPath: (T) -> String?,
            tagGenerator: (String) -> String
        ): MutableSet<String> {
            val pathSet = mutableSetOf<String>()
            val currentContent = content ?: ""
            val updatedContent = StringBuilder(currentContent)
            val existingPaths = mutableListOf<String>()

            dailyList.forEach { entity ->
                val path = getPath(entity).toString()
                if (FileUtil().checkFileExists(path)) {
                    pathSet.add(path)
                    existingPaths.add(path)
                    val tag = tagGenerator(path)
                    if (!currentContent.contains(tag)) {
                        if (updatedContent.isNotEmpty()) {
                            updatedContent.append("\n")
                        }
                        updatedContent.append(tag)
                    }
                } else {
                    dailyViewModel.deleteSelectPathImage(path)
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
                        false,
                        dailyLabel,
                        isPinned = isPinned
                    )
                )
            }

            // 从SharedPreferences获取更新后日记内容
            val sharedPreferences =
                requireActivity().getSharedPreferences("DAILY_CONTENT_UPDATE", Context.MODE_PRIVATE)
            val updateKey = "daily_update_content_$dailyUuid"
            val dailyUpdateContentText = sharedPreferences.getString(updateKey, "").orEmpty()
            // 确保不为空，以免死循环
            if (dailyUpdateContentText.isNotEmpty()) {
                // 如果保存的内容与最新内容不同，则重新更新
                if (dailyUpdateContentText != updatedContent.toString()) {
                    dailyViewModel.updateDaily(
                        DailyEntity(
                            id,
                            title,
                            dailyUpdateContentText,
                            dateTime,
                            backgroundColorIndex,
                            singlePassword,
                            moodIndex,
                            weatherIndex,
                            dailyUuid,
                            false,
                            dailyLabel,
                            isPinned = isPinned
                        )
                    )
                }

                // 直接清空，避免更新其它日记
                sharedPreferences.edit {
                    remove(updateKey)
                    apply()
                }
            }
            return pathSet
        }
        dailyViewModel.queryDailyVideoByUuid(dailyUuid!!).observe(viewLifecycleOwner) { videoList ->
            if (videoList.isNotEmpty()) {
                this.videoList = setDailyList(
                    videoList,
                    getPath = { it.videoPath },
                    tagGenerator = { path -> "<video src=\"$path\"/>" }
                )

                dailyTextView.setImagePathList(
                    content!!,
                    this.imageList.toList(),
                    this.videoList.toList(),
                    this.audioList.toList()
                )
            }
        }
        dailyViewModel.queryDailyImageByUuid(dailyUuid!!).observe(viewLifecycleOwner) { imageList ->
            if (imageList.isNotEmpty()) {
                this.imageList = setDailyList(
                    imageList,
                    getPath = { it.imagePath },
                    tagGenerator = { path -> "<img src=\"$path\"/>" }
                )

                dailyTextView.setImagePathList(
                    content!!,
                    this.imageList.toList(),
                    this.videoList.toList(),
                    this.audioList.toList()
                )
            }
        }
        dailyViewModel.queryDailyAudioByUuid(dailyUuid!!).observe(viewLifecycleOwner) { audioList ->
            if (audioList.isNotEmpty()) {
                this.audioList = setDailyList(
                    audioList,
                    getPath = { it.audioPath },
                    tagGenerator = { path -> "<audio src=\"$path\"/>" })

                dailyTextView.setImagePathList(
                    content!!,
                    this.imageList.toList(),
                    this.videoList.toList(),
                    this.audioList.toList()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 获取去除无效图片标签后的文本
        val contentWithoutInvalidImages =
            dailyTextView.checkImageExists(content.toString(), imageList.toList())
        // 获取去除无效视频标签后的文本
        val contentWithoutInvalidVideos =
            dailyTextView.checkVideoExists(contentWithoutInvalidImages, videoList.toList())
        // 获取去除无效音频标签后的文本
        val updatedContent =
            dailyTextView.checkAudioExists(contentWithoutInvalidVideos, audioList.toList())
        // 如果更新后的内容和原始内容不同，则进行更新
        if (updatedContent != content) {
            content = updatedContent
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
                    false,
                    dailyLabel,
                    isPinned = isPinned
                )
            )
        }
    }

    /**
     * 设置标签显示
     */
    private fun setLabelVisibility() {
        binding.lLabel.visibility = if (dailyLabel.isNullOrEmpty()) View.GONE else View.VISIBLE
    }
}