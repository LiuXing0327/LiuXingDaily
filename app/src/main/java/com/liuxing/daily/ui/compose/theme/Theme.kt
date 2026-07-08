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

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.liuxing.daily.R

/**
 * 主题类型定义
 */
enum class DailyThemeType {
    DEFAULT, RED, GREEN, BLUE, YELLOW, PINK, CYAN
}

private data class ThemeColorSpec(
    @field:ColorRes val primary: Int,
    @field:ColorRes val onPrimary: Int,
    @field:ColorRes val primaryContainer: Int,
    @field:ColorRes val onPrimaryContainer: Int,
    @field:ColorRes val secondary: Int,
    @field:ColorRes val onSecondary: Int,
    @field:ColorRes val secondaryContainer: Int,
    @field:ColorRes val onSecondaryContainer: Int,
    @field:ColorRes val tertiary: Int,
    @field:ColorRes val onTertiary: Int,
    @field:ColorRes val tertiaryContainer: Int,
    @field:ColorRes val onTertiaryContainer: Int,
    @field:ColorRes val error: Int,
    @field:ColorRes val onError: Int,
    @field:ColorRes val errorContainer: Int,
    @field:ColorRes val onErrorContainer: Int,
    @field:ColorRes val background: Int,
    @field:ColorRes val onBackground: Int,
    @field:ColorRes val surface: Int,
    @field:ColorRes val onSurface: Int,
    @field:ColorRes val surfaceVariant: Int,
    @field:ColorRes val onSurfaceVariant: Int,
    @field:ColorRes val outline: Int,
    @field:ColorRes val outlineVariant: Int,
    @field:ColorRes val inverseSurface: Int,
    @field:ColorRes val inverseOnSurface: Int,
    @field:ColorRes val inversePrimary: Int,
    @field:ColorRes val primaryFixed: Int,
    @field:ColorRes val onPrimaryFixed: Int,
    @field:ColorRes val primaryFixedDim: Int,
    @field:ColorRes val onPrimaryFixedVariant: Int,
    @field:ColorRes val secondaryFixed: Int,
    @field:ColorRes val onSecondaryFixed: Int,
    @field:ColorRes val secondaryFixedDim: Int,
    @field:ColorRes val onSecondaryFixedVariant: Int,
    @field:ColorRes val tertiaryFixed: Int,
    @field:ColorRes val onTertiaryFixed: Int,
    @field:ColorRes val tertiaryFixedDim: Int,
    @field:ColorRes val onTertiaryFixedVariant: Int,
    @field:ColorRes val surfaceDim: Int,
    @field:ColorRes val surfaceBright: Int,
    @field:ColorRes val surfaceContainerLowest: Int,
    @field:ColorRes val surfaceContainerLow: Int,
    @field:ColorRes val surfaceContainer: Int,
    @field:ColorRes val surfaceContainerHigh: Int,
    @field:ColorRes val surfaceContainerHighest: Int,
)

internal val DefaultLightColorScheme = lightColorScheme()
internal val DefaultDarkColorScheme = darkColorScheme()

