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

/**
 * 自定义自动保存开关偏好
 */
class AutoSaveDailySwitchPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_auto_save_switch
    }

    /**
     * 绑定视图
     *
     * @param holder 视图
     */
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val switchAutoSaveDaily =
            holder.findViewById(R.id.switch_auto_save_daily) as MaterialSwitch

        val switchAutoSavePreference =
            holder.findViewById(R.id.switch_auto_save_daily_preference) as LinearLayout
        switchAutoSavePreference.setOnClickListener {
            switchAutoSaveDaily.isChecked = !switchAutoSaveDaily.isChecked
        }

        switchAutoSaveDaily.isChecked =
            sharedPreferences.getBoolean("switch_preference_auto_save", true)
        switchAutoSaveDaily.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit {
                putBoolean("switch_preference_auto_save", isChecked)
                apply()
            }
        }
    }
}