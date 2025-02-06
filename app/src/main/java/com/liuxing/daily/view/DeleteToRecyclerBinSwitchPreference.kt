package com.liuxing.daily.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.google.android.material.materialswitch.MaterialSwitch
import com.liuxing.daily.R

/**
 * Author：流星
 * DateTime：2024/10/29 18:08
 * Description：
 */
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
            holder.findViewById(R.id.switch_delete_to_recycler_bin_daily) as MaterialSwitch
        switchDeleteToRecyclerBin.isChecked =
            sharedPreferences.getBoolean("switch_delete_to_recycler_bin_daily", true)
        switchDeleteToRecyclerBin.setOnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit {
                putBoolean("switch_delete_to_recycler_bin_daily", isChecked)
                    .apply()
            }
        }
    }
}