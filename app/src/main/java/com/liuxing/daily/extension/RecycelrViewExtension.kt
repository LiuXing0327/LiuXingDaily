/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import androidx.recyclerview.widget.RecyclerView
import com.liuxing.daily.entity.DailyEntity

fun RecyclerView.setLightStausBarsFromBitmap(
    dailyList: List<DailyEntity>, onLightStausBars: () -> Unit
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (!recyclerView.canScrollVertically(-1) && dailyList.isNotEmpty()) {
                onLightStausBars.invoke()
            }
        }
    })
}