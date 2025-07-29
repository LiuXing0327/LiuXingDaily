package com.liuxing.daily.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.listener.OnItemClickListener

/**
 * Author：流星
 * DateTime：2024/11/16 22:28
 * Description：选择日记标签
 */
class SelectDailyLabelAdapter(
    private val dailyLabelList: List<String>
) :
    RecyclerView.Adapter<SelectDailyLabelAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_daily_label_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dailyLabelList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dailyLabel = dailyLabelList[position]
        holder.tvDailyLabel.text = dailyLabel

        holder.labelContainer.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labelContainer: MaterialCardView = itemView.findViewById(R.id.label_container)
        val tvDailyLabel: MaterialTextView = itemView.findViewById(R.id.tv_daily_label)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener = onItemClickListener
    }
}