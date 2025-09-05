package com.liuxing.daily.util

import com.liuxing.daily.R


object ConstUtil {
    // 检查醒悟更新
    const val CHECK_APP_VERSION_URL =
        "https://gitee.com/LiuXing0327/app-version/raw/master/Daily/CheckUpdate/DailyVersion.json"

    // 日记列表类型
    const val VIEW_TYPE_HEADER = 0
    const val VIEW_TYPE_DAILY = 1
    const val VIEW_TYPE_PINNED = 2

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

    // 心情的标签
    val moodLabelList = listOf(
        R.string.happy,
        R.string.quiet,
        R.string.not_happy,
        R.string.angry,
        R.string.tired,
        R.string.sad,
        R.string.agitated,
        R.string.not_add
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
    val weatherLabelList = listOf(
        R.string.sunny,
        R.string.overcast,
        R.string.cloudy,
        R.string.snow_day,
        R.string.thunderstorm,
        R.string.rain_day,
        R.string.gale,
        R.string.haze,
        R.string.not_add
    )

    // 日记背景颜色
    val backgroundColorList = listOf(
        android.R.color.transparent,
        R.color.color_2,
        R.color.color_3,
        R.color.color_4,
        R.color.color_5,
        R.color.color_6,
        R.color.color_7
    )

    // 日记背景颜色的标签
    val backgroundColorLabelList = listOf(
        R.string.color_label_transparent,
        R.string.color_label_purple,
        R.string.color_label_red,
        R.string.color_label_green,
        R.string.color_label_blue,
        R.string.color_label_yellow,
        R.string.color_label_pink
    )

    // 媒体标签正则
    val imageRegex = Regex("<img\\s+src=\"(.*?)\"\\s*/?>", RegexOption.IGNORE_CASE)
    val videoRegex = Regex("<video\\s+src=\"(.*?)\"\\s*/?>", RegexOption.IGNORE_CASE)
    val audioRegex = Regex("<audio\\s+src=\"(.*?)\"\\s*/?>", RegexOption.IGNORE_CASE)

    // 字体大小键
    const val TEXT_SIZE_KEY = "text_font_size_preference"

    // 壁纸路径
    const val WALLPAPER_PATH = "/storage/emulated/0/Android/data/com.liuxing.daily/files/Wallpaper/wallpaper.jpg"
    // 壁纸透明度键
    const val WALLPAPER_ALPHA_KEY = "background_image_alpha"

    // 日记列表图片显示键
    const val DAILY_LIST_FIRST_IMAGE_DISPLAY_KEY = "switch_preference_image_display"

    const val TERMS_AND_PRIVACY_AGREED_KEY = "terms_and_privacy_agreed"
}