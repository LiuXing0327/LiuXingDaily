package com.liuxing.daily.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.liuxing.daily.R

/**
 * Author：流星
 * DateTime：2024/10/24 14:27
 * Description：日记图片页适配器
 */
class DailyImagePagerAdapter(private val imageList: List<String>, private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<DailyImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView: ImageView = view.findViewById(R.id.daily_image)

        fun bind(imageUri: String, position: Int) {
            Glide.with(imageView.context).load(imageUri).into(imageView)
            imageView.setOnClickListener { onClick(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_daily_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageList[position], position)
    }

    override fun getItemCount() = imageList.size
}