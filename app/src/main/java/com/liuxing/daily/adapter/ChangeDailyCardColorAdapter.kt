/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.liuxing.daily.R
import com.liuxing.daily.util.ConstUtil

/**
 * 切换颜色的适配器
 */
class ChangeDailyCardColorAdapter(
    private val colorList: List<Int>,
    private var selectedPosition: Int,
    private val onColorSelected: (Int,Int) -> Unit
) :
    RecyclerView.Adapter<ChangeDailyCardColorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_change_daily_card_color, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val color = colorList[position]

        holder.colorCard.setCardBackgroundColor(
            ContextCompat.getColor(
                holder.itemView.context,
                color
            )
        )

        if(position == selectedPosition){
            holder.ivSelected.visibility = View.VISIBLE
        }else{
            holder.ivSelected.visibility = View.GONE
        }
        holder.colorCard.setOnClickListener {

            selectedPosition = position
            onColorSelected(color,position)
            ConstUtil.backgroundColorLabelList[position].let {
                holder.colorCard.contentDescription = holder.colorCard.context.getString(it)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorCard: MaterialCardView = itemView.findViewById(R.id.color_card)
        val ivSelected: ImageView = itemView.findViewById(R.id.ic_selected)
    }
}