package com.liuxing.daily.adapter

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.liuxing.daily.R
import com.liuxing.daily.data.SpecialThanksData

/**
 * Author：流星
 * DateTime：2024/10/15 下午8:30
 * Description：特别鸣谢列表适配器
 */
class SpecialThanksAdapter : RecyclerView.Adapter<SpecialThanksAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_icon)
        val tvSpecialThanksName: MaterialTextView =
            itemView.findViewById(R.id.tv_special_thanks_name)
        val tvLink: MaterialTextView = itemView.findViewById(R.id.tv_link)
        val tvRemark: MaterialTextView = itemView.findViewById(R.id.tv_remark)
    }

    private var specialThanksList: List<SpecialThanksData> = ArrayList()

    fun setSpecialThanksList(specialThanksList: List<SpecialThanksData>) {
        this.specialThanksList = specialThanksList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_special_thanks_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = specialThanksList.size

    private val hashMap = HashMap<String, Int>()
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val specialThanksData = specialThanksList[position]
        hashMap[specialThanksData.name.toString()] = 1
        when (specialThanksData.intIndex) {
            0 -> {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.ivIcon.context,
                        R.drawable.baseline_person_24
                    )
                )
            }

            else -> {
                holder.ivIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.ivIcon.context,
                        R.drawable.baseline_public_24
                    )
                )
            }
        }
        holder.tvSpecialThanksName.text = specialThanksData.name
        holder.tvLink.text = Html.fromHtml("<a href='${specialThanksData.link}'>${specialThanksData.link}</a>", Html.FROM_HTML_MODE_COMPACT)
        holder.tvLink.movementMethod = LinkMovementMethod.getInstance()
        holder.tvRemark.text = specialThanksData.remark
    }
}