package com.liuxing.daily.ui.gallery

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.appbar.AppBarLayout
import com.liuxing.daily.databinding.FragmentLookGalleryImageBinding
import androidx.core.view.isVisible

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LookGalleryImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LookGalleryImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var lookGalleryImageBinding: FragmentLookGalleryImageBinding

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
        lookGalleryImageBinding = FragmentLookGalleryImageBinding.inflate(layoutInflater)
        return lookGalleryImageBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LookGalleryImageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LookGalleryImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*        Glide.with(requireContext()).load(arguments?.getString(ARG_PARAM1)).into(lookGalleryImageBinding.galleryImage)*/
        lookGalleryImageBinding.galleryImage.setImage(
            ImageSource.uri(
                arguments?.getString(
                    ARG_PARAM1
                )!!
            )
        )

        val activityLookGalleryImageBinding =
            (requireActivity() as LookGalleryImageActivity).lookGalleryImageBinding

        lookGalleryImageBinding.galleryImage.setOnClickListener {
            activityLookGalleryImageBinding.appBarLayout.let {
                if (it.isVisible) {
                    enterImmersive(it)
                    activityLookGalleryImageBinding.main.setBackgroundColor(Color.BLACK)
                } else {
                    exitImmersive(it)
                    activityLookGalleryImageBinding.main.setBackgroundColor(Color.TRANSPARENT)
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
    }
}