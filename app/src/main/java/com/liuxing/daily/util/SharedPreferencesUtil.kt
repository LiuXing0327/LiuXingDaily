package com.liuxing.daily.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

/**
 * Author：流星
 * DateTime：2024/10/27 10:28
 * Description：偏好工具类
 */
object SharedPreferencesUtil {

    /**
     * 自动保存日记的偏好
     *
     * @param context 上下文
     * @param insertOrUpdate 插入(0)或更新(1)
     * @param title 标题
     * @param content 内容
     * @param dateTime 日期时间
     * @param backgroundColorIndex 背景颜色索引
     * @param singlePassword 单篇日记的密码
     * @param moodIndex 心情索引
     * @param weatherIndex 天气索引
     * @param dailyUuid 日记识别码
     * @param imageListNotNull 图片集合不为空
     * @param dailyLabel 日记标签
     * @param videoListNotNull 视频集合不为空
     * @param audioListNotNull 音频集合不为空
     */
    fun autoSaveDailySharedPreferences(
        context: Context,
        insertOrUpdate: Int,
        title: String,
        content: String,
        dateTime: Long,
        backgroundColorIndex: Int,
        singlePassword: String,
        moodIndex: Int,
        weatherIndex: Int,
        dailyUuid: String,
        imageListNotNull: Boolean,
        dailyLabel: String,
        videoListNotNull: Boolean,
        audioListNotNull: Boolean
    ) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit {
            putInt("switch_preference_auto_save_is_insert_or_update", insertOrUpdate)
            putString(
                "switch_preference_auto_save_title",
                title
            )
            putString(
                "switch_preference_auto_save_content",
                content
            )
            putLong(
                "switch_preference_auto_save_date_time",
                dateTime
            )
            putInt(
                "switch_preference_auto_save_background_color_index",
                backgroundColorIndex
            )
            putString(
                "switch_preference_auto_save_single_password",
                HashUtil.hashSHA256(singlePassword)
            )
            putInt("switch_preference_auto_save_mood_index", moodIndex)
            putInt("switch_preference_auto_save_weather_index", weatherIndex)
            putString("switch_preference_auto_save_daily_uuid", dailyUuid)
            putBoolean(
                "switch_preference_auto_save_image_list_not_null",
                imageListNotNull
            )
            putString("switch_preference_auto_save_daily_label", dailyLabel)
            putBoolean("switch_preference_auto_save_video_list_not_null", videoListNotNull)
            putBoolean("switch_preference_auto_save_video_list_not_null", audioListNotNull)
            apply()
        }
    }

    private const val PREF_NAME = "AppPreferences"
    private var sharedPreferences: SharedPreferences? = null

    /**
     * 获取 SharedPreferences
     *
     * @param context 上下文
     * @return sharedPreferences
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!
    }

    /**
     * 获取 SharedPreferences.Editor
     *
     * @param context 上下文
     * @return SharedPreferences.Editor
     */
    private fun getEditor(context: Context): SharedPreferences.Editor? {
        return getSharedPreferences(context).edit()
    }

    /**
     * 存储数据
     *
     * @param context 上下文
     * @param key 键
     * @param value 值
     */
    fun putInt(context: Context, key: String, value: Int) {
        val editor = getEditor(context) ?: return
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * 获取数据
     *
     * @param context 上下文
     * @param key 键
     * @param defValue 默认值
     * @return 存储的值
     */
    fun getInt(context: Context, key: String, defValue: Int): Int {
        return getSharedPreferences(context).getInt(key, defValue)
    }
}