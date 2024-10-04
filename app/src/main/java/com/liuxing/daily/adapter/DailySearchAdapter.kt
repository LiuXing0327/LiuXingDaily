package com.liuxing.daily.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.util.DateUtil
import java.util.Date

class DailySearchAdapter : RecyclerView.Adapter<DailySearchAdapter.ViewHolder>() {

    private var dailyList: List<DailyEntity> = ArrayList()

    fun setDailyList(dailyList: List<DailyEntity>) {
        this.dailyList = dailyList;
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_daily_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == holder.layoutPosition) {
            val dailyEntity = dailyList[position]
            if (dailyEntity.title!!.trim().isEmpty()) {
                holder.tvTitle.visibility = View.GONE
            } else {
                holder.tvTitle.visibility = View.VISIBLE
            }
            if (dailyEntity.content!!.trim().isEmpty()) {
                holder.tvContent.visibility = View.GONE
            } else {
                holder.tvContent.visibility = View.VISIBLE
            }
            holder.tvTitle.text = dailyEntity.title
            holder.tvContent.text = dailyEntity.content
            holder.tvDateTime.text = DateUtil.getDateString(2, Date(dailyEntity.dateTime!!))
            setBackgroundColor(dailyEntity, holder)
        }
    }

    override fun getItemCount(): Int = dailyList.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: MaterialTextView = itemView.findViewById(R.id.tv_title)
        val tvContent: MaterialTextView = itemView.findViewById(R.id.tv_content)
        val tvDateTime: MaterialTextView = itemView.findViewById(R.id.tv_date_time)
        val cardView: MaterialCardView = itemView.findViewById(R.id.main_layout)

        init {
            itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.OnItemClick(adapterPosition)
                }
            }
            itemView.setOnLongClickListener {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener!!.onItemLongOnClick(adapterPosition)
                }
                true
            }
        }
    }

    companion object {
        private var onItemClickListener: OnItemClickListener? = null
        private var onItemLongClickListener: OnItemLongClickListener? = null
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    /**
     * 设置背景颜色
     */
    private fun setBackgroundColor(dailyEntity: DailyEntity, holder: ViewHolder) {
        val backgroundColorIndex = dailyEntity.backgroundColorIndex
        when (backgroundColorIndex) {
            1 -> holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.cardView.context,
                    R.color.color_2
                )
            )

            2 -> holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.cardView.context,
                    R.color.color_3
                )
            )

            3 -> holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.cardView.context,
                    R.color.color_4
                )
            )

            else -> {
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(
                        holder.cardView.context,
                        android.R.color.transparent
                    )
                )
            }
        }
    }
}