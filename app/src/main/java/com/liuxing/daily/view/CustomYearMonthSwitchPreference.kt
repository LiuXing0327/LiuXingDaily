package com.liuxing.daily.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.google.android.material.materialswitch.MaterialSwitch
import com.liuxing.daily.R

/**
 * Author：流星
 * DateTime：2024/10/7 上午11:31
 * Description：
 */
class CustomYearMonthSwitchPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_switch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val switchYearMonthDisplay =
            holder.findViewById(R.id.switch_year_month_display) as MaterialSwitch

        switchYearMonthDisplay.isChecked = sharedPreferences.getBoolean("switch_preference_header_display", true)
        switchYearMonthDisplay.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit()
                .putBoolean("switch_preference_header_display", isChecked)
                .apply()
        }
    }
}