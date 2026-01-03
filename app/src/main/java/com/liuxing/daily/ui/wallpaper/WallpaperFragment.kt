package com.liuxing.daily.ui.wallpaper

import android.app.Activity.RESULT_OK
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.slider.Slider
import com.liuxing.daily.R
import com.liuxing.daily.databinding.FragmentWallpaperBinding
import com.liuxing.daily.extension.checkFileExistsToPath
import com.liuxing.daily.extension.getFileMD5
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.ui.settings.SettingsActivity
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WallpaperFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WallpaperFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val binding by lazy {
        FragmentWallpaperBinding.inflate(layoutInflater)
    }
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WallpaperFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = WallpaperFragment().apply {
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
        addWallpaper()
        deleteWallpaper()
        initSlider()
    }

    /**
     * 添加壁纸
     */
    private fun addWallpaper() {
        binding.btnAddWallpaper.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            addImageLauncher.launch(intent)
        }
    }

    private val addImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            val uri = result.data?.data ?: return@registerForActivityResult
            lifecycleScope.launch {
                val saveResult = saveWallpaper(uri)
                saveResult.onSuccess { file ->
                    val newFileMD5 = file.getFileMD5()
                    val wallpaperMD5 = (requireActivity() as QRXActivity).getWallpaperMD5()
                    if (newFileMD5 != wallpaperMD5) {
                        (requireActivity() as SettingsActivity).qrx()
                    }
                    hintCloseImageDisplayDialog()
                }.onFailure {
                    MaterialAlertDialogUtil.showDialog(
                        requireContext(),
                        message = getString(R.string.add_failed),
                        positiveText = getString(R.string.sure)
                    )
                }
            }
        }

    private suspend fun saveWallpaper(uri: Uri): Result<File> = withContext(Dispatchers.IO) {
        try {
            val type = requireContext().contentResolver.getType(uri)?.lowercase()
                ?: return@withContext Result.failure(Exception("Invalid type"))

            if (!type.startsWith("image/")) {
                return@withContext Result.failure(Exception("Not an image"))
            }

            val inputStream = requireContext().contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Open stream failed"))

            val bitmap = BitmapFactory.decodeStream(inputStream)

            val appDir = requireContext().getExternalFilesDir(null)
            val wallpaperDir = File(appDir, "Wallpaper").apply { mkdirs() }

            val imageFile = File(wallpaperDir, "wallpaper.jpg")
            FileOutputStream(imageFile).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            Result.success(imageFile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 提示关闭图片显示的对话框
     */
    private fun hintCloseImageDisplayDialog() {
        val imageDisplay =
            sharedPreferences.getBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, false)
        if (!imageDisplay && File(ConstUtil.WALLPAPER_PATH).exists()) {
            MaterialAlertDialogUtil.showDialog(
                requireContext(),
                message = getString(R.string.for_a_better_experience_do_you_want_to_turn_off_the_diary_list_image_display),
                positiveText = getString(R.string.sure),
                onPositive = {
                    sharedPreferences.edit {
                        putBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, true)
                        apply()
                    }
                },
                negativeText = getString(R.string.cancel)
            )
        }
    }

    /**
     * 删除壁纸
     */
    private fun deleteWallpaper() {
        binding.btnDeleteWallpaper.setOnClickListener {
            if (File(ConstUtil.WALLPAPER_PATH).checkFileExistsToPath()) {
                MaterialAlertDialogUtil.showDialog(
                    requireContext(),
                    message = getString(R.string.delete_wallpaper_message),
                    positiveText = getString(R.string.sure),
                    onPositive = {
                        FileUtil().deleteFile(ConstUtil.WALLPAPER_PATH)
                        (requireActivity() as SettingsActivity).qrx()
                    },
                    neutralText = getString(R.string.cancel)
                )
            }
        }
    }

    /**
     * 初始化 [Slider] 的 alpha、contentDescription和value.
     */
    private fun initSlider() {
        val wallpaperAlpha = sharedPreferences.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY, 0.15F)
        binding.slider.contentDescription =
            getString(R.string.slider_alpha_description, (wallpaperAlpha * 100f).toInt())
        binding.slider.value = wallpaperAlpha * 100f

        saveAlpha()
    }

    /**
     * 保存选择的壁纸 alpha.
     */
    private fun saveAlpha() {
        binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                slider.apply {
                    contentDescription = getString(R.string.slider_alpha_description, value.toInt())
                }
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val wallpaperAlpha = slider.value / 100f
                sharedPreferences.edit {
                    putFloat(ConstUtil.WALLPAPER_ALPHA_KEY, wallpaperAlpha)
                    apply()
                }
                (requireActivity() as QRXActivity).setWallpaperAlpha(wallpaperAlpha)
            }

        })
    }

}