/*
 * Copyright (c) 2024 流星
 */

package com.liuxing.daily.entity

import com.google.gson.annotations.Expose

data class DailyWithMedia(
    @Expose(deserialize = true, serialize = true)
    val dailyEntity: DailyEntity,
    @Expose(deserialize = true, serialize = true)
    val imageList: List<DailyImageEntity>,
    @Expose(deserialize = true, serialize = true)
    val videoList: List<DailyVideoEntity>,
    @Expose(deserialize = true, serialize = true)
    val audioList: List<DailyAudioEntity>,
)