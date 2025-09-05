/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.ui.look.LookDailyPagerFragment

/**
 * 看日记适配器
 */
class LookDailyPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val dailyList: List<DailyEntity>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = dailyList.size

    override fun createFragment(position: Int): Fragment {
        val dailyEntity = dailyList[position]
        return LookDailyPagerFragment.newInstance(
            dailyEntity.id,
            dailyEntity.title,
            dailyEntity.dateTime,
            dailyEntity.content,
            dailyEntity.backgroundColorIndex,
            dailyEntity.singlePassword,
            dailyEntity.moodIndex,
            dailyEntity.weatherIndex,
            dailyEntity.dailyUUID,
            dailyEntity.dailyLabel,
            dailyEntity.isPinned
        )
    }
}