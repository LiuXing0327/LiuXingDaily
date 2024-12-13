package com.liuxing.daily.entity

import com.google.gson.annotations.Expose

/**
 * Author：流星
 * DateTime：2024/10/27 17:48
 * Description：
 */
data class DailyWithMedia(
    @Expose(deserialize = true, serialize = true)
    val dailyEntity: DailyEntity,
    @Expose(deserialize = true, serialize = true)
    val imageList: List<DailyImageEntity>,
    @Expose(deserialize = true, serialize = true)
    val videoList: List<DailyVideoEntity>,
    @Expose(deserialize = true, serialize = true)
    val audioList: List<DailyAudioEntity>
)