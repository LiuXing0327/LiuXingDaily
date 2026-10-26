/*
 * Copyright 2026 流星
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liuxing.daily.ui.compose.theme

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.liuxing.daily.util.ConstUtil

/**
 * Defines the background style for daily entry cards.
 */
sealed class CardStyle {


    /**
     * Solid color card background.
     */
    data class SolidColor(@ColorInt val colorInt: Int) : CardStyle() {
        val composeColor: Color get() = Color(colorInt)
    }

    /**
     * Illustration card background.
     */
    data class Illustration(
        @DrawableRes val drawableRes: Int,
        @ColorInt val textColorInt: Int = android.graphics.Color.TRANSPARENT
    ) : CardStyle()

    data class Glass(val alpha: Float = 0.7f) : CardStyle()
}

/**
 * Resolves [CardStyle] for standard XML Views / Adapters / Activities.
 *
 * @param context Context for resolving colors.
 * @param backgroundColorIndex Index of the background color in [ConstUtil.backgroundColorList].
 * @param isFull Whether to use full illustration ([ConstUtil.illustrationThemeFullList]) for detail/edit views.
 */
fun getCardStyle(context: Context, backgroundColorIndex: Int?, isFull: Boolean = false): CardStyle {
    val index = backgroundColorIndex ?: 0

    return when {
        // Range for Illustrated themes (index >= 100)
        index >= 100 -> {
            val list = if (isFull) ConstUtil.illustrationThemeFullList else ConstUtil.illustrationThemeList
            val illustrationRes = list.getOrNull(index - 100) ?: 0
            if (illustrationRes != 0) {
                CardStyle.Illustration(drawableRes = illustrationRes)
            } else {
                CardStyle.SolidColor(android.graphics.Color.TRANSPARENT)
            }
        }
        // Range for Solid Colors (0..99)
        else -> {
            val colorRes =
                ConstUtil.backgroundColorList.getOrElse(index) { android.R.color.transparent }
            val colorInt = ContextCompat.getColor(context, colorRes)
            CardStyle.SolidColor(colorInt)
        }
    }
}

/**
 * Resolves [CardStyle] for Jetpack Compose UI.
 */
@Composable
fun getCardStyle(backgroundColorIndex: Int?, isFull: Boolean = false): CardStyle {
    val context = LocalContext.current

    return getCardStyle(
        context = context,
        backgroundColorIndex = backgroundColorIndex,
        isFull = isFull
    )
}