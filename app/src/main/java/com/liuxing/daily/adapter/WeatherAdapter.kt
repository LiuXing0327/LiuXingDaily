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
 * DateTime：2024/10/23 19:08
 * Description：天气适配器
 */
class WeatherAdapter(context: Context) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    private val weatherTextList: List<String> =
        listOf(
            context.getString(R.string.sunny),
            context.getString(R.string.overcast),
            context.getString(R.string.cloudy),
            context.getString(R.string.snow_day),
            context.getString(R.string.thunderstorm),
            context.getString(R.string.rain_day),
            context.getString(R.string.gale),
            context.getString(R.string.haze),
            context.getString(R.string.not_add)
        )

    /**
     *             R.drawable.baseline_wb_sunny_24,
     *             R.drawable.ic_overcast,
     *             R.drawable.ic_cloudy,
     *             R.drawable.ic_snow_day,
     *             R.drawable.ic_thunderstorm,
     *             R.drawable.ic_rain_day,
     *             R.drawable.ic_gale,
     *             R.drawable.ic_haze,
     *             R.drawable.baseline_daily_mw_not_add_24
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_weather_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ConstUtil.weatherList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivWeather.setImageDrawable(
            ContextCompat.getDrawable(
                holder.ivWeather.context,
                ConstUtil.weatherList[position]
            )
        )
        holder.tvWeather.text = weatherTextList[position]
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivWeather: ImageView = itemView.findViewById(R.id.iv_weather)
        val tvWeather: MaterialTextView = itemView.findViewById(R.id.tv_weather)

    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}