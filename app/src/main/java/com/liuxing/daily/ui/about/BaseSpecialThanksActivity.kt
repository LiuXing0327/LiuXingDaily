/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.ui.about

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.liuxing.daily.adapter.SpecialThanksAdapter
import com.liuxing.daily.data.SpecialThanksData
import com.liuxing.daily.ui.qrx.QRXActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class BaseSpecialThanksActivity : QRXActivity() {

    private val specialThanksAdapter: SpecialThanksAdapter by lazy {
        SpecialThanksAdapter()
    }
    private lateinit var wallpaper: ImageView
    private lateinit var appBarLayout: AppBarLayout
    private val qrx by lazy {
        this as QRXActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(300)
                    qrx.checkStatusBarColor(true)
                }
            }
        }
    }

    fun initQRX(wallpaper: ImageView, appBarLayout: AppBarLayout) {
        this.wallpaper = wallpaper
        this.appBarLayout = appBarLayout

        qrx.init(wallpaper, appBarLayout)
    }

    fun initRecyclerView(
        recyclerView: RecyclerView,
        specialThanksDataList: List<SpecialThanksData>
    ) {
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = specialThanksAdapter
        specialThanksAdapter.setSpecialThanksList(specialThanksDataList)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}