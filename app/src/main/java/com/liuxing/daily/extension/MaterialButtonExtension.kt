/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.extension

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.liuxing.daily.R

/**
 * 默认图标。
 */
private var defIcon: Drawable? = null

/**
 * 初始化默认图标
 *
 * @param defDrawable 默认 Drawable.
 */
fun MaterialButton.initDefIcon(defDrawable: Drawable?) {
    defIcon = defDrawable
}

/**
 * 根据 [doneId] 切换完成图标的函数，默认传入 Id 与当前按钮 Id 不同则应用默认图标。
 *
 * 在使用此函数前应先调用 [initDefIcon] 进行默认图标的初始化。
 *
 * @param doneId 完成图标的按钮 Id.
 */
fun MaterialButton.toggleDoneIcon(doneId: Int) {
    icon = if (doneId == id) {
        ContextCompat.getDrawable(context, R.drawable.baseline_toolbar_done_24)
    } else {
        defIcon
    }
}