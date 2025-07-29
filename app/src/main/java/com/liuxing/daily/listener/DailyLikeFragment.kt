package com.liuxing.daily.listener

import com.liuxing.daily.entity.DailyEntity

/**
 * Author：流星
 * DateTime：2025/7/29 12:06
 * Description：
 */
interface DailyLikeFragment {

    fun getDailyList(): List<DailyEntity>

    fun getSelectedItems(): List<String>

    fun clearSection()

    fun selectAllItems()

    fun getSelectMode(): Boolean

    fun isPinnedDisplay(): Boolean
}