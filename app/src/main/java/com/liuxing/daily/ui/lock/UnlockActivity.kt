package com.liuxing.daily.ui.lock

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.PreferenceManager
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityUnlockBinding
import com.liuxing.daily.ui.main.MainActivity
import com.liuxing.daily.util.HashUtil
import com.liuxing.daily.util.IntentUtil
import com.liuxing.daily.util.ThemeUtil
import com.liuxing.daily.util.WindowUtil

class UnlockActivity : AppCompatActivity() {

    private lateinit var unlockBinding: ActivityUnlockBinding
    private var sharedPreferences: SharedPreferences? = null
    private var appPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        unlockBinding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(unlockBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                    val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                    insets
        }
        initData()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        initStatusBarColor()
        initSharePreference()
        getAppPassword()
        changeButtonState()
        unlock()
    }


    /**
     * 初始化状态栏颜色
     */
    private fun initStatusBarColor() {
        val typedValue = TypedValue()
        theme.resolveAttribute(
            R.attr.collapsed_status_bar, typedValue, true
        )
        WindowUtil.followPatternSetColor(window, this)
        window.statusBarColor =
            ContextCompat.getColor(this, android.R.color.transparent)
    }

    /**
     * 初始化 [sharedPreferences]
     */
    private fun initSharePreference() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    /**
     * 获取应用密码
     */
    private fun getAppPassword() {
        appPassword = sharedPreferences?.getString("app_password", "").toString()
    }

    /**
     * 通过监听文本更改，设置按钮是否启用
     */
    private fun changeButtonState() {
        unlockBinding.passwordLayout.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                unlockBinding.btnUnlock.isEnabled = HashUtil.hashSHA256(s.toString()) == appPassword
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    /**
     * 解除锁定
     */
    private fun unlock() {
        unlockBinding.btnUnlock.setOnClickListener {
            IntentUtil.startActivity(this, MainActivity::class.java, mapOf("lock" to false))
            finish()
        }
    }
}