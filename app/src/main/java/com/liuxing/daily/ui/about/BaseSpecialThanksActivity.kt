/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.ui.about

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.liuxing.daily.adapter.SpecialThanksAdapter
import com.liuxing.daily.data.SpecialThanksData

open class BaseSpecialThanksActivity : AppCompatActivity() {

    private val specialThanksAdapter: SpecialThanksAdapter by lazy {
        SpecialThanksAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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