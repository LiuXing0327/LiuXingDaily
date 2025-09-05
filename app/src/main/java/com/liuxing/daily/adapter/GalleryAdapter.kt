/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.liuxing.daily.R
import com.liuxing.daily.listener.OnItemClickListener
import com.liuxing.daily.ui.gallery.LookGalleryImageActivity
import com.liuxing.daily.ui.image.LookDailyImageActivity
import com.liuxing.daily.util.FileUtil
import java.io.File

/**
 * 图库适配器
 */
class GalleryAdapter(private val imageList: List<File>) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileUtil = FileUtil()
        val imageFile = imageList[position]
        if (fileUtil.checkFileExists(imageFile.toString())) {
            Glide.with(holder.imageView.context).load(imageFile).into(holder.imageView)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, LookGalleryImageActivity::class.java).apply {
                putExtra("look_all_daily_image_position", position)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }
}