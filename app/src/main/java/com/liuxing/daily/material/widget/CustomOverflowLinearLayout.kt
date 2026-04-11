/*
 * Copyright (c) 2026 流星
 */

package com.liuxing.daily.material.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.overflow.OverflowLinearLayout
import com.liuxing.daily.R

class CustomOverflowLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : OverflowLinearLayout(context, attrs, defStyleAttr) {

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return CustomLayoutParams(context, attrs)
    }

    class CustomLayoutParams : LayoutParams {
        var overflowIconTint: ColorStateList? = null

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            context.withStyledAttributes(
                attrs,
                R.styleable.CustomOverflowLinearLayout_Layout
            ) {
                if (hasValue(R.styleable.CustomOverflowLinearLayout_Layout_layout_overflowIconTint)) {
                    overflowIconTint =
                        getColorStateList(R.styleable.CustomOverflowLinearLayout_Layout_layout_overflowIconTint)
                }
            }

            if (overflowIcon != null && overflowIconTint != null) {
                val tinted = DrawableCompat.wrap(overflowIcon!!.mutate())
                DrawableCompat.setTintList(tinted, overflowIconTint)
                overflowIcon = tinted
            }
        }
    }
}