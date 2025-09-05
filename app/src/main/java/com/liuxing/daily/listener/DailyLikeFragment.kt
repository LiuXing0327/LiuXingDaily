/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.listener

import com.liuxing.daily.entity.DailyEntity

interface DailyLikeFragment {

    fun getDailyList(): List<DailyEntity>

    fun getSelectedItems(): List<String>

    fun clearSection()

    fun selectAllItems()

    fun getSelectMode(): Boolean

    fun isPinnedDisplay(): Boolean
}