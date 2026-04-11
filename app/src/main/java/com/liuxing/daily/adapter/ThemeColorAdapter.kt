/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.liuxing.daily.R
import com.liuxing.daily.data.ThemeColorData
import com.liuxing.daily.extension.setVisibility
import com.liuxing.daily.ui.appearance.AppearanceConst
import com.liuxing.daily.ui.appearance.AppearanceSettingsActivity
import com.liuxing.daily.util.SharedPreferencesUtil
import com.liuxing.daily.util.ThemeColor
import com.liuxing.daily.view.RoundTricolorView

/**
 * 主题色切换适配器
 */
class ThemeColorAdapter(
    private val context: Context,
    private val themeColorList: List<ThemeColorData>
) :
    RecyclerView.Adapter<ThemeColorAdapter.ViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ThemeColorAdapter.ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_change_theme_color_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ThemeColorAdapter.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val dynamicColorChecked = SharedPreferencesUtil.getBoolean(
            context, AppearanceConst.DYNAMIC_COLOR_SWITCH_KEY, false
        )
        holder.themeColorCard.isEnabled = !dynamicColorChecked

        val themeColorData = themeColorList[position]
        selectedPosition = SharedPreferencesUtil.getInt(context, "theme_color_id", 0)

        val selected = themeColorData.isSelected

        holder.icSelected.setVisibility(selected)

        val themeStrokeColor =
            if (selected) com.google.android.material.R.attr.colorOutline
            else com.google.android.material.R.attr.colorOutlineVariant
        holder.themeColorCard.apply {
            strokeColor = MaterialColors.getColor(
                this,
                themeStrokeColor
            )
        }

        holder.roundTricolor.setColors(
            themeColorData.topColor,
            themeColorData.leftColor,
            themeColorData.rightColor
        )

        val themeColor =
            ThemeColor.entries.firstOrNull { it.id == themeColorData.id } ?: ThemeColor.PURPLE
        themeColor.themeLabel.let { labelRes ->
            holder.themeColorCard.contentDescription =
                holder.themeColorCard.context.getString(labelRes)
        }

        holder.itemView.setOnClickListener {
            if (themeColorData.isSelected) {
                return@setOnClickListener
            }
            if (selectedPosition != position) {
                themeColorList[selectedPosition].isSelected = false
                notifyItemChanged(selectedPosition)
            }
            themeColorData.isSelected = true
            selectedPosition = position
            SharedPreferencesUtil.putInt(context, "theme_color_id", selectedPosition)
            notifyItemChanged(position)

            val animation =
                ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
            (context as Activity).finish()
            context.startActivity(Intent(context,AppearanceSettingsActivity::class.java),animation.toBundle())
        }
    }

    override fun getItemCount(): Int {
        return themeColorList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val themeColorCard: MaterialCardView = itemView.findViewById(R.id.theme_color_card)
        val roundTricolor: RoundTricolorView = itemView.findViewById(R.id.round_tricolor_view)
        val icSelected: ImageView = itemView.findViewById(R.id.ic_selected)
    }

    class MarginItemDecoration(context: Context, private val spanCount: Int) :
        RecyclerView.ItemDecoration() {

        var itemMargin = 0

        init {
            itemMargin = context.resources.getDimensionPixelSize(R.dimen.dp_8)
        }

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = column * itemMargin / spanCount
            outRect.right = itemMargin - (column + 1) * itemMargin / spanCount

            if (position >= spanCount) {
                outRect.top = itemMargin
            }
        }

    }
}