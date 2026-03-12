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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.adapter.ListMultisectionAdapter
import com.liuxing.daily.data.BaseListItemData
import com.liuxing.daily.databinding.FragmentSettingsBinding
import com.liuxing.daily.ui.about.AboutActivity
import com.liuxing.daily.ui.about.OpenSourceActivity
import com.liuxing.daily.ui.about.SpecialThanksActivity
import com.liuxing.daily.ui.appearance.AppearanceSettingsActivity
import com.liuxing.daily.ui.privacy.PrivacyActivity
import com.liuxing.daily.ui.updatelog.UpdateLogActivity
import com.liuxing.daily.ui.wallpaper.WallpaperActivity
import com.liuxing.daily.ui.webdav.WebDavBackupActivity
import com.liuxing.daily.util.CheckAppUpdateUtil
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.MaterialAlertDialogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val ABOUT_ITEM_SELECT_COUNT = 4
private const val UPDATE_ITEM_SELECT_COUNT = 2
private const val DAILY_ITEM_SELECT_COUNT = 8
private const val APPEARANCE_ITEM_SELECT_COUNT = 2

// private const val DATA_ITEM_SELECT_COUNT = 1
// private const val SAFE_ITEM_SELECT_COUNT = 1

private enum class AboutItemKey {
    ABOUT, SPECIAL_THANKS, OPEN_SOURCE_LIBRARIES, USER_AGREEMENT_AND_PRIVACY_POLICY
}

private enum class UpdateItemKey {
    CHECK_UPDATE, UPDATE_LOG
}

private enum class DailyItemKey(val key: String) {
    DISPLAYED_BY_YEAR_MONTH("switch_preference_header_display"), AUTO_SAVE("switch_preference_auto_save"), DELETE_TO_RECYCLER(
        "switch_delete_to_recycler_bin_daily"
    ),
    TURN_OFF_IMAGE_DISPLAY(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY), AUTO_DELETE_RECYCLER_BIN("auto_delete_recycler_bin_daily"), TEXT_SPACING(
        "text_line_spacing_preference"
    ),
    TEXT_SIZE("text_font_size_preference"), DAILY_SETTINGS("daily_settings_preference")
}

private enum class AppearanceItemKey {
    APPEARANCE, WALLPAPER
}

private enum class DataItemKey {
    WEBDAV
}

private enum class SafeIemKey {
    APP_LOCK
}

class SettingsFragment : Fragment() {

