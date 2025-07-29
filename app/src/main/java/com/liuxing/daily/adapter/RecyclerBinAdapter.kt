package com.liuxing.daily.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.entity.DailyEntity
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.listener.OnItemLongClickListener
import com.liuxing.daily.listener.OnItemSelectedStateChangedListener
import com.liuxing.daily.util.ConstUtil
import com.liuxing.daily.util.ConstUtil.VIEW_TYPE_DAILY
import com.liuxing.daily.util.ConstUtil.VIEW_TYPE_HEADER
import com.liuxing.daily.util.DateUtil
import com.liuxing.daily.util.FileUtil
import com.liuxing.daily.util.TextUtil
import java.io.File
import java.util.Date


class RecyclerBinAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categorizedList: List<Any> = ArrayList()
    var headerYearMonth: Boolean = true
    private var imageMap: Map<String, String> = emptyMap()
    private var onItemClickListener: OnItemClickListener? = null
    private var onItemLongClickListener: OnItemLongClickListener? = null
    var autoDeleteDays: Int = 0
    private lateinit var sharedPreferences: SharedPreferences
    var textSize = 16F
    var alpha = 0.15f
    var imageDisplay = false
    var selectMode = false
    private var onItemSelectedStateChangedListener: OnItemSelectedStateChangedListener? = null
    private val selectItems = mutableSetOf<String>()

    fun setDailyList(
        context: Context,
        dailyList: List<DailyEntity>,
        imageMap: Map<String, String>
    ) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val currentSortIndex = sharedPreferences.getInt("daily_sort_by", 0)
        this.imageMap = imageMap

        // 过滤被回收的数据
        val filteredList = dailyList.filter { it.isDeleted }
        val sortedByDescending =
            filteredList.withIndex().sortedByDescending { it.value.dailyRecyclerDateTime }

        val groupedMap = when (currentSortIndex) {
            1 -> sortedByDescending.sortedBy { it.value.dailyRecyclerDateTime }
                .groupBy {
                    DateUtil.getDateString(0, Date(it.value.dailyRecyclerDateTime!!))
                        .substring(0, 7)
                }

            else -> sortedByDescending.sortedByDescending { it.value.dailyRecyclerDateTime }
                .groupBy {
                    DateUtil.getDateString(0, Date(it.value.dailyRecyclerDateTime!!))
                        .substring(0, 7)
                }
        }

        val toSortedMap = groupedMap.mapKeys { dailyEntity ->
            dailyEntity.key to DateUtil.getDateString(
                0,
                Date(dailyEntity.value.first().value.dailyRecyclerDateTime!!)
            )
        }.mapKeys { it.key.first }

        val resultList = mutableListOf<Any>()
        toSortedMap.forEach { (yearMonth, list) ->
            val headerBoolean = sharedPreferences.getBoolean("switch_preference_header_display", true)
            // 判断设置开关添加 -> 年月 ?: 月
            if (headerBoolean) {
                headerYearMonth = true
                resultList.add(yearMonth)
            } else {
                headerYearMonth = false
                val month = yearMonth.substring(5, 7).toInt()
                resultList.add(month.toString())
            }

            // 根据排序方式添加 DailyEntity 及其索引
            when (currentSortIndex) {
                1 -> resultList.addAll(list.sortedBy { it.value.dailyRecyclerDateTime }
                    .map { Pair(it.value, it.index) })

                else -> resultList.addAll(list.sortedByDescending { it.value.dailyRecyclerDateTime }
                    .map { Pair(it.value, it.index) })
            }
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

    @SuppressLint("StringFormatMatches")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder) {
            holder.tvDateHeader.text = categorizedList[position] as String
        } else if (holder is DailyViewHolder) {
            val (dailyEntity, originalIndex) = categorizedList[position] as Pair<DailyEntity, Int>
            holder.tvTitle.visibility = if (dailyEntity.title.equals("")) {
                View.GONE
            } else {
                View.VISIBLE
            }
            holder.tvContent.visibility = if (dailyEntity.content.equals("")) {
                View.GONE
            } else {
                View.VISIBLE
            }
            textSize = sharedPreferences.getFloat(ConstUtil.TEXT_SIZE_KEY,16F)
            holder.tvTitle.textSize = textSize + 4
            holder.tvContent.textSize = textSize
            if (dailyEntity.singlePassword == "" || dailyEntity.singlePassword == null) {
                holder.tvTitle.text = dailyEntity.title
                holder.tvContent.text = TextUtil.replaceTag(dailyEntity.content!!)
            } else {
                holder.tvTitle.text = "***"
                holder.tvContent.text = "***"
            }
            val startDate = DateUtil.getDateString(1, Date(dailyEntity.dailyRecyclerDateTime!!))
            val daysBetween = DateUtil.getDaysBetween(startDate)
            autoDeleteDays = sharedPreferences.getInt("auto_delete_recycler_bin_daily", 7)
            val dateString = DateUtil.getDateString(0, Date(dailyEntity.dailyRecyclerDateTime!!))
            if (autoDeleteDays != 0) {
                val remainingDays = autoDeleteDays - daysBetween
                holder.tvDateTime.text = holder.tvDateTime.context.getString(
                    R.string.auto_delete_message,
                    dateString,
                    remainingDays
                )
            } else {
                holder.tvDateTime.text = dateString
            }
            setBackgroundColor(dailyEntity, holder)
            holder.ivMood.visibility =
                if (dailyEntity.moodIndex == 0 || dailyEntity.moodIndex == null) {
                    View.GONE
                } else {
                    holder.ivMood.setImageDrawable(
                        ContextCompat.getDrawable(
                            holder.ivMood.context,
                            ConstUtil.moodList[dailyEntity.moodIndex.minus(1)] // 将索引减1，得到原始索引
                        )
                    )
                    View.VISIBLE
                }
            holder.ivWeather.visibility =
                if (dailyEntity.weatherIndex == 0 || dailyEntity.weatherIndex == null) {
                    View.GONE
                } else {
                    holder.ivWeather.setImageDrawable(
                        ContextCompat.getDrawable(
                            holder.ivWeather.context,
                            ConstUtil.weatherList[dailyEntity.weatherIndex.minus(1)] // 将索引减1，得到原始索引
                        )
                    )
                    View.VISIBLE
                }
            imageDisplay =
                sharedPreferences.getBoolean(ConstUtil.DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY, false)
            val imagePath = imageMap[dailyEntity.dailyUUID]
            if (!imageDisplay && !imagePath.isNullOrEmpty() && FileUtil().checkFileExists(imagePath)) {
                Glide.with(holder.imageView.context)
                    .load(imagePath)
                    .into(holder.imageView)
                holder.imageView.visibility = View.VISIBLE
            } else {
                holder.imageView.visibility = View.GONE
            }

            holder.labelContainer.visibility =
                if (dailyEntity.dailyLabel.isNullOrEmpty()) View.GONE else {
                    holder.tvLabel.text = dailyEntity.dailyLabel
                    View.VISIBLE
                }

            holder.cardView.isChecked = selectMode && selectItems.contains(dailyEntity.dailyUUID)

            // 将原始索引传递给点击事件处理
            holder.itemView.setOnClickListener {
                if (selectMode) {
                    toggleSelection(dailyEntity.dailyUUID!!)
                    notifyItemChanged(position)
                } else {
                    onItemClickListener?.onItemClick(originalIndex)
                }
            }
            holder.itemView.setOnLongClickListener {
                if (!selectMode) {
                    selectMode = true
                    toggleSelection(dailyEntity.dailyUUID!!)
                    notifyItemChanged(position)
                }

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
        val ivMood: ImageView = itemView.findViewById(R.id.iv_mood)
        val ivWeather: ImageView = itemView.findViewById(R.id.iv_weather)
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val labelContainer: MaterialCardView = itemView.findViewById(R.id.label_container)
        val tvLabel: MaterialTextView = itemView.findViewById(R.id.tv_label)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    fun setOnItemSelectedStateChangedListener(listener: OnItemSelectedStateChangedListener) {
        onItemSelectedStateChangedListener = listener
    }

    private fun toggleSelection(uuid: String) {
        if (selectItems.contains(uuid)) {
            selectItems.remove(uuid)
        } else {
            selectItems.add(uuid)
        }
        onItemSelectedStateChangedListener?.onSelectionChanged(selectItems.size)
    }

    fun getSelectedItemsCount(): Int = selectItems.size

    fun getSelectedItems(): List<String> = selectItems.toList()

    @SuppressLint("NotifyDataSetChanged")
    fun clearSection() {
        selectItems.clear()
        selectMode = false
        notifyDataSetChanged()
        onItemSelectedStateChangedListener?.onSelectionChanged(0)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun selectAllItems() {
        if (!selectMode) return

        selectItems.clear()
        categorizedList.forEachIndexed { _, item ->
            if (item is Pair<*, *>) {
                val uuid = (item.first as? DailyEntity)?.dailyUUID
                if (uuid != null) {
                    selectItems.add(uuid)
                }
            }
        }
        notifyDataSetChanged()
        onItemSelectedStateChangedListener?.onSelectionChanged(selectItems.size)
    }

    /**
     * 设置背景颜色
     *
     * @param dailyEntity 日记 Entity
     * @param holder 日记 Holder
     */
    private fun setBackgroundColor(dailyEntity: DailyEntity, holder: DailyViewHolder) {
        val backgroundColorIndex = dailyEntity.backgroundColorIndex
        val baseColor = ContextCompat.getColor(
            holder.cardView.context,
            ConstUtil.backgroundColorList[backgroundColorIndex!!]
        )
        val alpha = sharedPreferences.getFloat(ConstUtil.WALLPAPER_ALPHA_KEY, 0.15F)
        this.alpha = alpha
        val wallPagerExists = File(ConstUtil.WALLPAPER_PATH).exists()

        holder.cardView.setCardBackgroundColor(
            if (backgroundColorIndex == 0 || !wallPagerExists) baseColor else ColorUtils.setAlphaComponent(
                baseColor,
                (alpha * 255).toInt()
            )
        )
    }
}