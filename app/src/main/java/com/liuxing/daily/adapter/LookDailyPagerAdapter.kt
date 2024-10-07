package com.liuxing.daily.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.ui.look.LookDailyPagerFragment

/**
 * Author：流星
 * DateTime：2024/10/5 下午7:38
 * Description：看日记适配器
 */
class LookDailyPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val dailyList: List<DailyEntity>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = dailyList.size

    override fun createFragment(position: Int): Fragment {
        val dailyEntity = dailyList[position]
        return LookDailyPagerFragment.newInstance(
            dailyEntity.title,
            dailyEntity.dateTime,
            dailyEntity.content,
            dailyEntity.backgroundColorIndex
        )
    }
}