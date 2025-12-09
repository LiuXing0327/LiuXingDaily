/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.listitem.ListItemViewHolder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.data.DailySettingsData
import com.liuxing.daily.extension.toggleSwitchIcon
import com.liuxing.daily.material.widget.DailyMaterialSwitch
import com.liuxing.daily.util.LogUtil

class DailySettingsAdapter(
    private val onCheckedChange: (DailySettingsData, Boolean) -> Unit
) : RecyclerView.Adapter<DailySettingsAdapter.ViewHolder>() {

    private var dailySettingsDataList: List<DailySettingsData> = emptyList()

    fun setDailySettingsDataList(dailySettingsDataList: List<DailySettingsData>) {
        this.dailySettingsDataList = dailySettingsDataList
    }

    class ViewHolder(itemView: View) : ListItemViewHolder(itemView) {
        private val textView: MaterialTextView = itemView.findViewById(R.id.list_item_text)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.list_item_card_view)
        private val switch: DailyMaterialSwitch = itemView.findViewById(R.id.list_item_switch)
        private val startIcon: ImageView = itemView.findViewById(R.id.list_item_start_icon)

        fun bind(
            data: DailySettingsData, onCheckedChange: (DailySettingsData, Boolean) -> Unit
        ) {
            textView.text = data.text
            startIcon.setImageResource(data.iconResource)

            cardView.isChecked = data.checked
            switch.isChecked = data.checked
            startIcon.isSelected = data.checked

            val clickListener = {
                val newChecked = !cardView.isChecked
                data.checked = newChecked
                cardView.toggle()
                switch.isChecked = newChecked
                startIcon.isSelected = newChecked

                LogUtil.d(message = "onCheckedChange invoked, key=${data.key}, value=$newChecked")
                onCheckedChange(data, newChecked)
            }

            cardView.setOnClickListener {
                clickListener()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_daily_settings_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dailySettingsData = dailySettingsDataList[position]
        holder.bind(dailySettingsData, onCheckedChange)
    }

    override fun getItemCount(): Int = dailySettingsDataList.size

    class MarginItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

        var itemMarin = 0

        init {
            itemMarin = context.resources.getDimensionPixelSize(R.dimen.dp_4)
        }

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position != state.itemCount - 1) outRect.bottom = itemMarin
        }
    }
}