private val RedSpec = ThemeColorSpec(
    primary = R.color.md_theme_primary_red,
    onPrimary = R.color.md_theme_onPrimary_red,
    primaryContainer = R.color.md_theme_primaryContainer_red,
    onPrimaryContainer = R.color.md_theme_onPrimaryContainer_red,
    secondary = R.color.md_theme_secondary_red,
    onSecondary = R.color.md_theme_onSecondary_red,
    secondaryContainer = R.color.md_theme_secondaryContainer_red,
    onSecondaryContainer = R.color.md_theme_onSecondaryContainer_red,
    tertiary = R.color.md_theme_tertiary_red,
    onTertiary = R.color.md_theme_onTertiary_red,
    tertiaryContainer = R.color.md_theme_tertiaryContainer_red,
    onTertiaryContainer = R.color.md_theme_onTertiaryContainer_red,
    error = R.color.md_theme_error_red,
    onError = R.color.md_theme_onError_red,
    errorContainer = R.color.md_theme_errorContainer_red,
    onErrorContainer = R.color.md_theme_onErrorContainer_red,
    background = R.color.md_theme_background_red,
    onBackground = R.color.md_theme_onBackground_red,
    surface = R.color.md_theme_surface_red,
    onSurface = R.color.md_theme_onSurface_red,
    surfaceVariant = R.color.md_theme_surfaceVariant_red,
    onSurfaceVariant = R.color.md_theme_onSurfaceVariant_red,
    outline = R.color.md_theme_outline_red,
    outlineVariant = R.color.md_theme_outlineVariant_red,
    inverseSurface = R.color.md_theme_inverseSurface_red,
    inverseOnSurface = R.color.md_theme_inverseOnSurface_red,
    inversePrimary = R.color.md_theme_inversePrimary_red,
    primaryFixed = R.color.md_theme_primaryFixed_red,
    onPrimaryFixed = R.color.md_theme_onPrimaryFixed_red,
    primaryFixedDim = R.color.md_theme_primaryFixedDim_red,
    onPrimaryFixedVariant = R.color.md_theme_onPrimaryFixedVariant_red,
    secondaryFixed = R.color.md_theme_secondaryFixed_red,
    onSecondaryFixed = R.color.md_theme_onSecondaryFixed_red,
    secondaryFixedDim = R.color.md_theme_secondaryFixedDim_red,
    onSecondaryFixedVariant = R.color.md_theme_onSecondaryFixedVariant_red,
    tertiaryFixed = R.color.md_theme_tertiaryFixed_red,
    onTertiaryFixed = R.color.md_theme_onTertiaryFixed_red,
    tertiaryFixedDim = R.color.md_theme_tertiaryFixedDim_red,
    onTertiaryFixedVariant = R.color.md_theme_onTertiaryFixedVariant_red,
    surfaceDim = R.color.md_theme_surfaceDim_red,
    surfaceBright = R.color.md_theme_surfaceBright_red,
    surfaceContainerLowest = R.color.md_theme_surfaceContainerLowest_red,
    surfaceContainerLow = R.color.md_theme_surfaceContainerLow_red,
    surfaceContainer = R.color.md_theme_surfaceContainer_red,
    surfaceContainerHigh = R.color.md_theme_surfaceContainerHigh_red,
    surfaceContainerHighest = R.color.md_theme_surfaceContainerHighest_red,
)
private val GreenSpec = ThemeColorSpec(
    primary = R.color.md_theme_primary_green,
    onPrimary = R.color.md_theme_onPrimary_green,
    primaryContainer = R.color.md_theme_primaryContainer_green,
    onPrimaryContainer = R.color.md_theme_onPrimaryContainer_green,
    secondary = R.color.md_theme_secondary_green,
    onSecondary = R.color.md_theme_onSecondary_green,
    secondaryContainer = R.color.md_theme_secondaryContainer_green,
    onSecondaryContainer = R.color.md_theme_onSecondaryContainer_green,
    tertiary = R.color.md_theme_tertiary_green,
    onTertiary = R.color.md_theme_onTertiary_green,
    tertiaryContainer = R.color.md_theme_tertiaryContainer_green,
    onTertiaryContainer = R.color.md_theme_onTertiaryContainer_green,
    error = R.color.md_theme_error_green,
    onError = R.color.md_theme_onError_green,
    errorContainer = R.color.md_theme_errorContainer_green,
    onErrorContainer = R.color.md_theme_onErrorContainer_green,
    background = R.color.md_theme_background_green,
    onBackground = R.color.md_theme_onBackground_green,
    surface = R.color.md_theme_surface_green,
    onSurface = R.color.md_theme_onSurface_green,
    surfaceVariant = R.color.md_theme_surfaceVariant_green,
    onSurfaceVariant = R.color.md_theme_onSurfaceVariant_green,
    outline = R.color.md_theme_outline_green,
    outlineVariant = R.color.md_theme_outlineVariant_green,
    inverseSurface = R.color.md_theme_inverseSurface_green,
    inverseOnSurface = R.color.md_theme_inverseOnSurface_green,
    inversePrimary = R.color.md_theme_inversePrimary_green,
    primaryFixed = R.color.md_theme_primaryFixed_green,
    onPrimaryFixed = R.color.md_theme_onPrimaryFixed_green,
    primaryFixedDim = R.color.md_theme_primaryFixedDim_green,
    onPrimaryFixedVariant = R.color.md_theme_onPrimaryFixedVariant_green,
    secondaryFixed = R.color.md_theme_secondaryFixed_green,
    onSecondaryFixed = R.color.md_theme_onSecondaryFixed_green,
    secondaryFixedDim = R.color.md_theme_secondaryFixedDim_green,
    onSecondaryFixedVariant = R.color.md_theme_onSecondaryFixedVariant_green,
    tertiaryFixed = R.color.md_theme_tertiaryFixed_green,
    onTertiaryFixed = R.color.md_theme_onTertiaryFixed_green,
    tertiaryFixedDim = R.color.md_theme_tertiaryFixedDim_green,
    onTertiaryFixedVariant = R.color.md_theme_onTertiaryFixedVariant_green,
    surfaceDim = R.color.md_theme_surfaceDim_green,
    surfaceBright = R.color.md_theme_surfaceBright_green,
    surfaceContainerLowest = R.color.md_theme_surfaceContainerLowest_green,
    surfaceContainerLow = R.color.md_theme_surfaceContainerLow_green,
    surfaceContainer = R.color.md_theme_surfaceContainer_green,
    surfaceContainerHigh = R.color.md_theme_surfaceContainerHigh_green,
    surfaceContainerHighest = R.color.md_theme_surfaceContainerHighest_green,
)
private val BlueSpec = ThemeColorSpec(
    primary = R.color.md_theme_primary_blue,
    onPrimary = R.color.md_theme_onPrimary_blue,
    primaryContainer = R.color.md_theme_primaryContainer_blue,
    onPrimaryContainer = R.color.md_theme_onPrimaryContainer_blue,
    secondary = R.color.md_theme_secondary_blue,
    onSecondary = R.color.md_theme_onSecondary_blue,
    secondaryContainer = R.color.md_theme_secondaryContainer_blue,
    onSecondaryContainer = R.color.md_theme_onSecondaryContainer_blue,
    tertiary = R.color.md_theme_tertiary_blue,
    onTertiary = R.color.md_theme_onTertiary_blue,
    tertiaryContainer = R.color.md_theme_tertiaryContainer_blue,
    onTertiaryContainer = R.color.md_theme_onTertiaryContainer_blue,
    error = R.color.md_theme_error_blue,
    onError = R.color.md_theme_onError_blue,
    errorContainer = R.color.md_theme_errorContainer_blue,
    onErrorContainer = R.color.md_theme_onErrorContainer_blue,
    background = R.color.md_theme_background_blue,
    onBackground = R.color.md_theme_onBackground_blue,
    surface = R.color.md_theme_surface_blue,
    onSurface = R.color.md_theme_onSurface_blue,
    surfaceVariant = R.color.md_theme_surfaceVariant_blue,
    onSurfaceVariant = R.color.md_theme_onSurfaceVariant_blue,
    outline = R.color.md_theme_outline_blue,
    outlineVariant = R.color.md_theme_outlineVariant_blue,
    inverseSurface = R.color.md_theme_inverseSurface_blue,
    inverseOnSurface = R.color.md_theme_inverseOnSurface_blue,
    inversePrimary = R.color.md_theme_inversePrimary_blue,
    primaryFixed = R.color.md_theme_primaryFixed_blue,
    onPrimaryFixed = R.color.md_theme_onPrimaryFixed_blue,
    primaryFixedDim = R.color.md_theme_primaryFixedDim_blue,
    onPrimaryFixedVariant = R.color.md_theme_onPrimaryFixedVariant_blue,
    secondaryFixed = R.color.md_theme_secondaryFixed_blue,
    onSecondaryFixed = R.color.md_theme_onSecondaryFixed_blue,
    secondaryFixedDim = R.color.md_theme_secondaryFixedDim_blue,
    onSecondaryFixedVariant = R.color.md_theme_onSecondaryFixedVariant_blue,
    tertiaryFixed = R.color.md_theme_tertiaryFixed_blue,
    onTertiaryFixed = R.color.md_theme_onTertiaryFixed_blue,
    tertiaryFixedDim = R.color.md_theme_tertiaryFixedDim_blue,
    onTertiaryFixedVariant = R.color.md_theme_onTertiaryFixedVariant_blue,
    surfaceDim = R.color.md_theme_surfaceDim_blue,
    surfaceBright = R.color.md_theme_surfaceBright_blue,
    surfaceContainerLowest = R.color.md_theme_surfaceContainerLowest_blue,
    surfaceContainerLow = R.color.md_theme_surfaceContainerLow_blue,
    surfaceContainer = R.color.md_theme_surfaceContainer_blue,
    surfaceContainerHigh = R.color.md_theme_surfaceContainerHigh_blue,
    surfaceContainerHighest = R.color.md_theme_surfaceContainerHighest_blue,
)
private val YellowSpec = ThemeColorSpec(
    primary = R.color.md_theme_primary_yellow,
    onPrimary = R.color.md_theme_onPrimary_yellow,
    primaryContainer = R.color.md_theme_primaryContainer_yellow,
    onPrimaryContainer = R.color.md_theme_onPrimaryContainer_yellow,
    secondary = R.color.md_theme_secondary_yellow,
    onSecondary = R.color.md_theme_onSecondary_yellow,
    secondaryContainer = R.color.md_theme_secondaryContainer_yellow,
    onSecondaryContainer = R.color.md_theme_onSecondaryContainer_yellow,
    tertiary = R.color.md_theme_tertiary_yellow,
    onTertiary = R.color.md_theme_onTertiary_yellow,
    tertiaryContainer = R.color.md_theme_tertiaryContainer_yellow,
    onTertiaryContainer = R.color.md_theme_onTertiaryContainer_yellow,
    error = R.color.md_theme_error_yellow,
    onError = R.color.md_theme_onError_yellow,
    errorContainer = R.color.md_theme_errorContainer_yellow,
    onErrorContainer = R.color.md_theme_onErrorContainer_yellow,
    background = R.color.md_theme_background_yellow,
    onBackground = R.color.md_theme_onBackground_yellow,
    surface = R.color.md_theme_surface_yellow,
    onSurface = R.color.md_theme_onSurface_yellow,
    surfaceVariant = R.color.md_theme_surfaceVariant_yellow,
    onSurfaceVariant = R.color.md_theme_onSurfaceVariant_yellow,
    outline = R.color.md_theme_outline_yellow,
    outlineVariant = R.color.md_theme_outlineVariant_yellow,
    inverseSurface = R.color.md_theme_inverseSurface_yellow,
    inverseOnSurface = R.color.md_theme_inverseOnSurface_yellow,
    inversePrimary = R.color.md_theme_inversePrimary_yellow,
    primaryFixed = R.color.md_theme_primaryFixed_yellow,
    onPrimaryFixed = R.color.md_theme_onPrimaryFixed_yellow,
    primaryFixedDim = R.color.md_theme_primaryFixedDim_yellow,
    onPrimaryFixedVariant = R.color.md_theme_onPrimaryFixedVariant_yellow,
    secondaryFixed = R.color.md_theme_secondaryFixed_yellow,
    onSecondaryFixed = R.color.md_theme_onSecondaryFixed_yellow,
    secondaryFixedDim = R.color.md_theme_secondaryFixedDim_yellow,
    onSecondaryFixedVariant = R.color.md_theme_onSecondaryFixedVariant_yellow,
    tertiaryFixed = R.color.md_theme_tertiaryFixed_yellow,
    onTertiaryFixed = R.color.md_theme_onTertiaryFixed_yellow,
    tertiaryFixedDim = R.color.md_theme_tertiaryFixedDim_yellow,
    onTertiaryFixedVariant = R.color.md_theme_onTertiaryFixedVariant_yellow,
    surfaceDim = R.color.md_theme_surfaceDim_yellow,
    surfaceBright = R.color.md_theme_surfaceBright_yellow,
    surfaceContainerLowest = R.color.md_theme_surfaceContainerLowest_yellow,
    surfaceContainerLow = R.color.md_theme_surfaceContainerLow_yellow,
    surfaceContainer = R.color.md_theme_surfaceContainer_yellow,
    surfaceContainerHigh = R.color.md_theme_surfaceContainerHigh_yellow,
    surfaceContainerHighest = R.color.md_theme_surfaceContainerHighest_yellow,
)
private val PinkSpec = ThemeColorSpec(
    primary = R.color.md_theme_primary_pink,
    onPrimary = R.color.md_theme_onPrimary_pink,
    primaryContainer = R.color.md_theme_primaryContainer_pink,
    onPrimaryContainer = R.color.md_theme_onPrimaryContainer_pink,
    secondary = R.color.md_theme_secondary_pink,
    onSecondary = R.color.md_theme_onSecondary_pink,
    secondaryContainer = R.color.md_theme_secondaryContainer_pink,
    onSecondaryContainer = R.color.md_theme_onSecondaryContainer_pink,
    tertiary = R.color.md_theme_tertiary_pink,
    onTertiary = R.color.md_theme_onTertiary_pink,
    tertiaryContainer = R.color.md_theme_tertiaryContainer_pink,
    onTertiaryContainer = R.color.md_theme_onTertiaryContainer_pink,
    error = R.color.md_theme_error_pink,
    onError = R.color.md_theme_onError_pink,
    errorContainer = R.color.md_theme_errorContainer_pink,
    onErrorContainer = R.color.md_theme_onErrorContainer_pink,
    background = R.color.md_theme_background_pink,
    onBackground = R.color.md_theme_onBackground_pink,
    surface = R.color.md_theme_surface_pink,
    onSurface = R.color.md_theme_onSurface_pink,
    surfaceVariant = R.color.md_theme_surfaceVariant_pink,
    onSurfaceVariant = R.color.md_theme_onSurfaceVariant_pink,
    outline = R.color.md_theme_outline_pink,
    outlineVariant = R.color.md_theme_outlineVariant_pink,
    inverseSurface = R.color.md_theme_inverseSurface_pink,
    inverseOnSurface = R.color.md_theme_inverseOnSurface_pink,
    inversePrimary = R.color.md_theme_inversePrimary_pink,
    primaryFixed = R.color.md_theme_primaryFixed_pink,
    onPrimaryFixed = R.color.md_theme_onPrimaryFixed_pink,
    primaryFixedDim = R.color.md_theme_primaryFixedDim_pink,
    onPrimaryFixedVariant = R.color.md_theme_onPrimaryFixedVariant_pink,
    secondaryFixed = R.color.md_theme_secondaryFixed_pink,
    onSecondaryFixed = R.color.md_theme_onSecondaryFixed_pink,
    secondaryFixedDim = R.color.md_theme_secondaryFixedDim_pink,
    onSecondaryFixedVariant = R.color.md_theme_onSecondaryFixedVariant_pink,
    tertiaryFixed = R.color.md_theme_tertiaryFixed_pink,
    onTertiaryFixed = R.color.md_theme_onTertiaryFixed_pink,
    tertiaryFixedDim = R.color.md_theme_tertiaryFixedDim_pink,
    onTertiaryFixedVariant = R.color.md_theme_onTertiaryFixedVariant_pink,
    surfaceDim = R.color.md_theme_surfaceDim_pink,
    surfaceBright = R.color.md_theme_surfaceBright_pink,
    surfaceContainerLowest = R.color.md_theme_surfaceContainerLowest_pink,
    surfaceContainerLow = R.color.md_theme_surfaceContainerLow_pink,
    surfaceContainer = R.color.md_theme_surfaceContainer_pink,
    surfaceContainerHigh = R.color.md_theme_surfaceContainerHigh_pink,
    surfaceContainerHighest = R.color.md_theme_surfaceContainerHighest_pink,
)
private val CyanSpec = ThemeColorSpec(
    primary = R.color.md_theme_primary_light_cyan,
    onPrimary = R.color.md_theme_onPrimary_light_cyan,
    primaryContainer = R.color.md_theme_primaryContainer_light_cyan,
    onPrimaryContainer = R.color.md_theme_onPrimaryContainer_light_cyan,
    secondary = R.color.md_theme_secondary_light_cyan,
    onSecondary = R.color.md_theme_onSecondary_light_cyan,
    secondaryContainer = R.color.md_theme_secondaryContainer_light_cyan,
    onSecondaryContainer = R.color.md_theme_onSecondaryContainer_light_cyan,
    tertiary = R.color.md_theme_tertiary_light_cyan,
    onTertiary = R.color.md_theme_onTertiary_light_cyan,
    tertiaryContainer = R.color.md_theme_tertiaryContainer_light_cyan,
    onTertiaryContainer = R.color.md_theme_onTertiaryContainer_light_cyan,
    error = R.color.md_theme_error_light_cyan,
    onError = R.color.md_theme_onError_light_cyan,
    errorContainer = R.color.md_theme_errorContainer_light_cyan,
    onErrorContainer = R.color.md_theme_onErrorContainer_light_cyan,
    background = R.color.md_theme_background_light_cyan,
    onBackground = R.color.md_theme_onBackground_light_cyan,
    surface = R.color.md_theme_surface_light_cyan,
    onSurface = R.color.md_theme_onSurface_light_cyan,
    surfaceVariant = R.color.md_theme_surfaceVariant_light_cyan,
    onSurfaceVariant = R.color.md_theme_onSurfaceVariant_light_cyan,
    outline = R.color.md_theme_outline_light_cyan,
    outlineVariant = R.color.md_theme_outlineVariant_light_cyan,
    inverseSurface = R.color.md_theme_inverseSurface_light_cyan,
    inverseOnSurface = R.color.md_theme_inverseOnSurface_light_cyan,
    inversePrimary = R.color.md_theme_inversePrimary_light_cyan,
    primaryFixed = R.color.md_theme_primaryFixed_light_cyan,
    onPrimaryFixed = R.color.md_theme_onPrimaryFixed_light_cyan,
    primaryFixedDim = R.color.md_theme_primaryFixedDim_light_cyan,
    onPrimaryFixedVariant = R.color.md_theme_onPrimaryFixedVariant_light_cyan,
    secondaryFixed = R.color.md_theme_secondaryFixed_light_cyan,
    onSecondaryFixed = R.color.md_theme_onSecondaryFixed_light_cyan,
    secondaryFixedDim = R.color.md_theme_secondaryFixedDim_light_cyan,
    onSecondaryFixedVariant = R.color.md_theme_onSecondaryFixedVariant_light_cyan,
    tertiaryFixed = R.color.md_theme_tertiaryFixed_light_cyan,
    onTertiaryFixed = R.color.md_theme_onTertiaryFixed_light_cyan,
    tertiaryFixedDim = R.color.md_theme_tertiaryFixedDim_light_cyan,
    onTertiaryFixedVariant = R.color.md_theme_onTertiaryFixedVariant_light_cyan,
    surfaceDim = R.color.md_theme_surfaceDim_light_cyan,
    surfaceBright = R.color.md_theme_surfaceBright_light_cyan,
    surfaceContainerLowest = R.color.md_theme_surfaceContainerLowest_light_cyan,
    surfaceContainerLow = R.color.md_theme_surfaceContainerLow_light_cyan,
    surfaceContainer = R.color.md_theme_surfaceContainer_light_cyan,
    surfaceContainerHigh = R.color.md_theme_surfaceContainerHigh_light_cyan,
    surfaceContainerHighest = R.color.md_theme_surfaceContainerHighest_light_cyan,
)

