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

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.liuxing.daily.R
import com.liuxing.daily.ui.compose.button.ExpressiveButton
import com.liuxing.daily.ui.compose.list.ListItemCard
import com.liuxing.daily.ui.compose.list.ListItemData
import com.liuxing.daily.ui.compose.scaffold.WallpaperScaffold
import com.liuxing.daily.ui.compose.scaffold.getTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperScreen(
    wallpaperBitmap: Bitmap?,
    initialWallpaperAlpha: Float,
    initialCardAlpha: Float = 0.7f,
    onWallpaperAlphaChange: (Float) -> Unit,
    onCardAlphaChange: (Float) -> Unit,
    onAddWallpaperClick: () -> Unit,
    onDeleteWallpaperClick: () -> Unit,
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

        Box(modifier = Modifier.fillMaxSize()) {
            // 预览列表
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                repeat(3) { index ->
                    ListItemCard(
                        data = ListItemData(
                            title = "示例条目 ${index + 1}",
                            supportingText = "预览卡片透明度效果",
                            index = index,
                            count = 3
                        ),
                        alpha = animatedCardAlpha
                    ) { }
                }
                Spacer(modifier = Modifier.height(320.dp))
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 按钮组
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
                            Icon(painterResource(R.drawable.baseline_add_circle_outline_24), null)
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

@Composable
private fun OpacitySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            value = value,
            onValueChange = { onValueChange(it.coerceIn(0f, 100f)) },
            valueRange = 0f..100f,
            steps = 99
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
            onWallpaperAlphaChange = {},
            onCardAlphaChange = {},
            onAddWallpaperClick = {},
            onDeleteWallpaperClick = {},
            {}
        )
    }
}
