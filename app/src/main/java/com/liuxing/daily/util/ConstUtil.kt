package com.liuxing.daily.util

import com.liuxing.daily.R


object ConstUtil {
    // 检查醒悟更新
    const val CHECK_APP_VERSION_URL =
        "https://gitee.com/LiuXing0327/app-version/raw/master/Daily/CheckUpdate/DailyVersion.json"

    // 日记列表类型
    const val VIEW_TYPE_HEADER = 0
    const val VIEW_TYPE_DAILY = 1

    // 日期时间格式
    val DATE_TIME_FORMAT =
        arrayOf("yyyy年MM月dd日 HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss")

    // 心情图片
    val moodList = listOf(
        R.drawable.ic_happy,
        R.drawable.ic_quiet,
        R.drawable.ic_not_happy,
        R.drawable.ic_angry,
        R.drawable.ic_tired,
        R.drawable.ic_sad,
        R.drawable.ic_agitated,
        R.drawable.baseline_daily_mw_not_add_24
    )

    // 天气图片
    val weatherList: List<Int> =
        listOf(
            R.drawable.baseline_wb_sunny_24,
            R.drawable.ic_overcast,
            R.drawable.ic_cloudy,
            R.drawable.ic_snow_day,
            R.drawable.ic_thunderstorm,
            R.drawable.ic_rain_day,
            R.drawable.ic_gale,
            R.drawable.ic_haze,
            R.drawable.baseline_daily_mw_not_add_24
        )

    // 日记背景颜色
    val backgroundColorList = listOf(
        android.R.color.transparent,
        R.color.color_2,
        R.color.color_3,
        R.color.color_4,
        R.color.color_5,
        R.color.color_6
    )
}