    /**
     * 当前选中的应用锁选项索引，默认索引为 0
     */
    private val appLockOptionsIndex: Int by lazy {
        sharedPreferences.getInt("app_lock_options_index", 0)
    }
    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }
    private val binding by lazy {
        FragmentSettingsBinding.inflate(layoutInflater)
    }

    /**
     * 设置数据列表
     */
    private val settingsDataList = mutableListOf<BaseListItemData>()

    private lateinit var listMultisectionAdapter: ListMultisectionAdapter

    /**
     * 应用锁的选项
     */
    private val lockOptions by lazy {
        arrayOf(
            getString(R.string.close), getString(R.string.password), getString(R.string.pin)
        )
    }

    /**
     * 自动清理回收站的索引，默认 7。
     */
    private var autoDeleteIndex = 7

    private val daysMap by lazy {
        mapOf(
            0 to requireContext().getString(R.string.close),
            3 to requireContext().getString(R.string.days_3),
            7 to requireContext().getString(R.string.days_7),
            14 to requireContext().getString(R.string.days_14),
            30 to requireContext().getString(R.string.days_30)
        )
    }

    /**
     *  when (autoDeleteIndex) {
     *             3 -> {
     *                 context.getString(R.string.days_3)
     *             }
     *
     *             7 -> {
     *                 context.getString(R.string.days_7)
     *             }
     *
     *             14 -> {
     *                 context.getString(R.string.days_14)
     *             }
     *
     *             30 -> {
     *                 context.getString(R.string.days_30)
     *             }
     *
     *             else -> {
     *                 context.getString(R.string.close)
     *             }
     *         }
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    private fun initData() {
        createItems()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val listMultisectionAdapter = initAdapter()
        binding.listFragment.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())

            adapter = listMultisectionAdapter
        }
        binding.listFragment.recyclerView.addItemDecoration(
            ListMultisectionAdapter.MarginItemDecoration(
                requireContext()
            )
        )
    }

    /**
     * 初始化 [listMultisectionAdapter]
     */
    private fun initAdapter(): ListMultisectionAdapter {
        listMultisectionAdapter = ListMultisectionAdapter(
            { item ->
                when (item.key) {

                    AboutItemKey.ABOUT -> startActivity(AboutActivity::class.java)

                    AboutItemKey.SPECIAL_THANKS -> startActivity(SpecialThanksActivity::class.java)

                    AboutItemKey.OPEN_SOURCE_LIBRARIES -> startActivity(OpenSourceActivity::class.java)

                    AboutItemKey.USER_AGREEMENT_AND_PRIVACY_POLICY -> startActivity(PrivacyActivity::class.java)

                    DailyItemKey.AUTO_DELETE_RECYCLER_BIN -> showAutoDeleteDialog()

                    DailyItemKey.TEXT_SPACING -> showTextLineSpacingDialog()

                    DailyItemKey.TEXT_SIZE -> showTextFontSizeDialog()

                    DailyItemKey.DAILY_SETTINGS -> startActivity(DailySettingsActivity::class.java)

                    UpdateItemKey.CHECK_UPDATE -> CheckAppUpdateUtil.checkUpdate(requireContext())

                    UpdateItemKey.UPDATE_LOG -> startActivity(UpdateLogActivity::class.java)

                    AppearanceItemKey.APPEARANCE -> startActivity(AppearanceSettingsActivity::class.java)

                    AppearanceItemKey.WALLPAPER -> startActivity(WallpaperActivity::class.java)

                    DataItemKey.WEBDAV -> startActivity(WebDavBackupActivity::class.java)

                    SafeIemKey.APP_LOCK -> showLockDialog(lockOptions)
                }
            },

            onCheckedChange = { item, newValue ->
                sharedPreferences.edit {
                    putBoolean(item.key, newValue)
                    apply()
                }
            })

        listMultisectionAdapter.setData(settingsDataList)

        return listMultisectionAdapter
    }

    /**
     * 启动 Activity
     *
     * @param clazz 要启动的 Activity 类
     */
    private fun startActivity(clazz: Class<*>) {
        IntentUtil.startActivity(
            requireContext(), clazz
        )
    }

    /**
     * 获取选中值
     *
     * @param key 键
     * @param defaultValue 默认值
     */
    private fun getCheckedValue(key: String, defaultValue: Boolean) =
        sharedPreferences.getBoolean(key, defaultValue)

    /**
     * 创建所有 Item
     */
    private fun createItems() {
        createAboutItems()
        createUpdateItems()
        createDailyItems()
        createAppearanceItems()
        createDataItems()
        createSafeItems()
    }

    /**
     * 创建关于 Item
     */
    private fun createAboutItems() {
        settingsDataList.add(BaseListItemData.Header(getString(R.string.about)))

        settingsDataList.add(
            BaseListItemData.Item(
                AboutItemKey.ABOUT,
                R.drawable.outline_info_preference_24,
                getString(R.string.about),
                false,
                0,
                ABOUT_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                AboutItemKey.SPECIAL_THANKS,
                R.drawable.outline_favorite_border_preference_24,
                getString(R.string.special_thanks),
                true,
                1,
                ABOUT_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                AboutItemKey.OPEN_SOURCE_LIBRARIES,
                R.drawable.outline_code_24,
                getString(R.string.open_source_libraries),
                true,
                2,
                ABOUT_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                AboutItemKey.USER_AGREEMENT_AND_PRIVACY_POLICY,
                R.drawable.outline_privacy_tip_24,
                getString(R.string.user_agreement_and_privacy_Policy),
                true,
                3,
                ABOUT_ITEM_SELECT_COUNT
            )
        )
    }

    /**
     * 创建更新 Item
     */
    private fun createUpdateItems() {
        settingsDataList.add(BaseListItemData.Header(getString(R.string.update)))

        settingsDataList.add(
            BaseListItemData.Item(
                UpdateItemKey.CHECK_UPDATE,
                R.drawable.outline_update_preference_24,
                getString(R.string.check_update),
                true,
                0,
                UPDATE_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                UpdateItemKey.UPDATE_LOG,
                R.drawable.outline_log_preference_24,
                getString(R.string.update_log),
                true,
                1,
                UPDATE_ITEM_SELECT_COUNT
            )
        )
    }

    /**
     * 创建日记 Item
     */
    private fun createDailyItems() {
        autoDeleteIndex = sharedPreferences.getInt("auto_delete_recycler_bin_daily", 7)

        settingsDataList.add(BaseListItemData.Header(getString(R.string.daily)))

        settingsDataList.add(
            BaseListItemData.SwitchItem(
                DailyItemKey.DISPLAYED_BY_YEAR_MONTH.key,
                R.drawable.outline_swap_horiz_preference_24,
                getString(R.string.classifications_are_displayed_by_year_month),
                checked = getCheckedValue(DailyItemKey.DISPLAYED_BY_YEAR_MONTH.key, true),
                indexInSelection = 0,
                selectionCount = DAILY_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.SwitchItem(
                DailyItemKey.AUTO_SAVE.key,
                R.drawable.outline_save_preference_24,
                getString(R.string.auto_save),
                checked = getCheckedValue(DailyItemKey.AUTO_SAVE.key, true),
                indexInSelection = 1,
                selectionCount = DAILY_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.SwitchItem(
                DailyItemKey.DELETE_TO_RECYCLER.key,
                R.drawable.outline_recycling_preference_24,
                getString(R.string.move_to_recycle_bin_when_deleting_a_diary),
                checked = getCheckedValue(DailyItemKey.DELETE_TO_RECYCLER.key, true),
                indexInSelection = 2,
                selectionCount = DAILY_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.SwitchItem(
                DailyItemKey.TURN_OFF_IMAGE_DISPLAY.key,
                R.drawable.outline_hide_image_24,
                getString(R.string.turn_off_the_journal_list_image_display),
                checked = getCheckedValue(DailyItemKey.TURN_OFF_IMAGE_DISPLAY.key, false),
                indexInSelection = 3,
                selectionCount = DAILY_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                DailyItemKey.AUTO_DELETE_RECYCLER_BIN,
                R.drawable.outline_auto_delete_preference_24,
                getString(R.string.regularly_automatically_delete_the_diaries_in_the_recycle_bin),
                true,
                4,
                DAILY_ITEM_SELECT_COUNT,
                daysMap[autoDeleteIndex].toString()
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                DailyItemKey.TEXT_SPACING,
                R.drawable.outline_vertical_distribute_24,
                getString(R.string.text_line_spacing),
                true,
                5,
                DAILY_ITEM_SELECT_COUNT,
                textLineSpacingValue(sharedPreferences).toString()
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                DailyItemKey.TEXT_SIZE,
                R.drawable.outline_text_increase_24,
                getString(R.string.text_font_size),
                true,
                6,
                DAILY_ITEM_SELECT_COUNT,
                textSizeValue(sharedPreferences).toString()
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                DailyItemKey.DAILY_SETTINGS,
                R.drawable.baseline_notes_preference_24,
                getString(R.string.daily_settings),
                true,
                7,
                DAILY_ITEM_SELECT_COUNT,
                getString(R.string.management_diary_related_functions)
            )
        )

    }

    /**
     * 创建外观 Item
     */
    private fun createAppearanceItems() {
        settingsDataList.add(BaseListItemData.Header(getString(R.string.appearance)))

        settingsDataList.add(
            BaseListItemData.Item(
                AppearanceItemKey.APPEARANCE,
                R.drawable.outline_palette_preference_24,
                getString(R.string.appearance),
                true,
                0,
                APPEARANCE_ITEM_SELECT_COUNT
            )
        )

        settingsDataList.add(
            BaseListItemData.Item(
                AppearanceItemKey.WALLPAPER,
                R.drawable.outline_image_24,
                getString(R.string.wallpaper),
                checked = true,
                indexInSelection = 1,
                selectionCount = APPEARANCE_ITEM_SELECT_COUNT
            )
        )
    }

    /**
     * 创建数据 Item
     */
    private fun createDataItems() {
        settingsDataList.add(BaseListItemData.Header(getString(R.string.data)))
        settingsDataList.add(
            BaseListItemData.Item(
                DataItemKey.WEBDAV,
                R.drawable.outline_cloud_upload_preference_24,
                getString(R.string.webdav_backup),
                false
            )
        )
    }

    /**
     * 创建安全 Item
     */
    private fun createSafeItems() {
        settingsDataList.add(BaseListItemData.Header(getString(R.string.safe)))
        settingsDataList.add(
            BaseListItemData.Item(
                SafeIemKey.APP_LOCK,
                R.drawable.outline_lock_24,
                getString(R.string.app_lock),
                false,
                subText = lockOptions[appLockOptionsIndex]
            )
        )
    }


    /*
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
            *//*            bindPreferenceAction("background_image_preference"){
                            showWallpaperDialog()
                        }*//*


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
*/

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
     */
    private fun showTextLineSpacingDialog() {
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

                updateItemSubText(
                    DailyItemKey.TEXT_SPACING, textLineSpacingValue(sharedPreferences).toString()
                )
            }
            create()
            show()
        }
    }

    /**
     * 更新普通 Item 的副文本
     *
     * @param key 要更新的 Item 键
     * @param newSubText 新的副文本
     */
    private fun updateItemSubText(key: Any, newSubText: String) {
        val index = settingsDataList.indexOfFirst { it is BaseListItemData.Item && it.key == key }
        if (index != -1) {
            val item = settingsDataList[index] as BaseListItemData.Item
            item.subText = newSubText
            listMultisectionAdapter.notifyItemChanged(index)
        }
    }

    /**
     * 显示是否设置应用锁的对话框
     *
     * @param options 对话框的选项
     */
    private fun showLockDialog(options: Array<String>) {
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

                            updateItemSubText(SafeIemKey.APP_LOCK, options[which])

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
                    updateItemSubText(SafeIemKey.APP_LOCK, getString(R.string.close))
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
     * @param textSize 未修改前的字体大小
     */
    private fun showTextFontSizeDialog(textSize: Float = textSizeValue(sharedPreferences)) {
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

                updateItemSubText(
                    DailyItemKey.TEXT_SIZE, textSizeValue(sharedPreferences).toString()
                )
            }
            setNeutralButton(getString(R.string.cancel), null)
            create()
            show()
        }
    }

    /*    private fun showDailyLockKeyDialog() {
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
        }*/

    private fun showAutoDeleteDialog() {
        val dayEntries = daysMap.entries.toList()

        MaterialAlertDialogBuilder(requireContext()).apply {
            setItems(
                dayEntries.map { it.value }.toTypedArray()
            ) { _, which ->
                putAutoDeleteIndex(dayEntries[which].key)
                updateItemSubText(DailyItemKey.AUTO_DELETE_RECYCLER_BIN, dayEntries[which].value)
            }

            setPositiveButton(getString(R.string.cancel), null)
            create()
            show()
        }
    }

    /**
     * 存入自动删除的索引
     *
     * @param index 索引
     */
    private fun putAutoDeleteIndex(index: Int) {
        sharedPreferences.edit {
            putInt("auto_delete_recycler_bin_daily", index)
            apply()
        }
    }
}