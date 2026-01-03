/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.ui.settings

import android.app.Activity.RESULT_OK
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.extension.bindPreferenceAction
import com.liuxing.daily.extension.bindPreferenceToActivity
import com.liuxing.daily.extension.bindPreferenceToNavigation
import com.liuxing.daily.ui.about.AboutActivity
import com.liuxing.daily.ui.about.OpenSourceActivity
import com.liuxing.daily.ui.about.SpecialThanksActivity
import com.liuxing.daily.ui.appearance.AppearanceSettingsActivity
import com.liuxing.daily.ui.datamanagement.DataManagementActivity
import com.liuxing.daily.ui.privacy.PrivacyActivity
import com.liuxing.daily.ui.updatelog.UpdateLogActivity
import com.liuxing.daily.ui.webdav.WebDavBackupActivity
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.text.startsWith

class SettingsFragment : PreferenceFragmentCompat() {

    /**
     * 当前选中的应用锁选项索引，默认索引为 0
     */
    private var appLockOptionsIndex: Int = 0
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        bindPreferenceToActivity<AboutActivity>("about_preference")
        bindPreferenceToActivity<SpecialThanksActivity>("special_thanks_preference")
        bindPreferenceToActivity<OpenSourceActivity>("open_source_preference")
        bindPreferenceToActivity<UpdateLogActivity>("update_log_preference")
        bindPreferenceToActivity<AppearanceSettingsActivity>("appearance_preference")
        bindPreferenceToActivity<WebDavBackupActivity>("webdav_backup_preference")
        bindPreferenceToActivity<PrivacyActivity>("user_agreement_and_privacy_policy_preference")
        bindPreferenceToActivity<DataManagementActivity>("data_management_preference")
        bindPreferenceToActivity<DailySettingsActivity>("daily_settings_preference")
       // bindPreferenceToActivity<WallpaperActivity>("background_image_preference")

        bindPreferenceToNavigation("background_image_preference",R.id.wallpaperFragment)

        bindPreferenceAction("check_update_preference") {
            CheckAppUpdateUtil.checkUpdate(requireContext())
        }
        /*            bindPreferenceAction("background_image_preference"){
                        showWallpaperDialog()
                    }*/

        val textLineSpacingPreference =
            findPreference<Preference>("text_line_spacing_preference")
        val textLineSpacingValue =
            textLineSpacingValue(sharedPreferences)
        textLineSpacingPreference?.summary = "$textLineSpacingValue"
        bindPreferenceAction("text_line_spacing_preference"){
            textLineSpacingPreference?.let {
                showTextLineSpacingDialog(it)
            }
        }

        val appLockPreference = findPreference<Preference>("app_lock_preference")
        appLockOptionsIndex = sharedPreferences.getInt("app_lock_options_index", 0)
        val options = arrayOf(
            getString(R.string.close),
            getString(R.string.password),
            getString(R.string.pin)
        )
        appLockPreference?.summary = options[appLockOptionsIndex]
        appLockPreference?.setOnPreferenceClickListener {
            appLockOptionsIndex = sharedPreferences.getInt("app_lock_options_index", 0)
            showLockDialog(it,options)
            true
        }

        val textFontSizePreference = findPreference<Preference>("text_font_size_preference")
        val textSize = textSizeValue(sharedPreferences)
        textFontSizePreference?.summary = textSize.toString()
        textFontSizePreference?.icon = if (textSize >= 16) ContextCompat.getDrawable(
            requireContext(),
            R.drawable.outline_text_increase_24
        ) else ContextCompat.getDrawable(requireContext(), R.drawable.outline_text_decrease_24)
        textFontSizePreference?.setOnPreferenceClickListener {
            showTextFontSizeDialog(it,textSize)
            true
        }

