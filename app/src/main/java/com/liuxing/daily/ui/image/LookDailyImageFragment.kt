package com.liuxing.daily.ui.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.appbar.AppBarLayout
import com.liuxing.daily.databinding.FragmentLookDailyImageBinding
import com.liuxing.daily.util.WindowUtil
import java.io.File
import java.io.FileOutputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LookDailyImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LookDailyImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var lookDailyImageBinding: FragmentLookDailyImageBinding

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
        lookDailyImageBinding = FragmentLookDailyImageBinding.inflate(layoutInflater)
        return lookDailyImageBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LookDailyImageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LookDailyImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
/*                Glide.with(requireContext()).load(arguments?.getString(ARG_PARAM1))
                    .into(lookDailyImageBinding.dailyImage)*/
       lookDailyImageBinding.dailyImage.setImage(ImageSource.uri(arguments?.getString(ARG_PARAM1)!!))

        val activityLookDailyImageBinding = (requireActivity() as LookDailyImageActivity).binding

        lookDailyImageBinding.dailyImage.setOnClickListener {
            activityLookDailyImageBinding.appBarLayout.let {
                if (it.isVisible) {
                   enterImmersive(it)
                    activityLookDailyImageBinding.main.setBackgroundColor(Color.BLACK)
                } else {
                   exitImmersive(it)
                    activityLookDailyImageBinding.main.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }

    /**
     * 进入沉侵式
     *
     * @param appBarLayout AppBarLayout
     */
    private fun enterImmersive(appBarLayout: AppBarLayout) {
        appBarLayout.visibility = View.GONE
        requireActivity().isImmersive = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            requireActivity().window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    /**
     * 退出沉侵式
     *
     * @param appBarLayout AppBarLayout
     */
    private fun exitImmersive(appBarLayout: AppBarLayout) {
        appBarLayout.visibility = View.VISIBLE
        requireActivity().isImmersive = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }

        WindowUtil.followPatternSetColor(requireActivity().window, requireContext())
    }
}