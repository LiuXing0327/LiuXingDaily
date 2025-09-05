/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.google.android.material.materialswitch.MaterialSwitch
import com.liuxing.daily.R

class YearMonthSwitchPreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_year_month_switch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val switchYearMonthDisplay =
            holder.findViewById(R.id.switch_year_month_display) as MaterialSwitch
        val switchYearMonthPreference =
            holder.findViewById(R.id.switch_year_month_preference) as LinearLayout

        switchYearMonthPreference.setOnClickListener {
            switchYearMonthDisplay.isChecked = !switchYearMonthDisplay.isChecked
        }

        switchYearMonthDisplay.isChecked = sharedPreferences.getBoolean("switch_preference_header_display", true)
        switchYearMonthDisplay.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit {
                putBoolean("switch_preference_header_display", isChecked)
                apply()
            }
        }
    }
}