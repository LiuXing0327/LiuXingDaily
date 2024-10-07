package com.liuxing.daily.util


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
}