@Composable
private fun themeColorScheme(spec: ThemeColorSpec, darkTheme: Boolean): ColorScheme {
    val context = LocalContext.current
    val currentConfig = LocalConfiguration.current

    return remember(spec, darkTheme) {
        val configuration = Configuration(currentConfig).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    if (darkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
        val themedContext = context.createConfigurationContext(configuration)
        fun color(@ColorRes id: Int): Color = Color(ContextCompat.getColor(themedContext, id))

        if (darkTheme) {
            darkColorScheme(
                primary = color(spec.primary),
                onPrimary = color(spec.onPrimary),
                primaryContainer = color(spec.primaryContainer),
                onPrimaryContainer = color(spec.onPrimaryContainer),
                secondary = color(spec.secondary),
                onSecondary = color(spec.onSecondary),
                secondaryContainer = color(spec.secondaryContainer),
                onSecondaryContainer = color(spec.onSecondaryContainer),
                tertiary = color(spec.tertiary),
                onTertiary = color(spec.onTertiary),
                tertiaryContainer = color(spec.tertiaryContainer),
                onTertiaryContainer = color(spec.onTertiaryContainer),
                error = color(spec.error),
                onError = color(spec.onError),
                errorContainer = color(spec.errorContainer),
                onErrorContainer = color(spec.onErrorContainer),
                background = color(spec.background),
                onBackground = color(spec.onBackground),
                surface = color(spec.surface),
                onSurface = color(spec.onSurface),
                surfaceVariant = color(spec.surfaceVariant),
                onSurfaceVariant = color(spec.onSurfaceVariant),
                outline = color(spec.outline),
                outlineVariant = color(spec.outlineVariant),
                inverseSurface = color(spec.inverseSurface),
                inverseOnSurface = color(spec.inverseOnSurface),
                inversePrimary = color(spec.inversePrimary),
                primaryFixed = color(spec.primaryFixed),
                onPrimaryFixed = color(spec.onPrimaryFixed),
                primaryFixedDim = color(spec.primaryFixedDim),
                onPrimaryFixedVariant = color(spec.onPrimaryFixedVariant),
                secondaryFixed = color(spec.secondaryFixed),
                onSecondaryFixed = color(spec.onSecondaryFixed),
                secondaryFixedDim = color(spec.secondaryFixedDim),
                onSecondaryFixedVariant = color(spec.onSecondaryFixedVariant),
                tertiaryFixed = color(spec.tertiaryFixed),
                onTertiaryFixed = color(spec.onTertiaryFixed),
                tertiaryFixedDim = color(spec.tertiaryFixedDim),
                onTertiaryFixedVariant = color(spec.onTertiaryFixedVariant),
                surfaceDim = color(spec.surfaceDim),
                surfaceBright = color(spec.surfaceBright),
                surfaceContainerLowest = color(spec.surfaceContainerLowest),
                surfaceContainerLow = color(spec.surfaceContainerLow),
                surfaceContainer = color(spec.surfaceContainer),
                surfaceContainerHigh = color(spec.surfaceContainerHigh),
                surfaceContainerHighest = color(spec.surfaceContainerHighest),
            )
        } else {
            lightColorScheme(
                primary = color(spec.primary),
                onPrimary = color(spec.onPrimary),
                primaryContainer = color(spec.primaryContainer),
                onPrimaryContainer = color(spec.onPrimaryContainer),
                secondary = color(spec.secondary),
                onSecondary = color(spec.onSecondary),
                secondaryContainer = color(spec.secondaryContainer),
                onSecondaryContainer = color(spec.onSecondaryContainer),
                tertiary = color(spec.tertiary),
                onTertiary = color(spec.onTertiary),
                tertiaryContainer = color(spec.tertiaryContainer),
                onTertiaryContainer = color(spec.onTertiaryContainer),
                error = color(spec.error),
                onError = color(spec.onError),
                errorContainer = color(spec.errorContainer),
                onErrorContainer = color(spec.onErrorContainer),
                background = color(spec.background),
                onBackground = color(spec.onBackground),
                surface = color(spec.surface),
                onSurface = color(spec.onSurface),
                surfaceVariant = color(spec.surfaceVariant),
                onSurfaceVariant = color(spec.onSurfaceVariant),
                outline = color(spec.outline),
                outlineVariant = color(spec.outlineVariant),
                inverseSurface = color(spec.inverseSurface),
                inverseOnSurface = color(spec.inverseOnSurface),
                inversePrimary = color(spec.inversePrimary),
                primaryFixed = color(spec.primaryFixed),
                onPrimaryFixed = color(spec.onPrimaryFixed),
                primaryFixedDim = color(spec.primaryFixedDim),
                onPrimaryFixedVariant = color(spec.onPrimaryFixedVariant),
                secondaryFixed = color(spec.secondaryFixed),
                onSecondaryFixed = color(spec.onSecondaryFixed),
                secondaryFixedDim = color(spec.secondaryFixedDim),
                onSecondaryFixedVariant = color(spec.onSecondaryFixedVariant),
                tertiaryFixed = color(spec.tertiaryFixed),
                onTertiaryFixed = color(spec.onTertiaryFixed),
                tertiaryFixedDim = color(spec.tertiaryFixedDim),
                onTertiaryFixedVariant = color(spec.onTertiaryFixedVariant),
                surfaceDim = color(spec.surfaceDim),
                surfaceBright = color(spec.surfaceBright),
                surfaceContainerLowest = color(spec.surfaceContainerLowest),
                surfaceContainerLow = color(spec.surfaceContainerLow),
                surfaceContainer = color(spec.surfaceContainer),
                surfaceContainerHigh = color(spec.surfaceContainerHigh),
                surfaceContainerHighest = color(spec.surfaceContainerHighest),
            )
        }
    }
}

@Composable
fun DailyTheme(
    themeType: DailyThemeType = DailyThemeType.DEFAULT,
    themeMode: Int = 0,
    dynamicColor: Boolean = false,
    isAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeType == DailyThemeType.RED -> themeColorScheme(RedSpec, darkTheme)
        themeType == DailyThemeType.GREEN -> themeColorScheme(GreenSpec, darkTheme)
        themeType == DailyThemeType.BLUE -> themeColorScheme(BlueSpec, darkTheme)
        themeType == DailyThemeType.YELLOW -> themeColorScheme(YellowSpec, darkTheme)
        themeType == DailyThemeType.PINK -> themeColorScheme(PinkSpec, darkTheme)
        themeType == DailyThemeType.CYAN -> themeColorScheme(CyanSpec, darkTheme)
        else -> if (darkTheme) DefaultDarkColorScheme else DefaultLightColorScheme
    }

    val finalColorScheme = remember(colorScheme, darkTheme, isAmoled) {
        if (darkTheme && isAmoled) {
            colorScheme.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceContainer = Color.Black,
                surfaceContainerLow = Color(0xFF0F0F0F),
                surfaceContainerLowest = Color.Black,
                surfaceVariant = Color.Black,
                onBackground = Color.White,
                onSurface = Color.White
            )
        } else {
            colorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}