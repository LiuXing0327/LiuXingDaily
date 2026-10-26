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

package com.liuxing.daily.ui.wallpaper

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.liuxing.daily.R
import com.liuxing.daily.ui.compose.button.ExpressiveButton
import com.liuxing.daily.ui.compose.list.ListItemCard
import com.liuxing.daily.ui.compose.list.ListItemData
import com.liuxing.daily.ui.compose.scaffold.WallpaperScaffold
import com.liuxing.daily.ui.compose.scaffold.getTopAppBarColors
import com.liuxing.daily.util.ConstUtil

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun WallpaperScreen(
    wallpaperBitmap: Bitmap?,
    initialWallpaperAlpha: Float,
    initialCardAlpha: Float = 0.7f,
    selectedBuiltInResId: Int = 0,
    onWallpaperAlphaChange: (Float) -> Unit,
    onCardAlphaChange: (Float) -> Unit,
    onAddWallpaperClick: () -> Unit,
    onDeleteWallpaperClick: () -> Unit,
    onSelectBuiltInWallpaperClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    // 滑块值（0-100）
    var wallpaperSliderValue by remember(initialWallpaperAlpha) {
        mutableFloatStateOf(kotlin.math.round(initialWallpaperAlpha * 100f))
    }
    var cardSliderValue by remember(initialCardAlpha) {
        mutableFloatStateOf(kotlin.math.round(initialCardAlpha * 100f))
    }

    // 卡片透明度动画（局部预览效果）
    val animatedCardAlpha by animateFloatAsState(
        targetValue = cardSliderValue / 100f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardAlpha"
    )

    WallpaperScaffold(
        wallpaperBitmap = wallpaperBitmap,
        wallpaperAlpha = wallpaperSliderValue / 100f,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.wallpaper),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
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
                },
                colors = wallpaperBitmap.getTopAppBarColors()
            )
        }
    ) { paddingValues, _ ->
        var isPresetsExpanded by rememberSaveable { mutableStateOf(false) }

        val springExpandSpec = spring<IntSize>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        val springShrinkSpec = spring<IntSize>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        val springFadeInSpec = spring<Float>(
            stiffness = Spring.StiffnessMediumLow
        )
        val springFadeOutSpec = spring<Float>(
            stiffness = Spring.StiffnessMediumLow
        )

        val springEnter =
            expandVertically(animationSpec = springExpandSpec) + fadeIn(animationSpec = springFadeInSpec)
        val springExit =
            shrinkVertically(animationSpec = springShrinkSpec) + fadeOut(animationSpec = springFadeOutSpec)

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val showPreview = maxHeight >= 720.dp
            val density = LocalDensity.current
            val collapsedItemLift = 8.dp
            var previewItemHeight by remember { mutableStateOf(72.dp) }
            val collapseProgress = remember {
                Animatable(if (isPresetsExpanded) 1f else 0f)
            }
            var previewItemsAreComposed by remember {
                mutableStateOf(!isPresetsExpanded)
            }

            LaunchedEffect(isPresetsExpanded) {
                if (isPresetsExpanded) {
                    collapseProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                    previewItemsAreComposed = false
                } else {
                    previewItemsAreComposed = true
                    collapseProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                }
            }

            val previewContainerHeight = previewItemHeight * (3f - 2f * collapseProgress.value)

            Box(modifier = Modifier.fillMaxSize()) {
                if (showPreview) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(previewContainerHeight)
                                .graphicsLayer { clip = true }
                        ) {
                            repeat(3) { index ->
                                if (index == 0 || previewItemsAreComposed) {
                                    val itemAlpha = if (index == 0) {
                                        1f
                                    } else {
                                        (1f - collapseProgress.value / 0.5f).coerceIn(0f, 1f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(
                                                y = previewItemHeight * index *
                                                        (1f - collapseProgress.value) -
                                                        collapsedItemLift * index * collapseProgress.value
                                            )
                                            .zIndex(if (index == 0) 1f else 0f)
                                            .graphicsLayer { alpha = itemAlpha }
                                            .then(
                                                if (index == 0) {
                                                    Modifier.onSizeChanged { size ->
                                                        previewItemHeight = with(density) {
                                                            size.height.toDp()
                                                        }
                                                    }
                                                } else Modifier
                                            )
                                    ) {
                                        ListItemCard(
                                            data = ListItemData(
                                                title = stringResource(
                                                    R.string.preview_sample_entry,
                                                    index + 1
                                                ),
                                                supportingText = stringResource(R.string.preview_card_opacity_effect),
                                                index = index,
                                                count = 3
                                            ),
                                            alpha = animatedCardAlpha,
                                            shape = if (index == 0) {
                                                val bottomRadius =
                                                    4.dp + 12.dp * collapseProgress.value
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = bottomRadius,
                                                    bottomEnd = bottomRadius
                                                )
                                            } else {
                                                null
                                            }
                                        ) { }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(320.dp))
                    }
                }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (ConstUtil.backgroundWallpaperMap.isNotEmpty()) {

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.preset_wallpapers),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                FilledTonalIconButton(
                                    onClick = { isPresetsExpanded = !isPresetsExpanded }
                                ) {
                                    Icon(
                                        imageVector = if (isPresetsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = stringResource(
                                            if (isPresetsExpanded) R.string.collapse else R.string.expand
                                        )
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isPresetsExpanded,
                                enter = springEnter,
                                exit = springExit
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    val carouselState =
                                        rememberCarouselState { ConstUtil.backgroundWallpaperMap.size }

                                    HorizontalMultiBrowseCarousel(
                                        state = carouselState,
                                        preferredItemWidth = 116.dp,
                                        itemSpacing = 12.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .padding(top = 12.dp)
                                    ) { index ->
                                        if (index >= ConstUtil.backgroundWallpaperMap.size) {
                                            return@HorizontalMultiBrowseCarousel
                                        }
                                        val presets =
                                            ConstUtil.backgroundWallpaperMap.keys.elementAt(index)
                                        val drawableRes =
                                            ConstUtil.backgroundWallpaperMap.values.elementAt(index)
                                        val isSelected = (selectedBuiltInResId == drawableRes)

                                        Card(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .maskClip(MaterialTheme.shapes.medium)
                                                .then(
                                                    if (isSelected) {
                                                        Modifier.border(
                                                            width = 3.dp,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            shape = RoundedCornerShape(16.dp)
                                                        )
                                                    } else Modifier
                                                )
                                                .clickable {
                                                    onSelectBuiltInWallpaperClick(
                                                        ConstUtil.backgroundWallpaperMap.values.elementAt(
                                                            index
                                                        )
                                                    )
                                                },
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(id = presets),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )

                                                if (isSelected) {
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape,
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                            modifier = Modifier.padding(4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                        }
                    }

                    // 按钮组（仅在预设折叠时显示）
                    AnimatedVisibility(
                        visible = !isPresetsExpanded,
                        enter = springEnter,
                        exit = springExit
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ExpressiveButton(
                                    onClick = onAddWallpaperClick,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                ) {
                                    Icon(
                                        painterResource(R.drawable.baseline_add_circle_outline_24),
                                        null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.change_wallpaper))
                                }

                                ExpressiveButton(
                                    onClick = onDeleteWallpaperClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = Modifier
                                        .height(56.dp)
                                        .alpha(if (wallpaperBitmap != null) 1f else 0.5f),
                                    enabled = wallpaperBitmap != null
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        stringResource(R.string.delete),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // 壁纸透明度控制
                    OpacitySlider(
                        label = stringResource(R.string.wallpaper_opacity),
                        value = wallpaperSliderValue,
                        onValueChange = {
                            val rounded = kotlin.math.round(it)
                            wallpaperSliderValue = rounded
                            onWallpaperAlphaChange(rounded / 100f)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 卡片透明度控制
                    OpacitySlider(
                        label = stringResource(R.string.card_opacity),
                        value = cardSliderValue,
                        onValueChange = {
                            val rounded = kotlin.math.round(it)
                            cardSliderValue = rounded
                            onCardAlphaChange(rounded / 100f)
                        }
                    )
                }
                }
            }
        }
    }
}

@Composable
private fun OpacitySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val sliderState = rememberSliderState(
        value,
        0,
        0f..100f
    )
    sliderState.value = value

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = label + " " + value.toInt() + "%"
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                "${value.toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            state = sliderState,
            onValueChange = onValueChange
        )
    }
}

@Composable
@Preview(showBackground = true)
fun WallpaperScreenPreview() {
    MaterialTheme {
        WallpaperScreen(
            wallpaperBitmap = null,
            initialWallpaperAlpha = 0.15f,
            initialCardAlpha = 0.7f,
            selectedBuiltInResId = 0,
            onWallpaperAlphaChange = {},
            onCardAlphaChange = {},
            onAddWallpaperClick = {},
            onDeleteWallpaperClick = {},
            onSelectBuiltInWallpaperClick = {},
            onBack = {}
        )
    }
}
