package com.liuxing.daily.ui.settings

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.databinding.SettingsActivityBinding
import com.liuxing.daily.ui.about.AboutActivity
import com.liuxing.daily.ui.about.OpenSourceActivity
import com.liuxing.daily.ui.about.SpecialThanksActivity
import com.liuxing.daily.ui.appearance.AppearanceSettingsActivity
import com.liuxing.daily.ui.privacy.PrivacyActivity
import com.liuxing.daily.ui.updatelog.UpdateLogActivity
import com.liuxing.daily.ui.webdav.WebDavBackupActivity
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class SettingsActivity : AppCompatActivity() {

    private lateinit var activityBinding: SettingsActivityBinding
    private var currentThemeColorId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        activityBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left,0,navigationBars.right,navigationBars.bottom)
            insets
        }
        setSupportActionBar(activityBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        currentThemeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
    }

    override fun onRestart() {
        super.onRestart()
        val themeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        if (themeColorId == currentThemeColorId) return
        ThemeUtil.applyTheme(this)
        recreate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private var optionsIndex: Int = 0
        private lateinit var sharedPreferences: SharedPreferences

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(requireContext())

            val aboutPreference = findPreference<Preference>("about_preference")
            aboutPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), AboutActivity::class.java)
                true
            }

            val specialThanksPreference = findPreference<Preference>("special_thanks_preference")
            specialThanksPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), SpecialThanksActivity::class.java)
                true
            }

            val openSourcePreference = findPreference<Preference>("open_source_preference")
            openSourcePreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), OpenSourceActivity::class.java)
                true
            }

            val checkUpdatePreference = findPreference<Preference>("check_update_preference")
            checkUpdatePreference?.setOnPreferenceClickListener {
                CheckAppUpdateUtil.checkUpdate(requireContext())
                true
            }

            val updateLogPreference = findPreference<Preference>("update_log_preference")
            updateLogPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), UpdateLogActivity::class.java)
                true
            }

            val themeColorPreference = findPreference<Preference>("appearance_preference")
            themeColorPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), AppearanceSettingsActivity::class.java)
                true
            }

            val webDavBackupPreference = findPreference<Preference>("webdav_backup_preference")
            webDavBackupPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), WebDavBackupActivity::class.java)
                true
            }

            val textLineSpacingPreference =
                findPreference<Preference>("text_line_spacing_preference")
            val textLineSpacingValue =
                textLineSpacingValue(sharedPreferences)
            textLineSpacingPreference?.summary = "$textLineSpacingValue"
            textLineSpacingPreference?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext(),R.style.ThemeOverlay_App_MaterialAlertDialog).apply {
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
                        textLineSpacingPreference.summary =
                            "${textLineSpacingValue(sharedPreferences)}"
                    }
                    create()
                    show()
                }
                true
            }

            val appLockPreference = findPreference<Preference>("app_lock_preference")
            optionsIndex = sharedPreferences.getInt("app_lock_options_index", 0)
            val options = arrayOf(getString(R.string.close), getString(R.string.enabled))
            appLockPreference?.summary = options[optionsIndex]
            appLockPreference?.setOnPreferenceClickListener {
                optionsIndex = sharedPreferences.getInt("app_lock_options_index", 0)
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.app_lock)
                    setSingleChoiceItems(options, optionsIndex) { dialog, which ->
                        if (which == 1) {
                            MaterialAlertDialogBuilder(requireContext()).apply {
                                val view = layoutInflater.inflate(
                                    R.layout.dialog_input_password_layout,
                                    null
                                )
                                val inputPassword =
                                    view.findViewById<TextInputEditText>(R.id.input_password)
                                setTitle(getString(R.string.password))
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
                                        appLockPreference?.summary = getString(R.string.enabled)
                                    }
                                }
                                setNeutralButton(getString(R.string.cancel), null)
                                create()
                                show()
                            }
                        } else {
                            putLockInfo(sharedPreferences, which)
                            appLockPreference?.summary = getString(R.string.close)
                        }
                        dialog.dismiss()
                    }
                    setPositiveButton(getString(R.string.cancel), null)
                    create()
                    show()
                }
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
                MaterialAlertDialogBuilder(requireContext(),R.style.ThemeOverlay_App_MaterialAlertDialog).apply {
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
                        textFontSizePreference.summary = newValue.toString()
                        textFontSizePreference.icon = if (newValue >= 16) ContextCompat.getDrawable(
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
                true
            }

            val backgroundImagePreference =
                findPreference<Preference>("background_image_preference")
            backgroundImagePreference?.setOnPreferenceClickListener {
                showWallpaperDialog()
                true
            }

            val userAgreementAndPrivacyPolicyPreference =
                findPreference<Preference>("user_agreement_and_privacy_policy_preference")
            userAgreementAndPrivacyPolicyPreference?.setOnPreferenceClickListener {
                IntentUtil.startActivity(requireContext(), PrivacyActivity::class.java)
                true
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
                            getString(R.string.for_a_better_experience_do_you_want_to_turn_off_the_diary_list_image_display),getString(R.string.sure),
                            {
                                sharedPreferences.edit {
                                    putBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, true)
                                    apply()
                                }
                                requireActivity().finish()
                                val intent = Intent(requireContext(),SettingsActivity::class.java)
                                val options = ActivityOptions.makeCustomAnimation(requireActivity(), R.anim.fade_in, R.anim.fade_out)
                                startActivity(intent, options.toBundle())
                            },
                            getString(R.string.cancel))
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
                        if (result.resultCode != Activity.RESULT_OK) return
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
                                withContext(Dispatchers.Main) {
                                    MaterialAlertDialogUtil.showDialog(
                                        requireContext(),
                                        getString(R.string.add_failed),
                                        getString(R.string.sure)
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
    }
}