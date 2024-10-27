package com.liuxing.daily.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.util.ConstUtil

/**
 * Author：流星
 * DateTime：2024/10/22 下午6:54
 * Description：心情适配器
 */
class MoodAdapter(context: Context) : RecyclerView.Adapter<MoodAdapter.ViewHolder>() {

    private val moodTextList: List<String> = listOf(
        context.getString(R.string.happy),
        context.getString(
            R.string.quiet
        ),
        context.getString(
            R.string.not_happy
        ),
        context.getString(R.string.angry),
        context.getString(R.string.tired),
        context.getString(R.string.sad),
        context.getString(R.string.agitated),
        context.getString(R.string.not_add)
    )
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_mood_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ConstUtil.moodList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivMood.setImageDrawable(
            ContextCompat.getDrawable(
                holder.ivMood.context,
                ConstUtil.moodList[position]
            )
        )
        holder.tvMood.text = moodTextList[position]
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMood: ImageView = itemView.findViewById(R.id.iv_mood)
        val tvMood: MaterialTextView = itemView.findViewById(R.id.tv_mood)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}