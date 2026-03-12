package com.liuxing.daily.ui.wallpaper

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liuxing.daily.R
import com.liuxing.daily.databinding.ActivityWallpaperBinding
import com.liuxing.daily.ui.qrx.QRXActivity
import com.liuxing.daily.util.ThemeUtil

class WallpaperActivity : QRXActivity() {

    private val binding by lazy {
        ActivityWallpaperBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.applyTheme(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.wallpaper_fragment, WallpaperFragment())
                .commit()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        qrx()
    }

    fun qrx() {
        val qrx = (this as QRXActivity)
        qrx.init(binding.wallpaper, binding.appBarLayout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}