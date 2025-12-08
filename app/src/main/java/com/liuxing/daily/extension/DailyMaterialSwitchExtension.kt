/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.extension

import androidx.core.content.ContextCompat
import com.liuxing.daily.material.widget.DailyMaterialSwitch

/**
 * 根据 isChecked 状态切换 DailyMaterialSwitch 的图标函数
 *
 * 应在 setChecked() 中或 OnCheckedChangeListener 回调中调用。
 *
 * @param isCheckedIcon 勾选图标 ID
 * @param unCheckedIcon 未勾选图标 ID
 */
fun DailyMaterialSwitch.toggleSwitchIcon(
    isCheckedIcon: Int,
    unCheckedIcon: Int
) {
    val thumbIconResource =
        if (isChecked) isCheckedIcon else unCheckedIcon

    thumbIconDrawable = ContextCompat.getDrawable(context, thumbIconResource)
}