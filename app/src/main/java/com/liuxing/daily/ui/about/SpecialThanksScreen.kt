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


package com.liuxing.daily.ui.about

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuxing.daily.R
import com.liuxing.daily.data.SpecialThanksData
import com.liuxing.daily.ui.compose.scaffold.WallpaperScaffold
import com.liuxing.daily.ui.compose.scaffold.getCardBackgroundColor
import com.liuxing.daily.ui.compose.scaffold.getTopAppBarColors

/**
 * 特别鸣谢 & 开源库 统一界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialThanksScreen(
    title: String,
    wallpaperBitmap: Bitmap?,
    wallpaperAlpha: Float,
    cardAlpha: Float,
    dataList: List<SpecialThanksData>,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    WallpaperScaffold(
        wallpaperBitmap = wallpaperBitmap,
        wallpaperAlpha = wallpaperAlpha,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(8.dp).size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = wallpaperBitmap.getTopAppBarColors()
            )
        }
    ) { contentPadding, hasWallpaper ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(dataList) { index, item ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // 弹性缩放
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.98f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "scale"
                )

                // 圆角形变
                // 顶部圆角：如果是第一个或者被按下，则为 32dp，否则为 4dp
                val topRadius by animateDpAsState(
                    targetValue = if (isPressed || index == 0) 32.dp else 4.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "topCorner"
                )
                // 底部圆角：如果是最后一个或者被按下，则为 32dp，否则为 4dp
                val bottomRadius by animateDpAsState(
                    targetValue = if (isPressed || index == dataList.size - 1) 32.dp else 4.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "bottomCorner"
                )
                
                val currentShape = RoundedCornerShape(
                    topStart = topRadius, 
                    topEnd = topRadius, 
                    bottomStart = bottomRadius, 
                    bottomEnd = bottomRadius
                )

                val visibleState = remember {
                    MutableTransitionState(false).apply { targetState = true }
                }

                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                ) {
                    val cardColor = wallpaperBitmap.getCardBackgroundColor(alpha = cardAlpha)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(currentShape)
                            .clickable(
                                interactionSource = interactionSource,
                                onClick = {
                                    if (!item.link.isNullOrBlank()) {
                                        try { uriHandler.openUri(item.link) } catch (_: Exception) { }
                                    }
                                }
                            ),
                        shape = currentShape,
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (hasWallpaper) 0.dp else 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = item.name ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (!item.remark.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SelectionContainer {
                                        Text(
                                            text = item.remark,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                lineHeight = 18.sp,
                                                letterSpacing = 0.2.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            if (!item.link.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.link,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
