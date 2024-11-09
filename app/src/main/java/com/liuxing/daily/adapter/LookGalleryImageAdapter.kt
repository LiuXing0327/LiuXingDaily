package com.liuxing.daily.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.liuxing.daily.ui.gallery.LookGalleryImageFragment
import com.liuxing.daily.ui.image.LookDailyImageFragment
import java.io.File

/**
 * Author：流星
 * DateTime：2024/10/27 14:09
 * Description：看图库图片的适配器
 */
class LookGalleryImageAdapter(
    fragmentActivity: FragmentActivity,
    private val imageList: List<File?>
) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = imageList.size

    override fun createFragment(position: Int): Fragment {
        return LookGalleryImageFragment.newInstance(imageList[position].toString(), "")
    }
}