        bindPreferenceAction("daily_lock_key_preference") {
            showDailyLockKeyDialog()
        }
    }

    /**
     * 显示壁纸对话框
     */
    private fun showWallpaperDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            val wallpaperAlpha =
                sharedPreferences.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY, 0.15F)
            val wallpaperLayout = layoutInflater.inflate(
                R.layout.set_wallpaper_layout,
                null
            )
            val wallpaper =
                wallpaperLayout.findViewById<ImageView>(R.id.wallpaper)
            if (File(ConstUtil.WALLPAPER_PATH).exists()) {
                val bitmap = BitmapFactory.decodeFile(ConstUtil.WALLPAPER_PATH)
                wallpaper.setImageBitmap(bitmap)
            }

            wallpaper.alpha = wallpaperAlpha
            val slider =
                wallpaperLayout.findViewById<Slider>(R.id.slider)
            slider.contentDescription =
                getString(R.string.slider_alpha_description, (wallpaperAlpha * 100f).toInt())
            slider.value = wallpaperAlpha * 100F
            var newAlpha = wallpaperAlpha
            slider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    newAlpha = value / 100F
                    wallpaper.alpha = newAlpha
                    slider.contentDescription =
                        getString(R.string.slider_alpha_description, value.toInt())
                }
            }
            setTitle(getString(R.string.wallpaper))
            setView(wallpaperLayout)
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                sharedPreferences.edit {
                    putFloat(ConstUtil.WALLPAPER_ALPHA_KEY, newAlpha)
                    apply()
                }
                val imageDisplay =
                    sharedPreferences.getBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, false)
                if(!imageDisplay && File(ConstUtil.WALLPAPER_PATH).exists()){
                    MaterialAlertDialogUtil.showDialog(requireContext(),
                        message = getString(R.string.for_a_better_experience_do_you_want_to_turn_off_the_diary_list_image_display), positiveText = getString(R.string.sure),
                        onPositive = {
                            sharedPreferences.edit {
                                putBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, true)
                                apply()
                            }
                            requireActivity().finish()
                            val intent = Intent(requireContext(),SettingsActivity::class.java)
                            val options = ActivityOptions.makeCustomAnimation(requireActivity(), R.anim.fade_in, R.anim.fade_out)
                            startActivity(intent, options.toBundle())
                        },
                        negativeText = getString(R.string.cancel))
                }
            }
            setNegativeButton(getString(R.string.add_image)) { _, _ ->
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                addImageLauncher.launch(intent)
            }
            setNeutralButton(getString(R.string.delete)) { _, _ ->
                FileUtil().deleteFile(ConstUtil.WALLPAPER_PATH)
            }
            create()
            show()
        }
    }

    private val addImageLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode != RESULT_OK) return
                    val data = result.data ?: return
                    val uri: Uri = data.data ?: return

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val type =
                                requireActivity().contentResolver.getType(uri)?.lowercase()
                            if (type != null && type.startsWith("image/")) {
                                val inputStream =
                                    requireActivity().contentResolver.openInputStream(uri)
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                val appDir = requireContext().getExternalFilesDir(null)
                                val wallpaperDir = File(appDir, "Wallpaper")
                                if (!wallpaperDir.exists()) {
                                    wallpaperDir.mkdirs()
                                }
                                val fileName = "wallpaper.jpg"
                                val imageFile = File(wallpaperDir, fileName)
                                val outStream = FileOutputStream(imageFile)
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                                outStream.flush()
                                outStream.close()
                                withContext(Dispatchers.Main) {
                                    showWallpaperDialog()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                MaterialAlertDialogUtil.showDialog(
                                    requireContext(),
                                    message = getString(R.string.add_failed),
                                    positiveText = getString(R.string.sure)
                                )
                            }
                        }
                    }

                }
            })

    /**
     * 获取文本行距
     *
     * @param sharedPreferences SharedPreferences
     * @return 文本行距，默认 0f
     */
    private fun textLineSpacingValue(sharedPreferences: SharedPreferences): Float {
        val textLineSpacingValue =
            sharedPreferences.getFloat("text_line_spacing_preference", 0F)
        return textLineSpacingValue
    }

    /**
     * 存储锁定信息
     *
     * @param password 锁定的密码
     * @param which 锁定选项的索引
     */
    private fun putLockInfo(
        sharedPreferences: SharedPreferences,
        which: Int,
        password: String = ""
    ) {
        sharedPreferences.edit {
            putString(
                "app_password",
                HashUtil.hashSHA256(password)
            )
            putInt("app_lock_options_index", which)
            apply()
        }
    }

    /**
     * 获取文本字体大小
     *
     * @param sharedPreferences SharedPreferences
     * @return 文本字体大小，默认 16f
     */
    private fun textSizeValue(sharedPreferences: SharedPreferences): Float =
        sharedPreferences.getFloat("text_font_size_preference", 16f)

    /**
     * 显示设置文本行间距的对话框
     *
     * @param preference Preference
     */
    private fun showTextLineSpacingDialog(preference: Preference) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        ).apply {
            val updateTextLineSpacingLayout =
                layoutInflater.inflate(R.layout.update_text_line_spacing_layout, null)
            val tvText =
                updateTextLineSpacingLayout.findViewById<MaterialTextView>(R.id.tv_text)
            tvText.textSize = textSizeValue(sharedPreferences)
            val slider = updateTextLineSpacingLayout.findViewById<Slider>(R.id.slider)
            val newLineSpacingValue =
                textLineSpacingValue(sharedPreferences)
            tvText.setLineSpacing(newLineSpacingValue, 1F)
            slider.value = newLineSpacingValue
            var newValue = 0F
            slider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    tvText.setLineSpacing(value, 1F)
                    newValue = value
                }
            }
            setTitle(getString(R.string.text_line_spacing))
            setView(updateTextLineSpacingLayout)
            setNeutralButton(getString(R.string.cancel), null)
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                sharedPreferences.edit {
                    putFloat("text_line_spacing_preference", newValue)
                    apply()
                }
                preference.summary =
                    "${textLineSpacingValue(sharedPreferences)}"
            }
            create()
            show()
        }
    }

    /**
     * 显示是否设置应用锁的对话框
     *
     * @param preference Preference
     * @param options 对话框的选项
     */
    private fun showLockDialog(preference: Preference, options: Array<String>) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.app_lock)
            setSingleChoiceItems(options, appLockOptionsIndex) { dialog, which ->
                if (which != 0) {
                    val isPassword = which == 1
                    val title =
                        if (isPassword) getString(R.string.password) else getString(R.string.pin)

                    var inputPassword: TextInputEditText? = null
                    var inputPasswordLayout: TextInputLayout? = null


                    val showDialog = MaterialAlertDialogUtil.showDialog(
                        requireContext(),
                        title,
                        layoutRes = R.layout.dialog_input_password_layout,
                        positiveText = getString(R.string.sure),
                        onPositive = {
                            if (inputPassword?.text.isNullOrEmpty()) {
                                return@showDialog
                            }
                            putLockInfo(
                                sharedPreferences,
                                which,
                                inputPassword?.text.toString()
                            )

                            preference.summary = options[which]

                            dialog.dismiss()
                        },
                        neutralText = getString(R.string.cancel),
                        onViewCreated = { view, _ ->
                            inputPassword = view.findViewById(R.id.input_password)

                            inputPasswordLayout = view.findViewById(R.id.input_password_layout)
                        }
                    )

                    if (isPassword) return@setSingleChoiceItems

                    inputPasswordLayout?.apply {
                        hint = ""
                        error = getString(R.string.the_pin_is_empty)
                    }

                    inputPassword?.inputType = InputType.TYPE_CLASS_NUMBER

                    val positiveButton = showDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton?.apply {
                        isEnabled = false
                        inputPassword?.addTextChangedListener {
                            val inputContext = it.toString()

                            isEnabled = inputContext.length >= 4

                            inputPasswordLayout?.error = when {

                                inputContext.isEmpty() -> getString(R.string.the_pin_is_empty)

                                inputContext.length < 4 -> getString(R.string.at_least_4_digits)

                                else -> null
                            }
                        }
                    }


                    /*                        MaterialAlertDialogBuilder(requireContext()).apply {
                                                val view = layoutInflater.inflate(
                                                    R.layout.dialog_input_password_layout,
                                                    null
                                                )
                                                val inputPassword =
                                                    view.findViewById<TextInputEditText>(R.id.input_password)
                                                setTitle(title)
                                                setView(view)
                                                setPositiveButton(
                                                    getString(R.string.sure)
                                                ) { _, _ ->
                                                    if (!inputPassword.text.isNullOrEmpty()) {
                                                        putLockInfo(
                                                            sharedPreferences,
                                                            which,
                                                            inputPassword.text.toString()
                                                        )
                                                        preference.summary = getString(R.string.enabled)
                                                    }
                                                }
                                                setNeutralButton(getString(R.string.cancel), null)
                                                create()
                                                show()
                                            }*/
                } else {
                    putLockInfo(sharedPreferences, which)
                    preference.summary = getString(R.string.close)
                }
                dialog.dismiss()
            }
            setPositiveButton(getString(R.string.cancel), null)
            create()
            show()
        }
    }

    /**
     * 显示设置文本字体大小的对话框
     *
     * @param preference Preference
     * @param textSize 未修改前的字体大小
     */
    private fun showTextFontSizeDialog(preference: Preference, textSize: Float) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        ).apply {
            val newTextSize = textSizeValue(sharedPreferences)
            val updateTextFontSizeLayout =
                layoutInflater.inflate(R.layout.update_text_font_size_layout, null)
            val tvTitle =
                updateTextFontSizeLayout.findViewById<MaterialTextView>(R.id.tv_title)
            val tvContent =
                updateTextFontSizeLayout.findViewById<MaterialTextView>(R.id.tv_content)
            tvTitle.textSize = newTextSize + 4
            tvContent.textSize = newTextSize
            val slider = updateTextFontSizeLayout.findViewById<Slider>(R.id.slider)
            slider.value = newTextSize
            var newValue = textSize
            slider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    newValue = value
                    tvTitle.textSize = newValue + 4
                    tvContent.textSize = newValue
                }
            }
            setTitle(getString(R.string.text_font_size))
            setView(updateTextFontSizeLayout)
            setPositiveButton(getString(R.string.sure)) { _, _ ->
                sharedPreferences.edit {
                    putFloat("text_font_size_preference", newValue)
                    apply()
                }
                preference.summary = newValue.toString()
                preference.icon = if (newValue >= 16) ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.outline_text_increase_24
                ) else ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.outline_text_decrease_24
                )
            }
            setNeutralButton(getString(R.string.cancel), null)
            create()
            show()
        }
    }

    private fun showDailyLockKeyDialog() {
        MaterialAlertDialogUtil.showDialog(
            requireContext(),
            title = requireContext().getString(R.string.key),
            layoutRes = R.layout.dialog_input_password_layout,
            onViewCreated = { view, _ ->
                val inputPasswordLayout =
                    view.findViewById<TextInputLayout>(R.id.input_password_layout)
                inputPasswordLayout.hint = requireContext().getString(R.string.key)
            }

        )
    }
}