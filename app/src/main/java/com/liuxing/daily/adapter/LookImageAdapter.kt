/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liuxing.daily.ui.image.LookDailyImageFragment

class LookImageAdapter(
    fragmentActivity: FragmentActivity,
    private val imageList: List<String?>
) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        return LookDailyImageFragment.newInstance(imageList[position].toString(), "")
    }
}