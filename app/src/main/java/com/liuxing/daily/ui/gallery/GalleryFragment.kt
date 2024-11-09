package com.liuxing.daily.ui.gallery

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.liuxing.daily.adapter.GalleryAdapter
import com.liuxing.daily.databinding.FragmentGalleryBinding
import com.liuxing.daily.util.FileUtil
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var galleryBinding: FragmentGalleryBinding
    private val imageList = mutableSetOf<File>()

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
        galleryBinding = FragmentGalleryBinding.inflate(layoutInflater)
        return galleryBinding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GalleryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GalleryFragment().apply {
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
    }

    /**
     * 初始化列表
     */
    private fun initRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        galleryBinding.recyclerView.layoutManager = gridLayoutManager
        loadImage()
        val galleryAdapter = GalleryAdapter(imageList.toList())
        galleryBinding.recyclerView.adapter = galleryAdapter
    }

    /**
     * 加载图片
     */
    private fun loadImage() {
        val fileUtil = FileUtil()
        val imageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imageDir?.let { dir ->
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && fileUtil.isImageFile(file)) {
                        imageList.add(file)
                    }
                }
            }
        }
    }
}