/*
 * Copyright (c) 2025 流星
 */

package com.liuxing.daily.material.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.google.android.material.materialswitch.MaterialSwitch
import com.liuxing.daily.R
import com.liuxing.daily.extension.toggleSwitchIcon

class DailyMaterialSwitch(context: Context, attrs: AttributeSet) : MaterialSwitch(context, attrs) {

    /**
     * 是否切换开关图标，默认切换
     */
    private var toggleSwitchIcon: Boolean = true

    /**
     * 勾选图标
     */
    private var checkedIcon = 0

    /**
     * 未勾选图标
     */
    private var unCheckedIcon = 0

    /**
     * 固定图标（不随选中状态变化）
     */
    private var staticIcon = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.DailyMaterialSwitch) {
            toggleSwitchIcon = getBoolean(
                R.styleable.DailyMaterialSwitch_toggle_switch_icon, true
            )

            checkedIcon = getResourceId(
                R.styleable.DailyMaterialSwitch_checked_icon, R.drawable.baseline_toolbar_done_24
            )

            unCheckedIcon = getResourceId(
                R.styleable.DailyMaterialSwitch_unchecked_icon, R.drawable.baseline_toolbar_close_24
            )

            staticIcon = getResourceId(
                R.styleable.DailyMaterialSwitch_static_icon, R.drawable.baseline_toolbar_done_24
            )

            thumbIconDrawable = ContextCompat.getDrawable(context, staticIcon)
        }
    }

    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        if (toggleSwitchIcon) {
            toggleSwitchIcon(checkedIcon, unCheckedIcon)
        }
    }
}