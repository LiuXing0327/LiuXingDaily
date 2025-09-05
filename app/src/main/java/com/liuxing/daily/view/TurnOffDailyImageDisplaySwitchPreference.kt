/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.google.android.material.materialswitch.MaterialSwitch
import com.liuxing.daily.R
import androidx.core.content.edit
import com.liuxing.daily.util.ConstUtil

/**
 * 关闭日记列表图像显示，使用自定义壁纸时获取更好的体验
 */
class TurnOffDailyImageDisplaySwitchPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_daily_image_display
    }

    /**
     * 绑定视图
     *
     * @param holder 视图
     */
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val switchTurnoffDailyImageDisplay =
            holder.findViewById(R.id.switch_daily_image_display) as MaterialSwitch

        val switchImageDisplayPreference =
            holder.findViewById(R.id.turn_off_daily_image_display_preference) as LinearLayout
        switchImageDisplayPreference.setOnClickListener {
            switchTurnoffDailyImageDisplay.isChecked = !switchTurnoffDailyImageDisplay.isChecked
        }

        switchTurnoffDailyImageDisplay.isChecked =
            sharedPreferences.getBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, false)
        switchTurnoffDailyImageDisplay.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit {
                putBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, isChecked)
                apply()
            }
        }
    }
}