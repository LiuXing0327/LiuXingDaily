package com.liuxing.daily.ui.look

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.liuxing.daily.R
import com.liuxing.daily.databinding.FragmentLookDailyPagerBinding
import com.liuxing.daily.util.DateUtil
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TITLE = "title"
private const val DATE_TIME = "dateTime"
private const val CONTENT = "context"
private const val BACKGROUND_INDEX = "backgroundColorIndex"
private const val SINGLE_PASSWORD = "singlePassword"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE)
            dateTime = it.getLong(DATE_TIME)
            content = it.getString(CONTENT)
            singlePassword = it.getString(SINGLE_PASSWORD)
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
            singlePassword: String?
        ) =
            LookDailyPagerFragment().apply {
                arguments = Bundle().apply {
                    title?.let { putString(TITLE, it) }
                    dateTime?.let { putLong(DATE_TIME, it) }
                    content?.let { putString(CONTENT, it) }
                    backgroundColorIndex?.let { putInt(BACKGROUND_INDEX, it) }
                    singlePassword?.let { putString(SINGLE_PASSWORD, it) }
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
        arguments?.getString(TITLE).also { binding.tvTitle.text = it }
        arguments?.getString(CONTENT).also { binding.tvContent.text = it }
        DateUtil.getDateString(2, Date(arguments?.getLong(DATE_TIME, 0)!!))
            .also { binding.tvDateTime.text = it }
        val cardBackgroundColor = when (arguments?.getInt(BACKGROUND_INDEX)) {
            1 -> ContextCompat.getColor(requireContext(), R.color.color_2)
            2 -> ContextCompat.getColor(requireContext(), R.color.color_3)
            3 -> ContextCompat.getColor(requireContext(), R.color.color_4)
            else -> android.R.color.transparent
        }
        binding.cardView.setCardBackgroundColor(cardBackgroundColor)

        "${arguments?.getString(TITLE)!!.length.plus(arguments?.getString(CONTENT)!!.length)}字".also {
            binding.tvDailyCount.text = it
        }
    }


}