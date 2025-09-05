package com.liuxing.daily.ui.gallery

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.appbar.AppBarLayout
import com.liuxing.daily.databinding.FragmentLookGalleryImageBinding
import com.liuxing.daily.extension.slide
import com.liuxing.daily.ui.immersive.ImmersiveActivity
import com.liuxing.daily.util.WindowUtil

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
        appBarLayout.slide(false)
        Handler(Looper.getMainLooper()).postDelayed({
            appBarLayout.visibility = View.GONE
        }, 300)
        (requireActivity() as ImmersiveActivity).enterImmersive()
    }

    /**
     * 退出沉侵式
     *
     * @param appBarLayout AppBarLayout
     */
    private fun exitImmersive(appBarLayout: AppBarLayout) {
        appBarLayout.visibility = View.VISIBLE
        appBarLayout.slide(true)
        val window = requireActivity().window
        (requireActivity() as ImmersiveActivity).exitImmersive()
        WindowUtil.followPatternSetColor(window, requireContext())
    }

}