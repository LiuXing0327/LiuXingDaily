package com.liuxing.daily.ui.lock

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityUnlockBinding
import com.liuxing.daily.ui.lock.UnlockActivity.Companion.sharedPreferences
import com.liuxing.daily.ui.settings.SettingsConst
import com.liuxing.daily.util.ThemeUtil

class UnlockActivity : AppCompatActivity() {

    private lateinit var unlockBinding: ActivityUnlockBinding

    val appLockIndex by lazy {
        sharedPreferences?.getInt(SettingsConst.APP_LOCK_OPTIONS_INDEX_KEY, 0)
    }

    companion object {
        private var sharedPreferences: SharedPreferences? = null
        val appPassword by lazy {
            sharedPreferences?.getString("app_password", "").toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        unlockBinding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(unlockBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                    insets
        }
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        initSharePreference()
    }

    /**
     * 初始化 [sharedPreferences]
     */
    private fun initSharePreference() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onStart() {
        super.onStart()
        changeFragmentWithIndex()
    }

    private fun changeFragmentWithIndex() {
        val navController = findNavController(R.id.fragmentContainerView)
        if (appLockIndex == 1) {
            navController.navigate(R.id.passwordFragment)
        } else {
            navController.navigate(R.id.pinFragment)
        }
    }

}