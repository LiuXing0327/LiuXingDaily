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
import com.liuxing.daily.R
import com.liuxing.daily.material.widget.DailyMaterialSwitch

class DeleteToRecyclerBinSwitchPreference(
    context: Context,
    attrs: AttributeSet,
) : Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_delete_to_recycler_bin_switch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val switchDeleteToRecyclerBin =
            holder.findViewById(R.id.switch_delete_to_recycler_bin_daily) as DailyMaterialSwitch
        val switchDeleteToRecyclerBinDailyPreference =
            holder.findViewById(R.id.switch_delete_to_recycler_bin_daily_preference) as LinearLayout

        switchDeleteToRecyclerBinDailyPreference.setOnClickListener {
            switchDeleteToRecyclerBin.isChecked = !switchDeleteToRecyclerBin.isChecked
        }

        switchDeleteToRecyclerBin.isChecked =
            sharedPreferences.getBoolean("switch_delete_to_recycler_bin_daily", true)
        switchDeleteToRecyclerBin.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit {
                putBoolean("switch_delete_to_recycler_bin_daily", isChecked)
                apply()
            }
        }
    }
}