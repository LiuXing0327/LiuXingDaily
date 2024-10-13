package com.liuxing.daily.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.util.ConstUtil.VIEW_TYPE_DAILY
import com.liuxing.daily.util.ConstUtil.VIEW_TYPE_HEADER
import com.liuxing.daily.util.DateUtil
import java.util.Date
import java.util.Objects

class DailyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categorizedList: List<Any> = ArrayList()
    var headerYearMonth: Boolean = true

    fun setDailyList(context: Context, dailyList: List<DailyEntity>) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val groupedMap = dailyList.withIndex()
            .groupBy { DateUtil.getDateString(2, Date(it.value.dateTime!!)).substring(0, 7) }
        val toSortedMap = groupedMap.mapKeys { dailyEntity ->
            dailyEntity.key to DateUtil.getDateString(
                2,
                Date(dailyEntity.value.first().value.dateTime!!)
            )
        }.mapKeys { it.key.first }
        val resultList = mutableListOf<Any>()
        toSortedMap.forEach { (yearMonth, list) ->
            val headerBoolean = sharedPreferences.getBoolean(
                "switch_preference_header_display",
                true
            )
            headerYearMonth = headerBoolean
            // 判断设置开关添加 -> 年月 ?: 月
            when {
                headerBoolean -> {
                    resultList.add(yearMonth)
                }

                else -> {
                    when {
                        Objects.equals(yearMonth.substring(5, 6), "0") -> {
                            resultList.add(yearMonth.substring(6, 7))
                        }

                        else -> {
                            resultList.add(yearMonth.substring(5, 7))
                        }
                    }
                }
            }
            // 添加当天的 DailyEntity 及其索引
            resultList.addAll(list.sortedByDescending { it.value.dateTime }
                .map { Pair(it.value, it.index) })
        }
        categorizedList = resultList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (categorizedList[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_DAILY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_header, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_daily_list, parent, false)
            DailyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder) {
            holder.tvDateHeader.text = categorizedList[position] as String
        } else if (holder is DailyViewHolder) {
            val (dailyEntity, originalIndex) = categorizedList[position] as Pair<DailyEntity, Int>
            if (dailyEntity.title.equals("")) {
                holder.tvTitle.visibility = View.GONE
            } else {
                holder.tvTitle.visibility = View.VISIBLE
            }
            if (dailyEntity.content.equals("")) {
                holder.tvContent.visibility = View.GONE
            } else {
                holder.tvContent.visibility = View.VISIBLE
            }
            holder.tvTitle.text = dailyEntity.title
            holder.tvContent.text = dailyEntity.content
            holder.tvDateTime.text = DateUtil.getDateString(2, Date(dailyEntity.dateTime!!))
            setBackgroundColor(dailyEntity, holder)
            // 将原始索引传递给点击事件处理
            holder.itemView.setOnClickListener {
                onItemClickListener?.onItemClick(originalIndex)
            }
            holder.itemView.setOnLongClickListener {
                onItemLongClickListener?.onItemLongOnClick(originalIndex)
                true
            }
        }
    }

    override fun getItemCount(): Int = categorizedList.size

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDateHeader: MaterialTextView = itemView.findViewById(R.id.tv_year_date)
    }

    class DailyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: MaterialTextView = itemView.findViewById(R.id.tv_title)
        val tvContent: MaterialTextView = itemView.findViewById(R.id.tv_content)
        val tvDateTime: MaterialTextView = itemView.findViewById(R.id.tv_date_time)
        val cardView: MaterialCardView = itemView.findViewById(R.id.main_layout)
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
     *
     * @param dailyEntity 日记 Entity
     * @param holder 日记 Holder
     */
    private fun setBackgroundColor(dailyEntity: DailyEntity, holder: DailyViewHolder) {
        val backgroundColorIndex = dailyEntity.backgroundColorIndex
        when (backgroundColorIndex) {
            1 -> holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.cardView.context, R.color.color_2)
            )

            2 -> holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.cardView.context, R.color.color_3)
            )

            3 -> holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.cardView.context, R.color.color_4)
            )

            else -> holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.cardView.context, android.R.color.transparent)
            )
        }
    }
}