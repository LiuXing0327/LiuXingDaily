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

package com.liuxing.daily.ui.appearance

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.material.color.DynamicColors
import com.liuxing.daily.R
import com.liuxing.daily.ui.compose.list.ListItemCard
import com.liuxing.daily.ui.compose.list.ListItemData
import com.liuxing.daily.ui.compose.list.ListItemTrailing
import com.liuxing.daily.ui.compose.scaffold.WallpaperScaffold
import com.liuxing.daily.ui.compose.scaffold.getTopAppBarColors
import com.liuxing.daily.ui.compose.theme.DailyThemeManager
import com.liuxing.daily.ui.compose.theme.DailyThemeType

private const val THEME_ITEM_COUNT = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    wallpaperBitmap: Bitmap?, wallpaperAlpha: Float, cardAlpha: Float, onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeType = DailyThemeManager.currentThemeType
    val isDynamic = DailyThemeManager.isDynamicColor
    val themeMode = DailyThemeManager.themeMode
    val isAmoled = DailyThemeManager.isAmoled

    val isSupportDynamic = DynamicColors.isDynamicColorAvailable()
    val expressiveShape = RoundedCornerShape(32.dp)

    WallpaperScaffold(
        wallpaperBitmap = wallpaperBitmap, wallpaperAlpha = wallpaperAlpha, topBar = {
            TopAppBar(title = {
                Text(
                    stringResource(R.string.appearance), style = MaterialTheme.typography.titleLarge
                )
            }, navigationIcon = {
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, colors = wallpaperBitmap.getTopAppBarColors())
        }) { contentPadding, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                shape = expressiveShape, colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                ), modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.theme_mode),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            ContextCompat.getString(context, R.string.light_mode),
                            ContextCompat.getString(context, R.string.night_mode),
                            ContextCompat.getString(context, R.string.follow_the_system)
                        )
                        options.forEachIndexed { index, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index, count = options.size
                                ),
                                onClick = { DailyThemeManager.updateThemeMode(index + 1) },
                                selected = themeMode == index + 1
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSupportDynamic) {
                ListItemCard(
                    data = ListItemData(
                        title = ContextCompat.getString(context, R.string.dynamic_color),
                        supportingText = ContextCompat.getString(
                            context, R.string.dynamic_color_supporting_string
                        ),
                        index = 0,
                        count = THEME_ITEM_COUNT
                    ),
                    alpha = cardAlpha,
                    trailing = ListItemTrailing.Switch(isDynamic),
                    onAction = {
                        DailyThemeManager.updateDynamicColor(context, !isDynamic)
                    })
            }

            val showBuiltInThemes = !isSupportDynamic || !isDynamic
            AnimatedVisibility(
                visible = showBuiltInThemes,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = expressiveShape, colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                        ), modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {

                            val themeOptions = listOf(
                                ThemeColorInfo(
                                    DailyThemeType.DEFAULT, 0, Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primaryFixed
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primary
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_inversePrimary
                                        )
                                    )
                                ), ThemeColorInfo(
                                    DailyThemeType.RED, 1, Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primaryFixed_red
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primary_red
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_inversePrimary_red
                                        )
                                    )
                                ), ThemeColorInfo(
                                    DailyThemeType.GREEN, 2, Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primaryFixedDim_green
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primary_green
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_inversePrimary_green
                                        )
                                    )
                                ), ThemeColorInfo(
                                    DailyThemeType.BLUE, 3, Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primaryFixedDim_blue
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primary_blue
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_inversePrimary_blue
                                        )
                                    )
                                ), ThemeColorInfo(
                                    DailyThemeType.YELLOW, 4, Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primaryFixedDim_yellow
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primary_yellow
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_inversePrimary_yellow
                                        )
                                    )
                                ), ThemeColorInfo(
                                    DailyThemeType.PINK, 5, Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primaryFixedDim_pink
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primary_pink
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_inversePrimary_pink
                                        )
                                    )
                                ), ThemeColorInfo(
                                    DailyThemeType.CYAN, 6, Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primaryFixed_light_cyan
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_primary_light_cyan
                                        )
                                    ), Color(
                                        ContextCompat.getColor(
                                            context, R.color.md_theme_inversePrimary_light_cyan
                                        )
                                    )
                                )
                            )

                            @OptIn(ExperimentalLayoutApi::class) FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                maxItemsInEachRow = 7,
                            ) {
                                themeOptions.forEach { info ->
                                    ComposeRoundTricolorView(
                                        info = info,
                                        isSelected = themeType == info.type,
                                        onClick = {
                                            DailyThemeManager.updateThemeType(
                                                context, info.type, info.id
                                            )
                                        })
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ComposeRoundTricolorView(info: ThemeColorInfo, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 12.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "corner"
    )

    Box(modifier = Modifier
        .size(72.dp)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(RoundedCornerShape(cornerRadius))
        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
        .clickable(
            interactionSource = interactionSource, indication = null, onClick = onClick
        ), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(42.dp)) {
            drawArc(color = info.p, startAngle = 180f, sweepAngle = 180f, useCenter = true)
            drawArc(color = info.s, startAngle = 90f, sweepAngle = 90f, useCenter = true)
            drawArc(color = info.t, startAngle = 0f, sweepAngle = 90f, useCenter = true)
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

data class ThemeColorInfo(
    val type: DailyThemeType, val id: Int, val p: Color, val s: Color, val t: Color
)
