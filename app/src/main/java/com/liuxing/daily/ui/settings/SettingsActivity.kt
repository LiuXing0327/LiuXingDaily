package com.liuxing.daily.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.liuxing.daily.R
import com.liuxing.daily.databinding.SettingsActivityBinding
import com.liuxing.daily.ui.appearance.AppearanceConst
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SettingsActivity : QRXActivity() {

    private lateinit var activityBinding: SettingsActivityBinding
    private var currentThemeColorId: Int = 0

    /**
     * 当前动态取色开关值，默认为 false.
     *
     * 在 [onCreate] 获取存储的值。
     *
     * 当执行 [onRestart] 时 配合 [currentThemeColorId] 来决定是否重新应用主题，并使用 [recreate] 重建 Activity。
     */
    private var currentDynamicColorChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        activityBinding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)/* if (savedInstanceState == null) {
             supportFragmentManager
                 .beginTransaction()
                 .replace(R.id.settings, SettingsFragment())
                 .commit()
         }*/

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(navigationBars.left, 0, navigationBars.right, navigationBars.bottom)
            insets
        }
        setSupportActionBar(activityBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val navController =
            (supportFragmentManager.findFragmentById(R.id.settings) as NavHostFragment).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            supportActionBar?.title = destination.label
        }
        currentThemeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        currentDynamicColorChecked =
            SharedPreferencesUtil.getBoolean(this, AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY, false)
        // qrx()
    }

    fun qrx() {
        val qrx = (this as QRXActivity)
        qrx.init(activityBinding.wallpaper, activityBinding.appBarLayout)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(300)
                    qrx.checkStatusBarColor(true)
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val themeColorId = SharedPreferencesUtil.getInt(this, "theme_color_id", 0)
        val dynamicColorChecked =
            SharedPreferencesUtil.getBoolean(this, AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY, false)
        if (themeColorId == currentThemeColorId && currentDynamicColorChecked == dynamicColorChecked) return
        ThemeUtil.applyTheme(this)
        recreate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}